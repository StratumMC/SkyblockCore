package com.sallamadm.skyblockcore.data.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class IslandGameruleData {

    private final SkyblockCore plugin;
    private final DatabaseConnection db;

    public IslandGameruleData(SkyblockCore plugin, DatabaseConnection db) {
        this.plugin = plugin;
        this.db = db;
    }

    private Connection connection() {
        return db.getConnection();
    }

    public void setGameruleAsync(String islandUuid, String node, boolean value) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "REPLACE INTO sb_island_gamerules (island_uuid, gamerule_node, value) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, node);
                ps.setBoolean(3, value);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Map<String, Boolean> loadGamerulesSync(String islandUuid) throws SQLException {
        Map<String, Boolean> result = new HashMap<>();
        Connection connection = connection();
        String sql = "SELECT gamerule_node, value FROM sb_island_gamerules WHERE island_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, islandUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("gamerule_node"), rs.getBoolean("value"));
                }
            }
        }
        return result;
    }
}