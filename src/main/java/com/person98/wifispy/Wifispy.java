package com.person98.wifispy;

import com.person98.wifispy.commands.ReinforceCommand;
import com.person98.wifispy.util.ConfigManager;
import com.person98.wifispy.util.HologramManager;
import com.person98.wifispy.util.ReinforcementManager;
import com.person98.wifispy.util.listeners.BlockListener;
import com.person98.wifispy.database.DatabaseManager;
import com.person98.wifispy.util.listeners.ExplosionListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Wifispy extends JavaPlugin {

    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private ReinforcementManager reinforcementManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();  // Create default configuration if it doesn't exist

        // Output Java version
        System.out.println("Java Version: " + System.getProperty("java.version"));

        // Check for DecentHolograms plugin
        Plugin decentHolograms = getServer().getPluginManager().getPlugin("DecentHolograms");
        if (decentHolograms != null && decentHolograms.isEnabled()) {
            System.out.println("DecentHolograms is installed and enabled!");
        } else {
            System.out.println("DecentHolograms is not installed or not enabled!");
        }

        // Initialize managers
        this.databaseManager = new DatabaseManager();
        this.configManager = new ConfigManager(this);
        this.reinforcementManager = new ReinforcementManager(this, configManager, databaseManager);
        databaseManager.initializeDatabase();

        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockListener(databaseManager, configManager), this);
        getServer().getPluginManager().registerEvents(reinforcementManager, this);  // Register ReinforcementManager as a listener
        getServer().getPluginManager().registerEvents(new ExplosionListener(databaseManager, configManager), this);

        this.getCommand("reinforce").setExecutor(new ReinforceCommand(this, databaseManager, configManager));
    }

    @Override
    public void onDisable() {
    }
}