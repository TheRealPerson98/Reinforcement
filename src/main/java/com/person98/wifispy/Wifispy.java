package com.person98.wifispy;

import com.person98.wifispy.commands.ReinforceCommand;
import com.person98.wifispy.util.ConfigManager;
import com.person98.wifispy.util.HologramManager;
import com.person98.wifispy.util.ReinforcementManager;
import com.person98.wifispy.util.listeners.BlockListener;
import com.person98.wifispy.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Wifispy extends JavaPlugin {

    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private ReinforcementManager reinforcementManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();  // Create default configuration if it doesn't exist

        // Initialize managers
        this.databaseManager = new DatabaseManager();
        this.configManager = new ConfigManager(this);  // Assuming you have a constructor that takes a JavaPlugin instance
        this.reinforcementManager = new ReinforcementManager(this, configManager, databaseManager);
        databaseManager.initializeDatabase();
        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockListener(databaseManager, configManager), this);
        getServer().getPluginManager().registerEvents(reinforcementManager, this);  // Register ReinforcementManager as a listener

        this.getCommand("reinforce").setExecutor(new ReinforceCommand(this,databaseManager));

    }

    @Override
    public void onDisable() {

    }
}
