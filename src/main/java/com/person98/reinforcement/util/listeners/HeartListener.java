package com.person98.reinforcement.util.listeners;

import com.person98.reinforcement.Reinforcement;
import com.person98.reinforcement.database.DatabaseManager;
import com.person98.reinforcement.database.ReinforcedBlockEntry;
import com.person98.reinforcement.database.HeartManager;
import com.person98.reinforcement.util.ConfigManager;
import com.person98.reinforcement.util.Heart;
import com.person98.reinforcement.util.HeartCoordinatesUtil;
import com.person98.reinforcement.util.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class HeartListener implements Listener {

    private Reinforcement plugin;
    private DatabaseManager dbManager;
    private HeartManager heartManager;
    private HologramManager hologramManager;
    private ConfigManager configManager;
    private HeartCoordinatesUtil heartCoordinatesUtil; // 1. Add a reference for HeartCoordinatesUtil
    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHANUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";

    public HeartListener(Reinforcement plugin, DatabaseManager dbManager, HeartManager heartManager, ConfigManager configManager, HeartCoordinatesUtil heartCoordinatesUtil) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.heartManager = heartManager;
        this.configManager = configManager;
        this.hologramManager = new HologramManager(configManager);
        this.heartCoordinatesUtil = heartCoordinatesUtil; // Set the HeartCoordinatesUtil instance
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("SettingHeart")) {
            Block block = event.getClickedBlock();
            if (block == null) return; // Ensure clicked block is not null
            Location proposedLocation = block.getLocation();

            String heartName = (String) player.getMetadata("SettingHeart").get(0).value();


            int notifyDistance = configManager.getNotifyDistance();
            for (Heart heart : heartManager.getAllHearts()) {
                Location heartLocation = new Location(Bukkit.getWorld(heart.getWorld()), heart.getX(), heart.getY(), heart.getZ());
                if (heartLocation.getWorld().equals(proposedLocation.getWorld()) && heartLocation.distance(proposedLocation) <= notifyDistance) {
                    player.sendMessage(ChatColor.RED + "You cannot set a heart so close to another heart!");
                    return;
                }
            }


            double heartPrice = configManager.getHeartPrice();
            if(plugin.getEconomy().has(player, heartPrice)) {
                plugin.getEconomy().withdrawPlayer(player, heartPrice);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have enough money to set a heart! It costs " + heartPrice);
                return;
            }

            // Retrieve trusted users from reinforced blocks
            List<ReinforcedBlockEntry> entries = dbManager.getAllReinforcedBlocksByOwner(player.getUniqueId().toString());
            List<String> trustedUUIDs = entries.stream()
                    .map(ReinforcedBlockEntry::getTrusted)
                    .filter(Objects::nonNull)
                    .flatMap(trusted -> Arrays.stream(trusted.split(",")))
                    .distinct()
                    .collect(Collectors.toList());

            // Unique holo ID generation and holo creation
            String holoId = generateRandomString(24);
            Location holoLocation = block.getLocation().add(0.5, 2, 0.5);
            int startingHP = configManager.getStartingHP();
            hologramManager.createHologram(holoId, holoLocation, Arrays.asList(ChatColor.RED + Integer.toString(startingHP) + " ❤"), true);

            // Heart creation and immediate tracking
            heartManager.createHeart(player.getUniqueId(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), trustedUUIDs, holoId, heartName);
            String trustedString = String.join(",", trustedUUIDs);  // Convert the List<String> to a comma-separated String
            Heart newHeart = new Heart(player.getUniqueId(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), startingHP, trustedString, holoId, heartName);

            player.sendMessage(ChatColor.GREEN + "Heart " + heartName + " has been set!");
            player.removeMetadata("SettingHeart", plugin);
            event.setCancelled(true); // Prevent further interaction with the block
        }
    }




    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player breaker = event.getPlayer();

        // Retrieve heart by its location
        Heart heart = heartManager.getHeart(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());

        // If block being broken isn't a heart, do nothing
        if (heart == null) {
            return;
        }

        boolean isOwner = heart.getOwnerUUID().toString().equals(breaker.getUniqueId().toString());
        boolean isTrusted = Arrays.asList(heart.getTrusted().split(",")).contains(breaker.getUniqueId().toString());

        // If the breaker is the owner or trusted, remove heart completely
        if (isOwner || isTrusted) {
            heart.setHp(0);  // Explicitly set the HP to 0
            hologramManager.deleteHologram(heart.getHoloId());
            heartManager.removeHeart(heart.getOwnerUUID());
            breaker.sendMessage(ChatColor.GREEN + "You have removed a heart you owned or were trusted with!");
            event.setCancelled(false); // Ensure the block breaks
        } else {
            // If the breaker is not the owner or trusted:

            // Decrement HP
            int newHp = heart.getHp() - 1;
            heart.setHp(newHp);

            // Update the heart's HP in the database
            if (!heartManager.updateHeartHP(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), newHp)) {
                plugin.getLogger().warning("Failed to update heart HP in the database.");
            }

            hologramManager.updateHologram(heart.getHoloId(), Arrays.asList(ChatColor.RED.toString() + heart.getHp() + " ❤"));

            if (newHp <= 0) {
                heartManager.removeHeart(heart.getOwnerUUID());
                hologramManager.deleteHologram(heart.getHoloId());

                Player owner = Bukkit.getPlayer(UUID.fromString(heart.getOwnerUUID().toString()));
                if (owner != null) owner.sendMessage(ChatColor.RED + "Your heart was destroyed by " + breaker.getName() + "!");

                for (String trustedUUID : heart.getTrusted().split(",")) {
                    Player trustedPlayer = Bukkit.getPlayer(UUID.fromString(trustedUUID));
                    if (trustedPlayer != null) trustedPlayer.sendMessage(ChatColor.RED + "A heart you were trusted with was destroyed by " + breaker.getName() + "!");
                }

                breaker.sendMessage(ChatColor.GREEN + "You have destroyed a heart!");
            } else {
                // Notification logic
                int initialHp = configManager.getStartingHP();
                if (newHp == initialHp - 1) {
                    // First break
                    notifyOwnerAndTrusted(heart, breaker, block.getLocation(), "heart_first_break_notify_");
                } else if (newHp % 10 == 0) {
                    // Every 10th break
                    notifyOwnerAndTrusted(heart, breaker, block.getLocation(), "heart_break_notify_");
                }

                breaker.sendMessage(ChatColor.YELLOW + "Heart has " + heart.getHp() + " HP left!");
                event.setCancelled(true);
            }
        }
    }

    private void notifyOwnerAndTrusted(Heart heart, Player breaker, Location location, String messagePrefix) {
        UUID ownerUUID = UUID.fromString(heart.getOwnerUUID().toString());
        String ownerMessage = configManager.getMessage(messagePrefix + "owner")
                .replace("%breaker_name%", breaker.getName())
                .replace("%x%", String.valueOf(location.getBlockX()))
                .replace("%y%", String.valueOf(location.getBlockY()))
                .replace("%z%", String.valueOf(location.getBlockZ()))
                .replace("%remaining_hp%", String.valueOf(heart.getHp()));

        if (Bukkit.getPlayer(ownerUUID) != null) {
            Bukkit.getPlayer(ownerUUID).sendMessage(ChatColor.translateAlternateColorCodes('&', ownerMessage));
        }

        for (String trustedUUID : heart.getTrusted().split(",")) {
            Player trustedPlayer = Bukkit.getPlayer(UUID.fromString(trustedUUID));
            if (trustedPlayer != null) {
                String trustedMessage = configManager.getMessage(messagePrefix + "trusted")
                        .replace("%breaker_name%", breaker.getName())
                        .replace("%x%", String.valueOf(location.getBlockX()))
                        .replace("%y%", String.valueOf(location.getBlockY()))
                        .replace("%z%", String.valueOf(location.getBlockZ()))
                        .replace("%remaining_hp%", String.valueOf(heart.getHp()));
                trustedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', trustedMessage));
            }
        }
    }
    private String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHANUMERIC_STRING.charAt(random.nextInt(ALPHANUMERIC_STRING.length())));
        }
        return builder.toString();
    }
}
