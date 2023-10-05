package com.person98.reinforcement.database;

import com.person98.reinforcement.util.Heart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeartManager {

    private DatabaseManager dbManager;

    public HeartManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean createHeart(UUID owner, String world, int x, int y, int z, List<String> trustedUUIDs, String holoId, String name) {
        String sql = "INSERT INTO hearts(owner_uuid, world, x, y, z, trusted, holo_id, name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Convert list to comma-separated string
        String trustedString = String.join(",", trustedUUIDs);

        try (Connection connection = dbManager.connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, owner.toString());
            pstmt.setString(2, world);
            pstmt.setInt(3, x);
            pstmt.setInt(4, y);
            pstmt.setInt(5, z);
            pstmt.setString(6, trustedString);  // set the trusted string to the SQL statement
            pstmt.setString(7, holoId);  // set the holoId to the SQL statement
            pstmt.setString(8, name);


            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Heart getHeart(String world, int x, int y, int z) {
        String sql = "SELECT * FROM hearts WHERE world = ? AND x = ? AND y = ? AND z = ? LIMIT 1";

        try (Connection connection = dbManager.connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, world);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            pstmt.setInt(4, z);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Extract values from result set and return a new Heart object
                return new Heart(
                        UUID.fromString(rs.getString("owner_uuid")),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("hp"),
                        rs.getString("trusted"),
                        rs.getString("holo_id"), // Assuming holoId is a column in your hearts table
                        rs.getString("name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public boolean removeHeart(String world, int x, int y, int z) {
        String sql = "DELETE FROM hearts WHERE world = ? AND x = ? AND y = ? AND z = ?";

        try (Connection connection = dbManager.connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, world);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            pstmt.setInt(4, z);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deductHP(String world, int x, int y, int z, int damage) {
        String sql = "UPDATE hearts SET hp = hp - ? WHERE world = ? AND x = ? AND y = ? AND z = ?";

        try (Connection connection = dbManager.connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, damage);
            pstmt.setString(2, world);
            pstmt.setInt(3, x);
            pstmt.setInt(4, y);
            pstmt.setInt(5, z);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean getHeartByOwner(String ownerUUID) {
        String sql = "SELECT * FROM hearts WHERE owner_uuid = ? LIMIT 1";

        try (Connection connection = dbManager.connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, ownerUUID);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // You can extract other details if needed, e.g., location
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean updateHeartHP(String world, int x, int y, int z, int newHP) {
        String sql = "UPDATE hearts SET hp = ? WHERE world = ? AND x = ? AND y = ? AND z = ?";

        try (Connection connection = dbManager.connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, newHP);
            pstmt.setString(2, world);
            pstmt.setInt(3, x);
            pstmt.setInt(4, y);
            pstmt.setInt(5, z);


            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeHeart(UUID ownerUUID) {
        String sql = "DELETE FROM hearts WHERE owner_uuid = ?";

        try (Connection connection = dbManager.connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, ownerUUID.toString());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Heart> getAllHearts() {
        List<Heart> hearts = new ArrayList<>();
        String sql = "SELECT * FROM hearts";

        try (Connection connection = dbManager.connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                hearts.add(new Heart(
                        UUID.fromString(rs.getString("owner_uuid")),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getInt("hp"),
                        rs.getString("trusted"),
                        rs.getString("holo_id"),
                        rs.getString("name")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hearts;
    }


}