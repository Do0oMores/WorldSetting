package top.mores.worldSetting;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.mores.worldSetting.EventListener.WorldSettingsListener;

import java.io.File;

public final class WorldSetting extends JavaPlugin {

    public static WorldSetting instance;
    public FileConfiguration config;
    private File configFile;

    @Override
    public void onEnable() {
        instance = this;
        initConfig();

        this.getServer().getPluginManager().registerEvents(new WorldSettingsListener(),this);
    }

    @Override
    public void onDisable() {

    }

    //Plugin instance
    public static WorldSetting getInstance() {
        return instance;
    }

    public void reloadConfigFile() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfigFile() {
        if (configFile == null) {
            reloadConfigFile();
        }
        return config;
    }

    private void initConfig(){
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            boolean isCreateDir = configFile.getParentFile().mkdirs();
            if (!isCreateDir) {
                getLogger().warning("创建config.yml目录失败");
                return;
            }
            saveResource("config.yml", false);
        }
        reloadConfigFile();
    }
}
