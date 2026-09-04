package com.sallamadm.skyblockcore.data.link;

import com.sallamadm.skyblockcore.data.core.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

public class LinkCodeData {

    private final DatabaseConnection db;

    public LinkCodeData(DatabaseConnection db) {
        this.db = db;
    }

    private Connection connection() {
        return db.getConnection();
    }

    private String normalizeLinkCode(String inputCode) {
        if (inputCode == null) return null;

        String normalizedCode = inputCode.trim();
        if (normalizedCode.isEmpty()) return null;

        normalizedCode = normalizedCode.toUpperCase(Locale.ROOT);
        if (normalizedCode.startsWith("STRATUM-")) {
            normalizedCode = normalizedCode.substring("STRATUM-".length());
        }

        if (normalizedCode.length() != 8) return null;
        return "STRATUM-" + normalizedCode;
    }

    public boolean consumeMinecraftLinkCode(UUID playerUuid, String username, String inputCode) {
        Connection connection = connection();
        if (connection == null || playerUuid == null || username == null) return false;

        String normalizedCode = normalizeLinkCode(inputCode);
        if (normalizedCode == null) return false;

        String selectSql = "SELECT stratum_id FROM stratum_minecraft_link_codes WHERE code = ? LIMIT 1";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, normalizedCode);
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String stratumId = rs.getString("stratum_id");

                String insertSql = "INSERT INTO stratum_identities (stratum_id, provider, provider_user_id, provider_username, created_at) VALUES (?, ?, ?, ?, CURRENT_DATE)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    insertStatement.setString(1, stratumId);
                    insertStatement.setString(2, "minecraft");
                    insertStatement.setString(3, playerUuid.toString());
                    insertStatement.setString(4, username);
                    insertStatement.executeUpdate();
                }

                String deleteSql = "DELETE FROM stratum_minecraft_link_codes WHERE code = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setString(1, normalizedCode);
                    deleteStatement.executeUpdate();
                }

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkIfDiscordLinked(UUID playerUuid) {
        Connection connection = connection();
        if (connection == null || playerUuid == null) return false;

        String selectSql = "SELECT mc_id FROM dc_identities WHERE mc_id = ? LIMIT 1";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, playerUuid.toString());
            try (ResultSet rs = selectStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkIfWebLinked(UUID playerUuid) {
        Connection connection = connection();
        if (connection == null || playerUuid == null) return false;

        String selectSql = "SELECT provider_user_id FROM stratum_identities WHERE provider_user_id = ? LIMIT 1";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, playerUuid.toString());
            try (ResultSet rs = selectStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean consumeDiscordLinkCode(UUID playerUuid, String username, String inputCode) {
        Connection connection = connection();
        if (connection == null || playerUuid == null || username == null) return false;

        String normalizedCode = normalizeLinkCode(inputCode);
        if (normalizedCode == null) return false;

        String selectSql = "SELECT dc_id FROM dc_link_codes WHERE code = ? LIMIT 1";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, normalizedCode);
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String discordID = rs.getString("dc_id");

                String insertSql = "INSERT INTO dc_identities (dc_id, mc_id, mc_username, created_at) VALUES (?, ?, ?, CURRENT_DATE)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    insertStatement.setString(1, discordID);
                    insertStatement.setString(2, playerUuid.toString());
                    insertStatement.setString(3, username);
                    insertStatement.executeUpdate();
                }

                String deleteSql = "DELETE FROM dc_link_codes WHERE code = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setString(1, normalizedCode);
                    deleteStatement.executeUpdate();
                }

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean consumeWebLinkCode(UUID playerUuid, String username, String inputCode) {
        Connection connection = connection();
        if (connection == null || playerUuid == null || username == null) return false;

        String normalizedCode = normalizeLinkCode(inputCode);
        if (normalizedCode == null) return false;

        String selectSql = "SELECT stratum_id FROM stratum_minecraft_link_codes WHERE code = ? LIMIT 1";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setString(1, normalizedCode);
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String stratumId = rs.getString("stratum_id");

                String insertSql = "INSERT INTO stratum_identities (stratum_id, provider, provider_user_id, provider_username, created_at) VALUES (?, ?, ?, ?, CURRENT_DATE)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    insertStatement.setString(1, stratumId);
                    insertStatement.setString(2, "minecraft");
                    insertStatement.setString(3, playerUuid.toString());
                    insertStatement.setString(4, username);
                    insertStatement.executeUpdate();
                }

                String deleteSql = "DELETE FROM stratum_minecraft_link_codes WHERE code = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setString(1, normalizedCode);
                    deleteStatement.executeUpdate();
                }

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}