package com.sallamadm.skyblockcore.data.likes;

import com.sallamadm.skyblockcore.data.DataManager;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class IslandRatingData {

    private final DatabaseConnection db;

    public IslandRatingData(DatabaseConnection db) {
        this.db = db;
    }

    private Connection connection() {
        return db.getConnection();
    }

    public synchronized DataManager.RateResult addIslandRating(UUID voterUuid, String islandUuid, double rating) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return DataManager.RateResult.DATABASE_ERROR;

        try {
            String existsSql = "SELECT 1 FROM sb_island_ratings WHERE island_uuid = ? AND voter_uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(existsSql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, voterUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return DataManager.RateResult.ALREADY_RATED;
                }
            }

            String insertSql = "INSERT INTO sb_island_ratings (island_uuid, voter_uuid, rating) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, voterUuid.toString());
                ps.setDouble(3, rating);
                ps.executeUpdate();
            }
            return DataManager.RateResult.SUCCESS;
        } catch (SQLException e) {
            e.printStackTrace();
            return DataManager.RateResult.DATABASE_ERROR;
        }
    }

    public double getIslandRating(String islandUuid) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return 0D;

        String sql = "SELECT AVG(rating) FROM sb_island_ratings WHERE island_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, islandUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0D;
    }

    public Map<String, Double> getTopRatedIslands() {
        Connection connection = connection();
        if (connection == null) return Collections.emptyMap();

        Map<String, Double> ratings = new LinkedHashMap<>();
        String sql = "SELECT island_uuid, AVG(rating) AS rating FROM sb_island_ratings GROUP BY island_uuid ORDER BY rating DESC LIMIT 10";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ratings.put(rs.getString("island_uuid"), rs.getDouble("rating"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratings;
    }
}