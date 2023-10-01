package com.person98.wifispy.util;


import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Location;

import java.util.List;

public class HologramManager {

    public Hologram createHologram(String name, Location location, List<String> lines, boolean saveToFile) {
        if (saveToFile) {
            return DHAPI.createHologram(name, location, true, lines);
        } else {
            return DHAPI.createHologram(name, location, lines);
        }
    }

    public Hologram getHologram(String hologramName) {
        return DHAPI.getHologram(hologramName);
    }

    public void moveHologram(String hologramName, Location newLocation) {
        DHAPI.moveHologram(hologramName, newLocation);
    }

    public void setHologramLines(Hologram hologram, List<String> lines) {
        DHAPI.setHologramLines(hologram, lines);
    }

    public void addHologramLine(Hologram hologram, String lineContent) {
        DHAPI.addHologramLine(hologram, lineContent);
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
    public void insertHologramLine(Hologram hologram, int lineIndex, String lineContent) {
        DHAPI.insertHologramLine(hologram, lineIndex, lineContent);
    }

    public void setHologramLine(Hologram hologram, int lineIndex, String newContent) {
        DHAPI.setHologramLine(hologram, lineIndex, newContent);
    }

    public void removeHologramLine(Hologram hologram, int lineIndex) {
        DHAPI.removeHologramLine(hologram, lineIndex);
    }

    // Page related operations
    public HologramPage addHologramPage(Hologram hologram, List<String> lines) {
        return DHAPI.addHologramPage(hologram, lines);
    }

    public HologramPage insertHologramPage(Hologram hologram, int pageIndex, List<String> lines) {
        return DHAPI.insertHologramPage(hologram, pageIndex, lines);
    }

    public void removeHologramPage(Hologram hologram, int pageIndex) {
        DHAPI.removeHologramPage(hologram, pageIndex);
    }

    // ... any other methods you'd want to encapsulate.
}
