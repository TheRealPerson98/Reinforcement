package com.person98.wifispy.commands;

import com.person98.wifispy.database.DatabaseManager;
import com.person98.wifispy.database.ReinforcedBlockEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class ReinforceCommand implements CommandExecutor {

    private Plugin plugin; // reference to your main plugin instance
    private DatabaseManager dbManager; // reference to your DatabaseManager instance

    public ReinforceCommand(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        // if command is "/reinforce trust <username>"
        if (args.length == 2 && args[0].equalsIgnoreCase("trust")) {
            String trustedUsername = args[1];
            Player trustedPlayer = Bukkit.getPlayer(trustedUsername);

            if (trustedPlayer == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            // Fetch all reinforced blocks owned by the player
            // This part assumes a method like `getAllReinforcedBlocksByOwner` exists in your dbManager.
            // You would need to implement this method or modify as per your implementation.
            for (ReinforcedBlockEntry entry : dbManager.getAllReinforcedBlocksByOwner(player.getUniqueId().toString())) {
                entry.setTrusted(trustedPlayer.getUniqueId().toString());
                dbManager.updateReinforcedBlock(entry); // This updates the block entry in the database.
            }

            player.sendMessage(ChatColor.GREEN + trustedUsername + " has been trusted with your reinforced blocks!");
            trustedPlayer.sendMessage(ChatColor.GREEN + player.getName() + " has trusted you with their reinforced blocks!");
            return true;
        }

        // Check if player has the permission to reinforce

            // Grant them the 'CanReinforce' metadata
            player.setMetadata("CanReinforce", new FixedMetadataValue(plugin, true));

            // Schedule the removal of the metadata after 5 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) { // check if player is still online
                    player.removeMetadata("CanReinforce", plugin);
                }
            }, 5 * 20L); // 5 seconds * 20 (ticks per second)


        return true;
    }
}
