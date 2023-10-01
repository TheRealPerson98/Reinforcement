package com.person98.reinforcement.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class HologramManager {
    private final ConfigManager configManager;

    public HologramManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
    public Hologram createHologram(String name, Location location, List<String> lines, boolean saveToFile) {
        Hologram hologram;

        if (saveToFile) {
            hologram = DHAPI.createHologram(name, location, true, lines);
        } else {
            hologram = DHAPI.createHologram(name, location, lines);
        }

        // Assuming a setDisplayRange method exists in Hologram class
        int displayRange = configManager.getHologramDisplayRange();
        hologram.setDisplayRange(displayRange);

        return hologram;
    }
    public void setShowPlayer(Player player, Hologram hologram) {
        hologram.setShowPlayer(player);
    }

    public void removeShowPlayer(Player player, Hologram hologram) {
        hologram.removeShowPlayer(player);
    }

    public void setHidePlayer(Player player, Hologram hologram) {
        hologram.setHidePlayer(player);
    }

    public void removeHidePlayer(Player player, Hologram hologram) {
        hologram.removeHidePlayer(player);
    }
    public void showHologram(Player player, String hologramName) {
        Hologram hologram = DHAPI.getHologram(hologramName);
        if (hologram != null) {
            hologram.setShowPlayer(player);
        }
    }

    public void hideHologram(Player player, String hologramName) {
        Hologram hologram = DHAPI.getHologram(hologramName);
        if (hologram != null) {
            hologram.setHidePlayer(player);
        }
    }
    public void setHologramLines(Hologram hologram, List<String> lines) {
        DHAPI.setHologramLines(hologram, lines);
    }

    public void deleteHologram(String hologramName) {
        Hologram hologram = DHAPI.getHologram(hologramName);
        if (hologram != null) {
            hologram.delete();  // Assumes there's a delete method. Please ensure and modify if needed.
        }
    }

    public void updateHologram(String hologramName, List<String> newLines) {
        Hologram hologram = DHAPI.getHologram(hologramName);
        if (hologram != null) {
            setHologramLines(hologram, newLines);
        }
    }
}
