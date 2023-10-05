package com.person98.reinforcement.util;

import java.util.UUID;

public class Heart {
    private UUID ownerUUID;
    private String world;
    private int x;
    private int y;
    private int z;
    private int hp;
    private String trusted;
    private String holoId;
    private String name;


    // Constructor
    public Heart(UUID ownerUUID, String world, int x, int y, int z, int hp, String trusted, String holoId, String name) {
        this.ownerUUID = ownerUUID;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.hp = hp;
        this.trusted = trusted;
        this.holoId = holoId;
        this.name = name;
    }

    // Getters and Setters

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getName() {
        return name;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public String getTrusted() {
        return trusted;
    }

    public void setTrusted(String trusted) {
        this.trusted = trusted;
    }

    public String getHoloId() {
        return holoId;
    }

    public void setHoloId(String holoId) {
        this.holoId = holoId;
    }
}
