package com.sallamadm.skyblockcore.data.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IslandWeatherData {

    private final SkyblockCore plugin;
    private final DatabaseConnection db;

    public IslandWeatherData(SkyblockCore plugin, DatabaseConnection db) {
        this.plugin = plugin;
        this.db = db;
    }

    private Connection connection() {
        return db.getConnection();
    }

    public void setWeatherOptionAsync(String islandUuid, String node) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "REPLACE INTO sb_island_weather (island_uuid, weather_option) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, node);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public String loadWeatherOptionSync(String islandUuid) throws SQLException {
        Connection connection = connection();
        String sql = "SELECT weather_option FROM sb_island_weather WHERE island_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, islandUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("weather_option");
            }
        }
        return null;
    }
}