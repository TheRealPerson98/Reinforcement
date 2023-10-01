package com.person98.wifispy.util.listeners;

import com.person98.wifispy.database.DatabaseManager;
import com.person98.wifispy.database.ReinforcedBlockEntry;
import com.person98.wifispy.util.ConfigManager;
import com.person98.wifispy.util.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
        this.hologramManager = new HologramManager(configManager);
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

                // Notify the player if they are the owner or trusted
                if (player.getUniqueId().toString().equals(owner)) {
                    String msgForOwner = configManager.getMessage("notify_owner_broke_own_block")
                            .replace("%x%", String.valueOf(entry.getX()))
                            .replace("%y%", String.valueOf(entry.getY()))
                            .replace("%z%", String.valueOf(entry.getZ()));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForOwner));
                } else {
                    String msgForTrusted = configManager.getMessage("notify_trusted_broke_trusted_block")
                            .replace("%x%", String.valueOf(entry.getX()))
                            .replace("%y%", String.valueOf(entry.getY()))
                            .replace("%z%", String.valueOf(entry.getZ()));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForTrusted));
                }

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
                            String msgForOwner = configManager.getMessage("notify_owner_damage")
                                    .replace("%x%", String.valueOf(entry.getX()))
                                    .replace("%y%", String.valueOf(entry.getY()))
                                    .replace("%z%", String.valueOf(entry.getZ()));
                            ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForOwner));
                        }

                        if (trusted != null) {
                            for (String trustedUuid : trusted.split(",")) {
                                Player trustedPlayer = Bukkit.getPlayer(UUID.fromString(trustedUuid.trim()));
                                if (trustedPlayer != null) {
                                    String msgForTrusted = configManager.getMessage("notify_trusted_damage")
                                            .replace("%x%", String.valueOf(entry.getX()))
                                            .replace("%y%", String.valueOf(entry.getY()))
                                            .replace("%z%", String.valueOf(entry.getZ()));
                                    trustedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForTrusted));
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
                    msgForOwner = msgForOwner.replace("%breaker_name%", player.getName())
                            .replace("%x%", String.valueOf(entry.getX()))
                            .replace("%y%", String.valueOf(entry.getY()))
                            .replace("%z%", String.valueOf(entry.getZ()));
                    ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForOwner));
                }

                if (trusted != null && !trusted.isEmpty()) {
                    for (String trustedUuid : trusted.split(",")) {
                        Player trustedPlayer = Bukkit.getPlayer(UUID.fromString(trustedUuid.trim()));
                        if (trustedPlayer != null && trustedPlayer.isOnline()) {
                            String msgForTrusted = configManager.getMessage("notify_trusted_block_broken");
                            msgForTrusted = msgForTrusted.replace("%breaker_name%", player.getName())
                                    .replace("%x%", String.valueOf(entry.getX()))
                                    .replace("%y%", String.valueOf(entry.getY()))
                                    .replace("%z%", String.valueOf(entry.getZ()));
                            trustedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForTrusted));
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();

            if (block != null && isContainer(block)) {
                String world = block.getWorld().getName();
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();

                ReinforcedBlockEntry entry = dbManager.getReinforcedBlock(world, x, y, z);

                if (entry != null) {
                    Player player = event.getPlayer();
                    String owner = entry.getOwnerUuid();
                    String trusted = entry.getTrusted();

                    // If the player is neither the owner nor trusted, they can't access the container
                    if (!player.getUniqueId().toString().equals(owner) && (trusted == null || !trusted.contains(player.getUniqueId().toString()))) {
                        String message = configManager.getMessage("check_block_reinforced");
                        message = message.replace("%remaining_hp%", String.valueOf(entry.getHp()));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    // Helper method to check if a block is a container
    private boolean isContainer(Block block) {
        return block.getState() instanceof Container;
    }
}
