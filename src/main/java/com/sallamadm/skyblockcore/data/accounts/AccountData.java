package com.sallamadm.skyblockcore.data.accounts;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AccountData {

    private final SkyblockCore plugin;
    private final DatabaseConnection db;

    public AccountData(SkyblockCore plugin, DatabaseConnection db) {
        this.plugin = plugin;
        this.db = db;
    }

    private Connection connection() {
        return db.getConnection();
    }

    public void updateUuid(String username, UUID uuid) {
        Connection connection = connection();
        if (connection == null) return;
        String sql = "UPDATE sb_accounts SET uuid = ? WHERE username = ?";
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public long loadFlyTimeSync(UUID uuid) {
        Connection connection = connection();
        if (connection == null) return 0L;
        String sql = "SELECT fly_seconds FROM sb_accounts WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("fly_seconds");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public void saveFlyTimeAsync(UUID uuid, long remainingSeconds) {
        if (connection() == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveFlyTimeSync(uuid, remainingSeconds));
    }

    public void saveFlyTimeSync(UUID uuid, long remainingSeconds) {
        Connection connection = connection();
        if (connection == null) return;
        String sql = "UPDATE sb_accounts SET fly_seconds = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, remainingSeconds);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isRegistered(String username) {
        Connection connection = connection();
        if (connection == null) return false;
        String sql = "SELECT username FROM sb_accounts WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void registerAccount(UUID uuid, String username, String email, String password) {
        Connection connection = connection();
        if (connection == null) return;
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(12));
        String sql = "INSERT INTO sb_accounts (username, uuid, email, password, role, credit) VALUES (?, ?, ?, ?, 'player', 0)";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, uuid.toString());
                ps.setString(3, email);
                ps.setString(4, hashedPassword);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean checkPassword(String username, String inputPassword) {
        Connection connection = connection();
        if (connection == null) return false;
        String sql = "SELECT password FROM sb_accounts WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbHash = rs.getString("password");
                    if (dbHash != null && dbHash.startsWith("$2b$")) {
                        dbHash = dbHash.replace("$2b$", "$2a$");
                    }

                    return org.mindrot.jbcrypt.BCrypt.checkpw(inputPassword, dbHash);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}