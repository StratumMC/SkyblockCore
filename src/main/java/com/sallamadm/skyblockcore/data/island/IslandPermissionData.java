package com.sallamadm.skyblockcore.data.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IslandPermissionData {

    private final SkyblockCore plugin;
    private final DatabaseConnection db;

    public IslandPermissionData(SkyblockCore plugin, DatabaseConnection db) {
        this.plugin = plugin;
        this.db = db;
    }

    private Connection connection() {
        return db.getConnection();
    }

    public void setPermissionAsync(String islandUuid, int roleTier, String node, boolean granted) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = granted
                    ? "INSERT IGNORE INTO sb_island_permissions (island_uuid, role_tier, permission_node) VALUES (?, ?, ?)"
                    : "DELETE FROM sb_island_permissions WHERE island_uuid = ? AND role_tier = ? AND permission_node = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, islandUuid);
                ps.setInt(2, roleTier);
                ps.setString(3, node);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Map<Integer, Set<String>> loadPermissionsSync(String islandUuid) throws SQLException {
        Map<Integer, Set<String>> result = new HashMap<>();
        Connection connection = connection();
        String sql = "SELECT role_tier, permission_node FROM sb_island_permissions WHERE island_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, islandUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int tier = rs.getInt("role_tier");
                    String node = rs.getString("permission_node");
                    result.computeIfAbsent(tier, k -> new HashSet<>()).add(node);
                }
            }
        }
        return result;
    }
}