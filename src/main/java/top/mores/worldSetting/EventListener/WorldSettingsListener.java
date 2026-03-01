package top.mores.worldSetting.EventListener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import top.mores.worldSetting.Tools.YamlFileTool;
import top.mores.worldSetting.WorldSetting;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldSettingsListener implements Listener {

    YamlFileTool yamlFileTool=new YamlFileTool();
    private final Map<UUID,Long> joinTimes=new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> removalTasks=new ConcurrentHashMap<>();

    private boolean shouldIgnoreRestrictions(Player player) {
        GameMode gameMode = player.getGameMode();
        return gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
    }

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
        if (shouldIgnoreRestrictions(player)) return;
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
        Player player = event.getPlayer();
        if (shouldIgnoreRestrictions(player)) return;

        if (!yamlFileTool.getLockMoveWorlds().contains(player.getWorld().getName())) return;
        if (!joinTimes.containsKey(player.getUniqueId())) return;

        long joinTime = joinTimes.get(player.getUniqueId());
        if (System.currentTimeMillis() - joinTime >= yamlFileTool.getLockMoveTime() * 1000L) {
            cancelRestrictionFor(player);
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {

            Location fixed = from.clone();
            fixed.setYaw(to.getYaw());
            fixed.setPitch(to.getPitch());
            event.setTo(fixed);
        }
    }

    @EventHandler
    public void onPlayerSwitchHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (shouldIgnoreRestrictions(player)) return;
        if (!yamlFileTool.getLockMoveWorlds().contains(player.getWorld().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (shouldIgnoreRestrictions(player)) return;
        if (!yamlFileTool.getLockMoveWorlds().contains(player.getWorld().getName())) return;
        if (event.getSlot() == 40) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (shouldIgnoreRestrictions(player)) return;
        if (!yamlFileTool.getLockMoveWorlds().contains(player.getWorld().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (shouldIgnoreRestrictions(player)) return;
        if (!yamlFileTool.getLockMoveWorlds().contains(player.getWorld().getName())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getState() instanceof InventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onUseItemWithLore(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (shouldIgnoreRestrictions(player)) return;
        if (!yamlFileTool.getUseTagsWorlds().contains(player.getWorld().getName())) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (!meta.hasLore()) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;

        for (String tag : yamlFileTool.getUseTags()) {
            for (String line : lore) {
                if (line.contains(tag)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
