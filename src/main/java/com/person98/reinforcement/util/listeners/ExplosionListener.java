package com.person98.reinforcement.util.listeners;

import com.person98.reinforcement.database.DatabaseManager;
import com.person98.reinforcement.database.ReinforcedBlockEntry;
import com.person98.reinforcement.util.ConfigManager;
import com.person98.reinforcement.util.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ExplosionListener implements Listener {

    private DatabaseManager dbManager;
    private ConfigManager configManager;
    private HologramManager hologramManager;

    public ExplosionListener(DatabaseManager dbManager, ConfigManager configManager) {
        this.dbManager = dbManager;
        this.configManager = configManager;
        this.hologramManager = new HologramManager(configManager);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        int tntDamage = configManager.getTntDamage();

        // We'll keep track of blocks we need to remove from the explosion list
        List<Block> blocksToPrevent = new ArrayList<>();

        for (Block block : event.blockList()) {
            ReinforcedBlockEntry entry = dbManager.getReinforcedBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
            if (entry != null) {
                if (entry.getHp() > tntDamage) {
                    // Deduct the tntDamage from the block's HP
                    entry.setHp(entry.getHp() - tntDamage);
                    dbManager.updateReinforcedBlock(entry);
                    hologramManager.updateHologram(entry.getHoloId(), Arrays.asList(ChatColor.RED + String.valueOf(entry.getHp()) + " ❤"));

                    blocksToPrevent.add(block);
                } else {
                    // The block's HP is below or equal to tntDamage, so it will be destroyed
                    dbManager.removeReinforcedBlock(entry);
                    hologramManager.deleteHologram(entry.getHoloId());
                }

                // Notifications:
                String owner = entry.getOwnerUuid();
                Player ownerPlayer = Bukkit.getPlayer(UUID.fromString(owner));

                if (ownerPlayer != null) {
                    // Notifying owner of block damage
                    String msgForOwner = configManager.getMessage("tnt_notify_owner_damage");
                    msgForOwner = msgForOwner.replace("%x%", String.valueOf(block.getX()))
                            .replace("%y%", String.valueOf(block.getY()))
                            .replace("%z%", String.valueOf(block.getZ()));
                    ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForOwner));

                    // If block is completely destroyed
                    if (entry.getHp() <= tntDamage) {
                        String msgForOwner1 = configManager.getMessage("notify_owner_block_broken");
                        msgForOwner1 = msgForOwner1.replace("%breaker_name%", event.getEntity() instanceof Player ? ((Player) event.getEntity()).getName() : "Explosion")
                                .replace("%x%", String.valueOf(block.getX()))
                                .replace("%y%", String.valueOf(block.getY()))
                                .replace("%z%", String.valueOf(block.getZ()));
                        ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgForOwner1));
                    }
                }

                // Check for trusted players and notify them
                String trusted = entry.getTrusted();
                if (trusted != null && !trusted.isEmpty()) {
                    for (String trustedUuid : trusted.split(",")) {
                        Player trustedPlayer = Bukkit.getPlayer(UUID.fromString(trustedUuid.trim()));
                        if (trustedPlayer != null) {
                            String messageForTrusted = configManager.getMessage("tnt_notify_trusted_damage");
                            messageForTrusted = messageForTrusted.replace("%x%", String.valueOf(block.getX()))
                                    .replace("%y%", String.valueOf(block.getY()))
                                    .replace("%z%", String.valueOf(block.getZ()));
                            trustedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', messageForTrusted));
                        }
                    }
                }



            }
        }
        event.blockList().removeAll(blocksToPrevent);
    }
}
