package com.person98.reinforcement.database;

public class ReinforcedBlockEntry {

    private int id;
    private String world;
    private int x, y, z;
    private int hp;
    private long creationTimestamp;
    private String ownerUuid;
    private String holoId;
    private String trusted;

    public ReinforcedBlockEntry(int id, String world, int x, int y, int z, int hp, long creationTimestamp, String ownerUuid, String holoId, String trusted) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.hp = hp;
        this.creationTimestamp = creationTimestamp;
        this.ownerUuid = ownerUuid;
        this.holoId = holoId;
        this.trusted = trusted;

    }
    public String getHoloId() {
        return holoId;
    }

    // Setter for holoId
    public void setHoloId(String holoId) {
        this.holoId = holoId;
    }
    // Getters
    public int getId() {
        return id;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getHp() {
        return hp;
    }
    public String getTrusted() {
        return trusted;
    }

    // Setter for trusted
    public void setTrusted(String trusted) {
        this.trusted = trusted;
    }
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }
}
