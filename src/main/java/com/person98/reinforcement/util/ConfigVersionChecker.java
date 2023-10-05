package com.person98.reinforcement.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

public class ConfigVersionChecker {

    private final JavaPlugin plugin;

    public ConfigVersionChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkVersion() {
        plugin.getLogger().info("Checking config version...");

        int internalVersion = 1; // The version inside your plugin
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("ConfigVersion") || config.getInt("ConfigVersion") != internalVersion) {
            plugin.getLogger().info("Updating config.yml...");

            InputStream defaultConfigStream = plugin.getResource("config.yml");
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));

                // Loop through the default config and add missing keys to the server's config
                Set<String> defaultKeys = defaultConfig.getKeys(true);
                for (String key : defaultKeys) {
                    if (!config.contains(key)) {
                        config.set(key, defaultConfig.get(key));
                    }
                }

                // Update the ConfigVersion to match the internal version
                config.set("ConfigVersion", internalVersion);
                plugin.saveConfig();
            }
            plugin.getLogger().info("config.yml updated successfully.");
        } else {
            plugin.getLogger().info("Config version matches the plugin's requirements.");
        }
    }
}
