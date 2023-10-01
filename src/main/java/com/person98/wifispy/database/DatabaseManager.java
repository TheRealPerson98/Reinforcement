package com.person98.wifispy.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static String URL = "jdbc:sqlite:plugins/wifispy/reinforce.db";

    public Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void initializeDatabase() {
        // Owners table
        String sqlOwners = "CREATE TABLE IF NOT EXISTS owners (" +
                "uuid TEXT PRIMARY KEY," +
                "ign TEXT NOT NULL);";

        // Trusted table
        String sqlTrusted = "CREATE TABLE IF NOT EXISTS trusted (" +
                "owner_uuid TEXT," +
                "trusted_uuid TEXT," +
                "trusted_ign TEXT," +
                "PRIMARY KEY (owner_uuid, trusted_uuid));";

        // Reinforced Blocks table
        String sqlBlocks = "CREATE TABLE IF NOT EXISTS reinforced_blocks (" +
                "id INTEGER PRIMARY KEY," +
                "world TEXT NOT NULL," +
                "x INTEGER NOT NULL," +
                "y INTEGER NOT NULL," +
                "z INTEGER NOT NULL," +
                "hp INTEGER NOT NULL," +
                "creation_timestamp LONG NOT NULL," +
                "owner_uuid TEXT NOT NULL," +
                "holo_id TEXT," +
                "trusted TEXT," +  // Add this line for trusted users
                "FOREIGN KEY (owner_uuid) REFERENCES owners(uuid));";


        try (Connection connection = connect();
             PreparedStatement pstmtOwners = connection.prepareStatement(sqlOwners);
             PreparedStatement pstmtTrusted = connection.prepareStatement(sqlTrusted);
             PreparedStatement pstmtBlocks = connection.prepareStatement(sqlBlocks)) {

            pstmtOwners.execute();
            pstmtTrusted.execute();
            pstmtBlocks.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addReinforcedBlock(ReinforcedBlockEntry entry) {
        String sql = "INSERT INTO reinforced_blocks(world, x, y, z, hp, creation_timestamp, owner_uuid, holo_id, trusted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, entry.getWorld());
            pstmt.setInt(2, entry.getX());
            pstmt.setInt(3, entry.getY());
            pstmt.setInt(4, entry.getZ());
            pstmt.setInt(5, entry.getHp());
            pstmt.setLong(6, entry.getCreationTimestamp());
            pstmt.setString(7, entry.getOwnerUuid());
            pstmt.setString(8, entry.getHoloId());
            pstmt.setString(9, entry.getTrusted());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve a reinforced block entry from the database based on block coordinates and world.
    public ReinforcedBlockEntry getReinforcedBlock(String world, int x, int y, int z) {
        String sql = "SELECT * FROM reinforced_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?";

        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, world);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            pstmt.setInt(4, z);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new ReinforcedBlockEntry(
                        rs.getInt("id"),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("hp"),
                        rs.getLong("creation_timestamp"),
                        rs.getString("owner_uuid"),
                        rs.getString("holo_id"),
                        rs.getString("trusted") // Retrieve the trusted field
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Decrement the HP of a reinforced block by 1 in the database.
    public void decrementBlockHP(ReinforcedBlockEntry entry) {
        String sql = "UPDATE reinforced_blocks SET hp = hp - 1 WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, entry.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a reinforced block entry from the database.
    public void removeReinforcedBlock(ReinforcedBlockEntry entry) {
        String sql = "DELETE FROM reinforced_blocks WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, entry.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<ReinforcedBlockEntry> getAllReinforcedBlocksByOwner(String ownerUuid) {
        List<ReinforcedBlockEntry> entries = new ArrayList<>();

        String sql = "SELECT * FROM reinforced_blocks WHERE owner_uuid = ?";

        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, ownerUuid);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                entries.add(new ReinforcedBlockEntry(
                        rs.getInt("id"),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("hp"),
                        rs.getLong("creation_timestamp"),
                        rs.getString("owner_uuid"),
                        rs.getString("holo_id"),
                        rs.getString("trusted")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entries;
    }

    // Method to update a reinforced block entry in the database.
    public void updateReinforcedBlock(ReinforcedBlockEntry entry) {
        String sql = "UPDATE reinforced_blocks SET world = ?, x = ?, y = ?, z = ?, hp = ?, creation_timestamp = ?, owner_uuid = ?, holo_id = ?, trusted = ? WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, entry.getWorld());
            pstmt.setInt(2, entry.getX());
            pstmt.setInt(3, entry.getY());
            pstmt.setInt(4, entry.getZ());
            pstmt.setInt(5, entry.getHp());
            pstmt.setLong(6, entry.getCreationTimestamp());
            pstmt.setString(7, entry.getOwnerUuid());
            pstmt.setString(8, entry.getHoloId());
            pstmt.setString(9, entry.getTrusted());
            pstmt.setInt(10, entry.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<String> getAllHoloIds() {
        List<String> holoNames = new ArrayList<>();

        String sql = "SELECT holo_id FROM reinforced_blocks";

        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                holoNames.add(rs.getString("holo_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return holoNames;
    }
}