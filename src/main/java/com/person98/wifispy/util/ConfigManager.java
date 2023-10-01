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

    public int getReinforcementValue(String materialName) {
        return config.getInt("reinforcement." + materialName.toLowerCase(), 0);
    }
    public String getMessage(String key) {
        return this.plugin.getConfig().getString("messages." + key);
    }

}
