package com.person98.reinforcement.util;

import com.person98.reinforcement.Reinforcement;
import com.person98.reinforcement.database.HeartManager;
import com.person98.reinforcement.util.Heart;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class HeartCoordinatesUtil {
    private final Reinforcement plugin;
    private final HeartManager heartManager;
    private final ConfigManager configManager;

    private final Map<Location, Set<UUID>> playersInProximity = new HashMap<>();
    private Map<UUID, Long> lastNotificationTimes;

    public HeartCoordinatesUtil(Reinforcement plugin, HeartManager heartManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.heartManager = heartManager;
        this.configManager = configManager;
        loadHeartCoordinates();
    }

    private List<Location> loadHeartCoordinates() {
        List<Location> locations = new ArrayList<>();
        List<Heart> hearts = heartManager.getAllHearts();
        for (Heart heart : hearts) {
            Location loc = new Location(plugin.getServer().getWorld(heart.getWorld()), heart.getX(), heart.getY(), heart.getZ());
            locations.add(loc);
        }
        return locations;
    }
    // Entry/Exit Monitor
    public void monitorEntryExitProximity() {
        int notifyDistance = configManager.getNotifyDistance();

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            List<Location> heartLocations = loadHeartCoordinates();

            for (Location heartLoc : heartLocations) {
                Heart heart = heartManager.getHeart(heartLoc.getWorld().getName(), heartLoc.getBlockX(), heartLoc.getBlockY(), heartLoc.getBlockZ());
                if (heart == null) continue;

                Set<UUID> currentProximity = playersInProximity.getOrDefault(heartLoc, new HashSet<>());
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    boolean isInProximity = heartLoc.getWorld().equals(player.getWorld()) && heartLoc.distance(player.getLocation()) <= notifyDistance;
                    boolean isOwnerOrTrusted = heart.getOwnerUUID().equals(player.getUniqueId()) || Arrays.asList(heart.getTrusted().split(",")).contains(player.getUniqueId().toString());

                    if (isInProximity && !isOwnerOrTrusted) {
                        if (!currentProximity.contains(player.getUniqueId())) {
                            // Player just entered the proximity
                            currentProximity.add(player.getUniqueId());
                            player.sendMessage(replacePlaceholders(configManager.getMessage("enterProximity"), "%heartname%", heart.getName()));
                        }
                    } else if (!isInProximity && currentProximity.contains(player.getUniqueId())) {
                        // Player just exited the proximity
                        currentProximity.remove(player.getUniqueId());
                        player.sendMessage(replacePlaceholders(configManager.getMessage("exitProximity"), "%heartname%", heart.getName()));
                    }
                }
                playersInProximity.put(heartLoc, currentProximity);
            }
        }, 0L, 20L * 5);  // This checks every 5 seconds (20 ticks = 1 second)
    }

    // Location Update Monitor
    public void monitorLocationUpdate() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Location heartLoc : playersInProximity.keySet()) {
                Heart heart = heartManager.getHeart(heartLoc.getWorld().getName(), heartLoc.getBlockX(), heartLoc.getBlockY(), heartLoc.getBlockZ());
                if (heart == null) continue;

                for (UUID uuid : playersInProximity.get(heartLoc)) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player != null) {
                        notifyOwnerAndTrusted(heart, player);
                    }
                }
            }
        }, 0L, 20L * 30);  // This checks every 30 seconds (20 ticks = 1 second)
    }


    private void notifyOwnerAndTrusted(Heart heart, Player intruder) {
        Player owner = plugin.getServer().getPlayer(heart.getOwnerUUID());
        Location intruderLoc = intruder.getLocation();
        if (owner != null) {
            owner.sendMessage(replacePlaceholders(configManager.getMessage("intruderNotification"),
                    "%intruder%", intruder.getName(),
                    "%coordx%", Integer.toString((int) intruderLoc.getX()),
                    "%coordy%", Integer.toString((int) intruderLoc.getY()),
                    "%coordz%", Integer.toString((int) intruderLoc.getZ()),
                    "%heartname%", heart.getName()));
        }
        for (String trustedUUID : heart.getTrusted().split(",")) {
            if (isValidUUID(trustedUUID)) {
                Player trustedPlayer = plugin.getServer().getPlayer(UUID.fromString(trustedUUID));
                if (trustedPlayer != null) {
                    trustedPlayer.sendMessage(replacePlaceholders(configManager.getMessage("trustedIntruderNotification"),
                            "%intruder%", intruder.getName(),
                            "%coordx%", Integer.toString((int) intruderLoc.getX()),
                            "%coordy%", Integer.toString((int) intruderLoc.getY()),
                            "%coordz%", Integer.toString((int) intruderLoc.getZ()),
                            "%heartname%", heart.getName()));
                }
            } else {
                plugin.getLogger().warning("Invalid UUID found: " + trustedUUID);
            }
        }
    }


        private boolean isValidUUID(String uuid) {
            try {
                UUID.fromString(uuid);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

    private String replacePlaceholders(String message, String... placeholders) {
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
