package com.person98.reinforcement;

import com.person98.reinforcement.commands.ReinforceCommand;
import com.person98.reinforcement.database.HeartManager;
import com.person98.reinforcement.util.ConfigManager;
import com.person98.reinforcement.util.ConfigVersionChecker;
import com.person98.reinforcement.util.HeartCoordinatesUtil;
import com.person98.reinforcement.util.ReinforcementManager;
import com.person98.reinforcement.util.listeners.BlockListener;
import com.person98.reinforcement.database.DatabaseManager;
import com.person98.reinforcement.util.listeners.ExplosionListener;
import com.person98.reinforcement.util.listeners.HeartListener;
import com.person98.reinforcement.util.tasks.HeartBlockCleaner;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Reinforcement extends JavaPlugin {

    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private ReinforcementManager reinforcementManager;
    private HeartManager heartManager;
    private HeartCoordinatesUtil heartCoordinatesUtil;
    private static Economy econ = null;

    @Override
    public void onEnable() {

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


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
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(configManager);

        this.reinforcementManager = new ReinforcementManager(this, configManager, databaseManager);
        databaseManager.initializeDatabase();
        this.heartManager = new HeartManager(databaseManager);
        heartCoordinatesUtil = new HeartCoordinatesUtil(this, heartManager, configManager);
        heartCoordinatesUtil.monitorEntryExitProximity();
        heartCoordinatesUtil.monitorLocationUpdate();
        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockListener(databaseManager, configManager), this);
        getServer().getPluginManager().registerEvents(reinforcementManager, this);  // Register ReinforcementManager as a listener
        getServer().getPluginManager().registerEvents(new ExplosionListener(databaseManager, configManager), this);
        getServer().getPluginManager().registerEvents(new HeartListener(this, databaseManager, heartManager, configManager, heartCoordinatesUtil), this);

        this.getCommand("reinforce").setExecutor(new ReinforceCommand(this, databaseManager, configManager, heartManager));

        ConfigVersionChecker versionChecker = new ConfigVersionChecker(this);
        versionChecker.checkVersion();

        HeartBlockCleaner cleaner = new HeartBlockCleaner(databaseManager, heartManager, configManager);
        cleaner.schedule();

    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public Economy getEconomy() {
        return econ;
    }

    @Override
    public void onDisable() {
    }
}