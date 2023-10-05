package com.person98.reinforcement.util;

import com.person98.reinforcement.Reinforcement;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Reinforcement plugin;
    private final FileConfiguration config;

    public ConfigManager(Reinforcement plugin) {
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
    public int getStartingHP() {
        return config.getInt("Hearts.starting_hp", 1000); // Default is 1000 if not set in the config
    }

    public int getNotifyDistance() {
        return config.getInt("Hearts.notifydes", 250); // Default is 250 if not set in the config
    }
    public int getHeartPrice() {
        return config.getInt("Hearts.price", 1000); // Default is 1000 if not set in the config
    }
    public int getHeartTrackingTime() {
        return config.getInt("Hearts.tracking_time", 30);  // Default is 5 seconds if not set in the config
    }

}
