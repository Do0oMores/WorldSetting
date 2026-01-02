package top.mores.worldSetting.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mores.worldSetting.WorldSetting;

public class WorldSettingsCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender,
                             @NotNull Command command,
                             @NotNull String s,
                             String[] strings) {
        if (commandSender instanceof Player) {
            if (strings.length == 1 && strings[0].equals("reload")) {
                if (commandSender.isOp()){
                    WorldSetting.getInstance().reloadConfigFile();
                    commandSender.sendMessage("【WorldSetting】已重载配置文件");
                }
            }
        }
        return true;
    }
}
