package com.sallamadm.skyblockcore.data.likes;

import com.sallamadm.skyblockcore.data.DataManager;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class IslandLikeData {

    private final DatabaseConnection db;

    public IslandLikeData(DatabaseConnection db) {
        this.db = db;
    }

    private Connection connection() {
        return db.getConnection();
    }

    public synchronized DataManager.LikeResult addIslandLike(UUID voterUuid, String islandUuid) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return DataManager.LikeResult.DATABASE_ERROR;

        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.ISO;
        String weekKey = today.get(weekFields.weekBasedYear()) + "-" + String.format("%02d", today.get(weekFields.weekOfWeekBasedYear()));
        String monthKey = YearMonth.from(today).toString();

        try {
            String weeklySql = "SELECT 1 FROM sb_island_likes WHERE island_uuid = ? AND voter_uuid = ? AND week_key = ?";
            try (PreparedStatement ps = connection.prepareStatement(weeklySql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, voterUuid.toString());
                ps.setString(3, weekKey);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return DataManager.LikeResult.ALREADY_LIKED_THIS_WEEK;
                }
            }

            String monthlySql = "SELECT COUNT(*) FROM sb_island_likes WHERE island_uuid = ? AND voter_uuid = ? AND month_key = ?";
            try (PreparedStatement ps = connection.prepareStatement(monthlySql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, voterUuid.toString());
                ps.setString(3, monthKey);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) >= 4) return DataManager.LikeResult.MONTHLY_LIMIT_REACHED;
                }
            }

            String insertSql = "INSERT INTO sb_island_likes (island_uuid, voter_uuid, week_key, month_key) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setString(1, islandUuid);
                ps.setString(2, voterUuid.toString());
                ps.setString(3, weekKey);
                ps.setString(4, monthKey);
                ps.executeUpdate();
            }
            return DataManager.LikeResult.SUCCESS;
        } catch (SQLException e) {
            e.printStackTrace();
            return DataManager.LikeResult.DATABASE_ERROR;
        }
    }

    public int getWeeklyLikeCount(String islandUuid) {
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.ISO;
        String weekKey = today.get(weekFields.weekBasedYear()) + "-" + String.format("%02d", today.get(weekFields.weekOfWeekBasedYear()));
        return getLikeCount(islandUuid, "week_key", weekKey);
    }

    public int getMonthlyLikeCount(String islandUuid) {
        return getLikeCount(islandUuid, "month_key", YearMonth.now().toString());
    }

    public int getTotalLikeCount(String islandUuid) {
        return getLikeCount(islandUuid, null, null);
    }

    private int getLikeCount(String islandUuid, String periodColumn, String periodKey) {
        Connection connection = connection();
        if (connection == null || islandUuid == null) return 0;

        String sql;
        if (periodColumn != null) {
            sql = "SELECT COUNT(DISTINCT voter_uuid) FROM sb_island_likes WHERE island_uuid = ? AND " + periodColumn + " = ?";
        } else {
            sql = "SELECT COUNT(DISTINCT voter_uuid) FROM sb_island_likes WHERE island_uuid = ?";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, islandUuid);
            if (periodColumn != null) ps.setString(2, periodKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Integer> getTopLikedIslands(String period) {
        Connection connection = connection();
        if (connection == null) return Collections.emptyMap();

        String periodColumn = null;
        String periodKey = null;
        if (period.equals("hafta")) {
            LocalDate today = LocalDate.now();
            WeekFields weekFields = WeekFields.ISO;
            periodColumn = "week_key";
            periodKey = today.get(weekFields.weekBasedYear()) + "-" + String.format("%02d", today.get(weekFields.weekOfWeekBasedYear()));
        } else if (period.equals("ay")) {
            periodColumn = "month_key";
            periodKey = YearMonth.now().toString();
        }

        Map<String, Integer> likes = new LinkedHashMap<>();
        String sql;
        if (periodColumn != null) {
            sql = "SELECT island_uuid, COUNT(DISTINCT voter_uuid) AS likes FROM sb_island_likes WHERE " + periodColumn + " = ? GROUP BY island_uuid ORDER BY likes DESC LIMIT 10";
        } else {
            sql = "SELECT island_uuid, COUNT(DISTINCT voter_uuid) AS likes FROM sb_island_likes GROUP BY island_uuid ORDER BY likes DESC LIMIT 10";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (periodColumn != null) ps.setString(1, periodKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    likes.put(rs.getString("island_uuid"), rs.getInt("likes"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return likes;
    }
}