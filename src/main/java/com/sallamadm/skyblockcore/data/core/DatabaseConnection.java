package com.sallamadm.skyblockcore.data.core;

import com.sallamadm.skyblockcore.SkyblockCore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private final SkyblockCore plugin;
    private Connection connection;

    public DatabaseConnection(SkyblockCore plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    public Connection getConnection() {
        return connection;
    }

    private void connect() {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        String database = plugin.getConfig().getString("mysql.database", "skyblock");
        String username = plugin.getConfig().getString("mysql.username", "root");
        String password = plugin.getConfig().getString("mysql.password", "");

        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=true", username, password
            );
            plugin.getLogger().severe("MySQL baglandi.");
        } catch (Exception e) {
            plugin.getLogger().severe("MySQL baglanamadi " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        if (connection == null) return;
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS sb_system (" +
                    "id INT PRIMARY KEY, " +
                    "next_grid_index INT NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_available_grids (" +
                    "grid_index INT PRIMARY KEY)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_islands (" +
                    "owner_uuid VARCHAR(36) PRIMARY KEY, " +
                    "island_uuid VARCHAR(36) UNIQUE, " +
                    "grid_index INT NOT NULL, " +
                    "island_size INT NOT NULL, " +
                    "island_level DOUBLE NOT NULL, " +
                    "island_name VARCHAR(64), " +
                    "is_locked BOOLEAN, " +
                    "biome VARCHAR(32), " +
                    "center_world VARCHAR(64), " +
                    "center_x DOUBLE, " +
                    "center_y DOUBLE, " +
                    "center_z DOUBLE, " +
                    "spawn_x DOUBLE, " +
                    "spawn_y DOUBLE, " +
                    "spawn_z DOUBLE, " +
                    "spawn_yaw FLOAT, " +
                    "spawn_pitch FLOAT, " +
                    "banned_players TEXT)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_warps (" +
                    "owner_uuid VARCHAR(36), " +
                    "name VARCHAR(32), " +
                    "icon VARCHAR(32), " +
                    "is_visible BOOLEAN, " +
                    "x DOUBLE, " +
                    "y DOUBLE, " +
                    "z DOUBLE, " +
                    "yaw FLOAT, " +
                    "pitch FLOAT, " +
                    "PRIMARY KEY (owner_uuid, name), " +
                    "FOREIGN KEY (owner_uuid) REFERENCES sb_islands(owner_uuid) ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_island_permissions (" +
                    "island_uuid VARCHAR(36) NOT NULL, " +
                    "role_tier INT NOT NULL, " +
                    "permission_node VARCHAR(64) NOT NULL, " +
                    "PRIMARY KEY (island_uuid, role_tier, permission_node), " +
                    "FOREIGN KEY (island_uuid) REFERENCES sb_islands(island_uuid) ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_island_members (" +
                    "island_uuid VARCHAR(36) NOT NULL, " +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "role_tier INT NOT NULL, " +
                    "added_by VARCHAR(36) DEFAULT NULL, " +
                    "PRIMARY KEY (island_uuid, player_uuid), " +
                    "FOREIGN KEY (island_uuid) REFERENCES sb_islands(island_uuid) ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_island_gamerules (" +
                    "island_uuid VARCHAR(36) NOT NULL, " +
                    "gamerule_node VARCHAR(64) NOT NULL, " +
                    "value BOOLEAN NOT NULL, " +
                    "PRIMARY KEY (island_uuid, gamerule_node), " +
                    "FOREIGN KEY (island_uuid) REFERENCES sb_islands(island_uuid) ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_island_weather (" +
                    "island_uuid VARCHAR(36) PRIMARY KEY, " +
                    "weather_option VARCHAR(32) NOT NULL, " +
                    "FOREIGN KEY (island_uuid) REFERENCES sb_islands(island_uuid) ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_island_likes (" +
                    "island_uuid VARCHAR(36) NOT NULL, " +
                    "voter_uuid VARCHAR(36) NOT NULL, " +
                    "week_key VARCHAR(10) NOT NULL, " +
                    "month_key VARCHAR(7) NOT NULL, " +
                    "liked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (island_uuid, voter_uuid, week_key), " +
                    "FOREIGN KEY (island_uuid) REFERENCES sb_islands(island_uuid) ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_island_ratings (" +
                    "island_uuid VARCHAR(36) NOT NULL, " +
                    "voter_uuid VARCHAR(36) NOT NULL, " +
                    "rating DECIMAL(4,2) NOT NULL, " +
                    "rated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (island_uuid, voter_uuid), " +
                    "FOREIGN KEY (island_uuid) REFERENCES sb_islands(island_uuid) ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS sb_accounts (" +
                    "username VARCHAR(32) PRIMARY KEY, " +
                    "uuid VARCHAR(36) DEFAULT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(32) DEFAULT 'player', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "credit INT DEFAULT 0, " +
                    "fly_seconds BIGINT NOT NULL DEFAULT 0)");


            statement.execute("ALTER TABLE sb_islands MODIFY island_level DOUBLE NOT NULL");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}