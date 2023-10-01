package com.person98.wifispy.util.listeners;

import com.person98.wifispy.database.DatabaseManager;
import com.person98.wifispy.database.ReinforcedBlockEntry;
import com.person98.wifispy.util.ConfigManager;
import com.person98.wifispy.util.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockListener implements Listener {

    private DatabaseManager dbManager;
    private ConfigManager configManager;
    private HologramManager hologramManager;
    private Map<String, Long> lastAlertTimes = new HashMap<>();

    public BlockListener(DatabaseManager dbManager, ConfigManager configManager) {
        this.dbManager = dbManager;
        this.configManager = configManager;
        this.hologramManager = new HologramManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String world = event.getBlock().getWorld().getName();
        int x = event.getBlock().getX();
        int y = event.getBlock().getY();
        int z = event.getBlock().getZ();

        ReinforcedBlockEntry entry = dbManager.getReinforcedBlock(world, x, y, z);

        if (entry != null) {
            String owner = entry.getOwnerUuid();
            String trusted = entry.getTrusted();

            if (player.getUniqueId().toString().equals(owner) || (trusted != null && trusted.contains(player.getUniqueId().toString()))) {
                hologramManager.deleteHologram(entry.getHoloId());
                dbManager.removeReinforcedBlock(entry);
                return;
            }

            if (entry.getHp() > 1) {
                dbManager.decrementBlockHP(entry);
                event.setCancelled(true);
                hologramManager.updateHologram(entry.getHoloId(), Arrays.asList(ChatColor.RED + String.valueOf(entry.getHp() - 1) + " ❤"));

                String message = configManager.getMessage("block_reinforced_hp_remaining");
                message = message.replace("%remaining_hp%", String.valueOf(entry.getHp() - 1));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

                String blockKey = world + "-" + x + "-" + y + "-" + z;
                long currentTime = System.currentTimeMillis();
                if (!lastAlertTimes.containsKey(blockKey) || currentTime - lastAlertTimes.get(blockKey) > 30000) {
                    if ((entry.getHp() - 1) % 5 == 0 || (entry.getHp() - 1) == 1) {
                        Player ownerPlayer = Bukkit.getPlayer(UUID.fromString(owner));
                        if (ownerPlayer != null) {
                            ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("notify_owner_damage")));
                        }

                        if (trusted != null) {
                            for (String trustedUuid : trusted.split(",")) {
                                Player trustedPlayer = Bukkit.getPlayer(UUID.fromString(trustedUuid.trim()));
                                if (trustedPlayer != null) {
                                    trustedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("notify_trusted_damage")));
                                }
                            }
                        }
                        lastAlertTimes.put(blockKey, currentTime);
                    }
                }
            } else if (entry.getHp() == 1) {
                hologramManager.deleteHologram(entry.getHoloId());
                dbManager.removeReinforcedBlock(entry);
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName();
                String msgForBreaker = configManager.getMessage("reinforced_block_broken_by_others");
                msgForBreaker = msgForBreaker.replace("%player_name%", playerName);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForBreaker));

                // Message for the owner of the block
                Player ownerPlayer = Bukkit.getPlayer(UUID.fromString(owner));
                if (ownerPlayer != null && ownerPlayer.isOnline()) {
                    String msgForOwner = configManager.getMessage("notify_owner_block_broken");
                    msgForOwner = msgForOwner.replace("%breaker_name%", player.getName());
                    ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForOwner));
                }
            }
        }
    }
}
