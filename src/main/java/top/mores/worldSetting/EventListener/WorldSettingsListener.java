package top.mores.worldSetting.EventListener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;
import top.mores.worldSetting.Tools.YamlFileTool;
import top.mores.worldSetting.WorldSetting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldSettingsListener implements Listener {

    YamlFileTool yamlFileTool=new YamlFileTool();
    private final Map<UUID,Long> joinTimes=new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> removalTasks=new ConcurrentHashMap<>();

    private void startRestrictionFor(Player p) {
        UUID id = p.getUniqueId();
        cancelRestrictionFor(p);
        joinTimes.put(id, System.currentTimeMillis());
        BukkitTask task = Bukkit.getScheduler().runTaskLater(WorldSetting.getInstance(), () -> {
            joinTimes.remove(id);
            removalTasks.remove(id);
        }, yamlFileTool.getLockMoveTime() * 20L);
        removalTasks.put(id, task);
    }

    private void cancelRestrictionFor(Player p) {
        UUID id = p.getUniqueId();
        joinTimes.remove(id);
        BukkitTask t = removalTasks.remove(id);
        if (t != null) {
            t.cancel();
        }
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        Player player=event.getPlayer();
        if (yamlFileTool.getLockMoveWorlds().contains(player.getWorld().getName())) {
            startRestrictionFor(player);
        }else {
            cancelRestrictionFor(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelRestrictionFor(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        cancelRestrictionFor(event.getEntity());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        cancelRestrictionFor(event.getPlayer());
    }

    @EventHandler
    public void controlPlayerMove(PlayerMoveEvent event) {
        Player player=event.getPlayer();
        if(!yamlFileTool.getLockMoveWorlds().contains(player.getWorld().getName())) return;
        if(!joinTimes.containsKey(player.getUniqueId())) return;

        long joinTime = joinTimes.get(player.getUniqueId());
        if (System.currentTimeMillis() - joinTime >= yamlFileTool.getLockMoveTime() * 1000L) {
            cancelRestrictionFor(player);
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to==null) return;

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location fixed = from.clone();
            fixed.setYaw(to.getYaw());
            fixed.setPitch(to.getPitch());
            event.setTo(fixed);
        }
    }
}
