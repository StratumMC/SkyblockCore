package com.sallamadm.skyblockcore.data.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class IslandMemberData {

    private final SkyblockCore plugin;
    private final DatabaseConnection db;

    public IslandMemberData(SkyblockCore plugin, DatabaseConnection db) {
        this.plugin = plugin;
        this.db = db;
    }

    private Connection connection() {
        return db.getConnection();
    }

    public void saveMemberAsync(String islandUuid, UUID playerUuid, int roleTier, UUID addedBy) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "REPLACE INTO sb_island_members (island_uuid, player_uuid, role_tier, added_by) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, playerUuid.toString());
                ps.setInt(3, roleTier);
                ps.setString(4, addedBy != null ? addedBy.toString() : null);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeMemberAsync(String islandUuid, UUID playerUuid) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "DELETE FROM sb_island_members WHERE island_uuid = ? AND player_uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, playerUuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadMembersSync(Island island, String islandUuid) throws SQLException {
        Connection connection = connection();
        String sql = "SELECT player_uuid, role_tier, added_by FROM sb_island_members WHERE island_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, islandUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
                    int tier = rs.getInt("role_tier");
                    String addedByStr = rs.getString("added_by");
                    UUID addedBy = addedByStr != null ? UUID.fromString(addedByStr) : null;

                    island.loadMemberFromDb(playerUuid, tier, addedBy);
                }
            }
        }
    }
}