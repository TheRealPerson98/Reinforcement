package com.person98.reinforcement.util.tasks;

import com.person98.reinforcement.database.DatabaseManager;
import com.person98.reinforcement.database.HeartManager;
import com.person98.reinforcement.database.ReinforcedBlockEntry;
import com.person98.reinforcement.util.ConfigManager;
import com.person98.reinforcement.util.Heart;
import com.person98.reinforcement.util.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

    public class HeartBlockCleaner implements Runnable {
        private final DatabaseManager dbManager;
        private final HeartManager heartManager;
        private HologramManager hologramManager;
        private ConfigManager configManager;



        public HeartBlockCleaner(DatabaseManager dbManager, HeartManager heartManager,ConfigManager configManager) {
            this.dbManager = dbManager;
            this.heartManager = heartManager;
            this.configManager = configManager;
            this.hologramManager = new HologramManager(configManager);
        }

        @Override
        public void run() {
            // Check all reinforced blocks
            List<ReinforcedBlockEntry> allBlocks = dbManager.getAllReinforcedBlocksByOwner(null);
            for (ReinforcedBlockEntry entry : allBlocks) {
                Block block = Bukkit.getWorld(entry.getWorld()).getBlockAt(new Location(Bukkit.getWorld(entry.getWorld()), entry.getX(), entry.getY(), entry.getZ()));
                if (block.getType() == Material.AIR) {
                    dbManager.removeReinforcedBlock(entry);
                    hologramManager.deleteHologram(entry.getHoloId());

                }
            }

            // Check all hearts
            // Note: I'm assuming you have a method like `getAllHearts` in your DatabaseManager.
            // If not, you'll need to create one.
            List<Heart> allHearts = heartManager.getAllHearts();
            for (Heart heart : allHearts) {
                Block block = Bukkit.getWorld(heart.getWorld()).getBlockAt(new Location(Bukkit.getWorld(heart.getWorld()), heart.getX(), heart.getY(), heart.getZ()));
                if (block.getType() == Material.AIR) {
                    heartManager.removeHeart(heart.getOwnerUUID());
                    hologramManager.deleteHologram(heart.getHoloId());
                }
            }
        }

        public void schedule() {
            // This will run the cleaner every 10 minutes (20 ticks = 1 second, 12000 ticks = 10 minutes)
            Bukkit.getScheduler().runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("Reinforcement"), this, 0L, 12000L);
        }
    }