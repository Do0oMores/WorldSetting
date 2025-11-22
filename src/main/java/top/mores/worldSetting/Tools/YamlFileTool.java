package top.mores.worldSetting.Tools;

import org.bukkit.configuration.file.FileConfiguration;
import top.mores.worldSetting.WorldSetting;

import java.util.List;

public class YamlFileTool {

    private FileConfiguration getConfig() {
        return WorldSetting.getInstance().getConfigFile();
    }

    /**
     * List<String>/ Get lock move worlds
     * @return worlds name list
     */
    public List<String> getLockMoveWorlds(){
        return getConfig().getStringList("LockMoveWorlds");
    }

    /**
     * Integer/ Get lock move time
     * @return time UNIT:s
     */
    public Integer getLockMoveTime(){
        return getConfig().getInt("LockMoveTime");
    }

    /**
     * List<String>/ Get control worlds
     * @return control worlds name list
     */
    public List<String> getControlWorlds(){
        return getConfig().getStringList("ControlWorlds");
    }
}
