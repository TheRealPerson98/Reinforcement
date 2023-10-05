package com.person98.reinforcement.commands;

import com.person98.reinforcement.database.DatabaseManager;
import com.person98.reinforcement.database.HeartManager;
import com.person98.reinforcement.database.ReinforcedBlockEntry;
import com.person98.reinforcement.util.ConfigManager;
import com.person98.reinforcement.util.Heart;
import com.person98.reinforcement.util.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class ReinforceCommand implements CommandExecutor {

    private Plugin plugin;
    private DatabaseManager dbManager;
    private ConfigManager configManager;
    private HologramManager hologramManager;
    private HeartManager heartManager;

    public ReinforceCommand(Plugin plugin, DatabaseManager dbManager, ConfigManager configManager, HeartManager heartManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.configManager = configManager;
        this.hologramManager = new HologramManager(configManager);
        this.heartManager = new HeartManager(dbManager);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        // if command is "/reinforce trust <username>"

        if (args.length == 1 && args[0].equalsIgnoreCase("holo")) {
            List<String> allHoloNames = dbManager.getAllHoloIds(); // Fetch all hologram names from database

            if (player.hasMetadata("ReinforceHoloHidden")) {
                for (String holoName : allHoloNames) {
                    hologramManager.showHologram(player, holoName); // Make the specific hologram visible
                }
                player.removeMetadata("ReinforceHoloHidden", plugin);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("holo_visible")));
            } else {
                for (String holoName : allHoloNames) {
                    hologramManager.hideHologram(player, holoName); // Hide the specific hologram
                }
                player.setMetadata("ReinforceHoloHidden", new FixedMetadataValue(plugin, true));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("holo_disabled")));

            }
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.GREEN + "---- Reinforce Commands ----");
            player.sendMessage(ChatColor.GOLD + "/reinforce" + ChatColor.WHITE + " - Toggle reinforce mode.");
            player.sendMessage(ChatColor.GOLD + "/reinforce trust <username>" + ChatColor.WHITE + " - Trust a player.");
            player.sendMessage(ChatColor.GOLD + "/reinforce trustlist" + ChatColor.WHITE + " - View your trusted players.");
            player.sendMessage(ChatColor.GOLD + "/reinforce untrust <username>" + ChatColor.WHITE + " - Untrust a player.");
            player.sendMessage(ChatColor.GOLD + "/reinforce holo" + ChatColor.WHITE + " - Toggle holograms visibility.");
            player.sendMessage(ChatColor.GOLD + "/reinforce help" + ChatColor.WHITE + " - Display this help message.");
            player.sendMessage(ChatColor.GOLD + "/reinforce heart <name>" + ChatColor.WHITE + " - Create a heart with a given name.");
            player.sendMessage(ChatColor.GOLD + "/reinforce destroyheart" + ChatColor.WHITE + " - Destroy your heart reinforcement.");

            if(player.isOp()) {  // Check if player has operator permissions
                player.sendMessage(ChatColor.RED + "---- Admin Commands ----");
                player.sendMessage(ChatColor.GOLD + "/reinforce admin destroy" + ChatColor.WHITE + " - Destroy a target's heart.");
                player.sendMessage(ChatColor.GOLD + "/reinforce admin heartlist" + ChatColor.WHITE + " - List all hearts on the server.");
            }

            return true;
        }


        if (args.length == 2 && args[0].equalsIgnoreCase("trust")) {
            String trustedUsername = args[1];

            UUID trustedPlayerUUID = null;
            for (OfflinePlayer offPlayer : Bukkit.getOfflinePlayers()) {
                if (offPlayer.getName().equalsIgnoreCase(trustedUsername)) {
                    trustedPlayerUUID = offPlayer.getUniqueId();
                    break;
                }
            }

            if (trustedPlayerUUID == null) {
                player.sendMessage(ChatColor.RED + "Player not found or has never joined the server!");
                return true;
            }

            // Fetch all reinforced blocks owned by the player
            for (ReinforcedBlockEntry entry : dbManager.getAllReinforcedBlocksByOwner(player.getUniqueId().toString())) {
                entry.setTrusted(trustedPlayerUUID.toString());
                dbManager.updateReinforcedBlock(entry);
            }

            String message = configManager.getMessage("owner_trusted_player");
            message = message.replace("%player_name%", trustedUsername);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            Player onlineTrustedPlayer = Bukkit.getPlayer(trustedPlayerUUID);
            if (onlineTrustedPlayer != null) {
                String message2 = configManager.getMessage("player_trusted_owner");
                message2 = message2.replace("%player_name%", player.getName());
                onlineTrustedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', message2));
            }

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("trustlist")) {
            List<ReinforcedBlockEntry> entries = dbManager.getAllReinforcedBlocksByOwner(player.getUniqueId().toString());
            Set<String> trustedUUIDs = entries.stream()
                    .map(ReinforcedBlockEntry::getTrusted)
                    .filter(Objects::nonNull)
                    .flatMap(trusted -> Arrays.stream(trusted.split(",")))
                    .collect(Collectors.toSet());

            if (trustedUUIDs.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "You haven't trusted anyone yet.");
                return true;
            }

            player.sendMessage(ChatColor.GREEN + "Trusted players:");
            for (String uuid : trustedUUIDs) {
                OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                player.sendMessage(ChatColor.GOLD + "- " + trustedPlayer.getName());
            }
            return true;
        }

        // untrust subcommand
        if (args.length == 2 && args[0].equalsIgnoreCase("untrust")) {
            String untrustedUsername = args[1];

            UUID untrustedPlayerUUID = null;
            for (OfflinePlayer offPlayer : Bukkit.getOfflinePlayers()) {
                if (offPlayer.getName().equalsIgnoreCase(untrustedUsername)) {
                    untrustedPlayerUUID = offPlayer.getUniqueId();
                    break;
                }
            }

            if (untrustedPlayerUUID == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            final UUID finalUntrustedPlayerUUID = untrustedPlayerUUID; // Create an effectively final variable

            boolean untrusted = false;
            List<ReinforcedBlockEntry> entries = dbManager.getAllReinforcedBlocksByOwner(player.getUniqueId().toString());
            for (ReinforcedBlockEntry entry : entries) {
                if (entry.getTrusted() != null && entry.getTrusted().contains(finalUntrustedPlayerUUID.toString())) {
                    String updatedTrustList = Arrays.stream(entry.getTrusted().split(","))
                            .filter(uuid -> !uuid.equals(finalUntrustedPlayerUUID.toString()))
                            .collect(Collectors.joining(","));

                    entry.setTrusted(updatedTrustList.isEmpty() ? null : updatedTrustList);
                    dbManager.updateReinforcedBlock(entry);
                    untrusted = true;
                }
            }

            if (untrusted) {
                player.sendMessage(ChatColor.GREEN + untrustedUsername + " has been untrusted from all your blocks.");
            } else {
                player.sendMessage(ChatColor.RED + untrustedUsername + " was not trusted on any of your blocks.");
            }

            return true;
        }


            // For the /reinforce heart <name> command
        if (args.length == 2 && args[0].equalsIgnoreCase("heart")) {
            String heartName = args[1].trim();  // Trim to remove any leading or trailing whitespace

            if (heartName.isEmpty()) {  // Check if the heartName is empty after trimming
                player.sendMessage(ChatColor.RED + "You must provide a valid name for your heart!");
                return true;
            }

            // Ensure player doesn't already have a heart
            if (heartManager.getHeartByOwner(player.getUniqueId().toString())) {
                player.sendMessage(ChatColor.RED + "You already have a heart. Destroy it first if you want to set a new one.");
                return true;
            }

            player.setMetadata("SettingHeart", new FixedMetadataValue(plugin, heartName));
            player.sendMessage(ChatColor.GREEN + "Right-click the block you want to set as your heart named: " + heartName);
            return true;
        }


        // For the /reinforce destroyheart command
        if (args.length == 1 && args[0].equalsIgnoreCase("destroyheart")) {
            if (heartManager.removeHeart(player.getUniqueId())) {
                player.sendMessage(ChatColor.GREEN + "Your heart has been removed.");
            } else {
                player.sendMessage(ChatColor.RED + "You don't have a heart set.");
            }
            return true;
        }

        if (player.hasMetadata("CanReinforce")) {
            player.removeMetadata("CanReinforce", plugin);
            player.sendMessage(ChatColor.RED + "Reinforce mode disabled!");
        } else {
            player.setMetadata("CanReinforce", new FixedMetadataValue(plugin, true));
            player.sendMessage(ChatColor.GREEN + "Reinforce mode enabled!");
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("admin")) {
            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            if (args[1].equalsIgnoreCase("destroy") && args.length == 3) {
                String targetPlayerName = args[2];
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);

                if (heartManager.removeHeart(targetPlayer.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + targetPlayerName + "'s heart has been removed.");
                } else {
                    player.sendMessage(ChatColor.RED + targetPlayerName + " doesn't have a heart set.");
                }
                return true;
            } else if (args[1].equalsIgnoreCase("heartlist")) {
                List<Heart> allHearts = heartManager.getAllHearts();
                if (allHearts.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "There are no hearts set on the server.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Listing all hearts on the server:");
                    for (Heart heart : allHearts) {
                        player.sendMessage(ChatColor.GOLD + heart.getName() + " owned by " + Bukkit.getOfflinePlayer(heart.getOwnerUUID()).getName());
                    }
                }
                return true;
            }
        }


        return true;
    }
}