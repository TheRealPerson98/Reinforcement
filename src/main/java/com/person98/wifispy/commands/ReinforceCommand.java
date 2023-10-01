package com.person98.wifispy.commands;

import com.person98.wifispy.database.DatabaseManager;
import com.person98.wifispy.database.ReinforcedBlockEntry;
import com.person98.wifispy.util.ConfigManager;
import com.person98.wifispy.util.HologramManager;
import eu.decentsoftware.holograms.api.DHAPI;
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

    public ReinforceCommand(Plugin plugin, DatabaseManager dbManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.configManager = configManager;
        this.hologramManager = new HologramManager(configManager);

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
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("trust")) {
            String trustedUsername = args[1];
            OfflinePlayer trustedOfflinePlayer = Bukkit.getOfflinePlayer(trustedUsername);

            if (!trustedOfflinePlayer.hasPlayedBefore()) {  // If the player never played before
                player.sendMessage(ChatColor.RED + "Player not found or has never joined the server!");
                return true;
            }

            UUID trustedPlayerUUID = trustedOfflinePlayer.getUniqueId();

            // Fetch all reinforced blocks owned by the player
            for (ReinforcedBlockEntry entry : dbManager.getAllReinforcedBlocksByOwner(player.getUniqueId().toString())) {
                entry.setTrusted(trustedPlayerUUID.toString());
                dbManager.updateReinforcedBlock(entry);
            }

            String message = configManager.getMessage("owner_trusted_player");
            message = message.replace("%player_name%", trustedUsername);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            if (trustedOfflinePlayer.isOnline()) {  // Only send the message if the player is online
                Player onlineTrustedPlayer = (Player) trustedOfflinePlayer;
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
            OfflinePlayer untrustedPlayer = Bukkit.getOfflinePlayer(untrustedUsername);

            if (!untrustedPlayer.hasPlayedBefore()) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            boolean untrusted = false;
            List<ReinforcedBlockEntry> entries = dbManager.getAllReinforcedBlocksByOwner(player.getUniqueId().toString());
            for (ReinforcedBlockEntry entry : entries) {
                if (entry.getTrusted() != null && entry.getTrusted().contains(untrustedPlayer.getUniqueId().toString())) {
                    String updatedTrustList = Arrays.stream(entry.getTrusted().split(","))
                            .filter(uuid -> !uuid.equals(untrustedPlayer.getUniqueId().toString()))
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

        if (player.hasMetadata("CanReinforce")) {
            player.removeMetadata("CanReinforce", plugin);
            player.sendMessage(ChatColor.RED + "Reinforce mode disabled!");
        } else {
            player.setMetadata("CanReinforce", new FixedMetadataValue(plugin, true));
            player.sendMessage(ChatColor.GREEN + "Reinforce mode enabled!");
        }

        return true;
    }
}