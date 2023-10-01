package com.person98.wifispy.util;

import com.person98.wifispy.Wifispy;
import com.person98.wifispy.database.DatabaseManager;
import com.person98.wifispy.database.ReinforcedBlockEntry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.MetadataValue;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ReinforcementManager implements Listener {

    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private static final List<String> REINFORCEMENT_MATERIALS = Arrays.asList("COBBLESTONE", "IRON_INGOT", "GOLD_INGOT", "DIAMOND");
    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHANUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
    private final Wifispy plugin;  // Replace YourPluginClass with the actual class name of your main plugin
    private final HologramManager hologramManager;
    private List<String> unreinforceableBlocks;
    public ReinforcementManager(Wifispy plugin, ConfigManager configManager, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.hologramManager = new HologramManager(configManager);
        this.unreinforceableBlocks = plugin.getConfig().getStringList("unreinforceable-blocks");

    }

    @EventHandler
    public void handleReinforce(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (action == Action.RIGHT_CLICK_BLOCK && block != null && event.getItem() != null) {
            String materialName = event.getItem().getType().name();
            if (player.hasMetadata("CanReinforce")) {
                List<MetadataValue> values = player.getMetadata("CanReinforce");
                for (MetadataValue value : values) {

                    if (unreinforceableBlocks.contains(block.getType().name())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("cant_reinforce")));

                        player.sendMessage(ChatColor.RED + "You can't reinforce this block.");
                        return;
                    }

                    if (REINFORCEMENT_MATERIALS.contains(materialName)) {
                        ReinforcedBlockEntry existingEntry = databaseManager.getReinforcedBlock(
                                block.getWorld().getName(),
                                block.getX(),
                                block.getY(),
                                block.getZ()
                        );

                        // If the block is already reinforced, inform the player and exit
                        if (existingEntry != null) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("already_reinforced")));
                            return;
                        }

                        int hp = configManager.getReinforcementValue(materialName);

                        if (hp > 0) {
                            String holoId = generateRandomString(24);

                            // Create the hologram using the new method
                            Location holoLocation = block.getLocation().add(0.5, 2, 0.5);
                            hologramManager.createHologram(holoId, holoLocation, Arrays.asList(ChatColor.RED + String.valueOf(hp) + " ❤"), true);

                            // Retrieve all reinforced blocks owned by the player
                            List<ReinforcedBlockEntry> blocksByOwner = databaseManager.getAllReinforcedBlocksByOwner(player.getUniqueId().toString());

                            // Extract all trusted UUIDs and store in a set to ensure uniqueness
                            Set<String> trustedUUIDs = blocksByOwner.stream()
                                    .map(ReinforcedBlockEntry::getTrusted)
                                    .filter(Objects::nonNull)
                                    .flatMap(trusted -> Arrays.stream(trusted.split(",")))
                                    .collect(Collectors.toSet());

                            // Convert set to comma-separated string
                            String trustedPlayers = String.join(",", trustedUUIDs);

                            ReinforcedBlockEntry entry = new ReinforcedBlockEntry(
                                    0,
                                    block.getWorld().getName(),
                                    block.getX(),
                                    block.getY(),
                                    block.getZ(),
                                    hp,
                                    System.currentTimeMillis(),
                                    player.getUniqueId().toString(),
                                    holoId, // Store the holo ID
                                    trustedPlayers // Store the merged trusted players
                            );
                            databaseManager.addReinforcedBlock(entry);
                            String message = configManager.getMessage("block_reinforced").replace("%hp%", String.valueOf(hp));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            // Deduct one of the reinforcement items from the player's hand
                            if (event.getItem().getAmount() > 1) {
                                event.getItem().setAmount(event.getItem().getAmount() - 1);
                            } else {
                                player.getInventory().removeItem(event.getItem());
                            }
                        }
                    }
                }
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