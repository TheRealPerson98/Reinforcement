package com.person98.wifispy.util;

import com.person98.wifispy.Wifispy;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Wifispy plugin;
    private final FileConfiguration config;

    public ConfigManager(Wifispy plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    public int getTntDamage() {
        return config.getInt("tnt_damage", 2); // Default is 2 if not set in the config
    }
    public int getHologramDisplayRange() {
        return config.getInt("hologram.display_range", 5);  // Default is 5 if not set in the config
    }
    public int getReinforcementValue(String materialName) {
        return config.getInt("reinforcement." + materialName.toLowerCase(), 0);
    }
    public String getMessage(String key) {
        return this.plugin.getConfig().getString("messages." + key);
    }

}
