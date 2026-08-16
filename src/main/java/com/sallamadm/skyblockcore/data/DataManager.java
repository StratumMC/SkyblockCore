package com.sallamadm.skyblockcore.data;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final SkyblockCore plugin;
    private Connection connection;
    private boolean loadingData = false;

    public DataManager(SkyblockCore plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    public boolean isLoading() {
        return loadingData;
    }

    private void connect() {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        String database = plugin.getConfig().getString("mysql.database", "skyblock");
        String username = plugin.getConfig().getString("mysql.username", "root");
        String password = plugin.getConfig().getString("mysql.password", "");

        try {
            if(connection != null && !connection.isClosed()){
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
                    "grid_index INT NOT NULL, " +
                    "island_size INT NOT NULL, " +
                    "island_level INT NOT NULL, " +
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

            statement.execute("CREATE TABLE IF NOT EXISTS sb_accounts (" +
                    "username VARCHAR(32) PRIMARY KEY, " +
                    "uuid VARCHAR(36) DEFAULT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(32) DEFAULT 'player', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "credit INT DEFAULT 0)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateUuid(String username, UUID uuid) {
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

    public boolean isRegistered(String username) {
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

    public void saveData() {
        if (loadingData || connection == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveDataSync);
    }

    public void saveIslandAsync(Island island) {
        if (loadingData || connection == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                saveIslandSync(island);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveDataSync() {
        if (loadingData || connection == null) return;
        try {
            try (PreparedStatement ps = connection.prepareStatement("REPLACE INTO sb_system (id, next_grid_index) VALUES (1, ?)")) {
                ps.setInt(1, plugin.getIslandManager().getNextGridIndex());
                ps.executeUpdate();
            }

            try (Statement st = connection.createStatement()) {
                st.execute("TRUNCATE TABLE sb_available_grids");
            }
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO sb_available_grids (grid_index) VALUES (?)")) {
                for (int index : plugin.getIslandManager().getAvailableGridIndices()) {
                    ps.setInt(1, index);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            for (Island island : plugin.getIslandManager().getAllIslands().values()) {
                saveIslandSync(island);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteIsland(UUID ownerUUID) {
        if (connection == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM sb_islands WHERE owner_uuid = ?")) {
                ps.setString(1, ownerUUID.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void saveIslandSync(Island island) throws SQLException {
        String sql = "REPLACE INTO sb_islands (owner_uuid, grid_index, island_size, island_level, island_name, is_locked, biome, center_world, center_x, center_y, center_z, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, banned_players) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, island.getOwnerUUID().toString());
            ps.setInt(2, island.getGridIndex());
            ps.setInt(3, island.getIslandSize());
            ps.setInt(4, island.getLevel());
            ps.setString(5, island.getIslandName());
            ps.setBoolean(6, island.isLocked());
            ps.setString(7, island.getBiome() != null ? island.getBiome().name() : Biome.PLAINS.name());

            if (island.getCenterLocation() != null) {
                ps.setString(8, island.getCenterLocation().getWorld().getName());
                ps.setDouble(9, island.getCenterLocation().getX());
                ps.setDouble(10, island.getCenterLocation().getY());
                ps.setDouble(11, island.getCenterLocation().getZ());
            } else {
                ps.setString(8, null); ps.setDouble(9, 0); ps.setDouble(10, 0); ps.setDouble(11, 0);
            }

            if (island.getSpawnLocation() != null) {
                ps.setDouble(12, island.getSpawnLocation().getX());
                ps.setDouble(13, island.getSpawnLocation().getY());
                ps.setDouble(14, island.getSpawnLocation().getZ());
                ps.setFloat(15, island.getSpawnLocation().getYaw());
                ps.setFloat(16, island.getSpawnLocation().getPitch());
            } else {
                ps.setDouble(12, 0); ps.setDouble(13, 0); ps.setDouble(14, 0); ps.setFloat(15, 0); ps.setFloat(16, 0);
            }

            ps.setString(17, island.getBannedPlayersAsString());
            ps.executeUpdate();
        }

        try (PreparedStatement deleteWarps = connection.prepareStatement("DELETE FROM sb_warps WHERE owner_uuid = ?")) {
            deleteWarps.setString(1, island.getOwnerUUID().toString());
            deleteWarps.executeUpdate();
        }

        if (!island.getWarps().isEmpty()) {
            String warpSql = "INSERT INTO sb_warps (owner_uuid, name, icon, is_visible, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(warpSql)) {
                for (Warp warp : island.getWarps().values()) {
                    ps.setString(1, island.getOwnerUUID().toString());
                    ps.setString(2, warp.getName());
                    ps.setString(3, warp.getIcon().name());
                    ps.setBoolean(4, warp.isVisible());
                    if (warp.getLocation() != null) {
                        ps.setDouble(5, warp.getLocation().getX());
                        ps.setDouble(6, warp.getLocation().getY());
                        ps.setDouble(7, warp.getLocation().getZ());
                        ps.setFloat(8, warp.getLocation().getYaw());
                        ps.setFloat(9, warp.getLocation().getPitch());
                    } else {
                        ps.setDouble(5, 0); ps.setDouble(6, 0); ps.setDouble(7, 0); ps.setFloat(8, 0); ps.setFloat(9, 0);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    public void loadData() {
        if (connection == null) return;
        loadingData = true;

        try {
            try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT next_grid_index FROM sb_system WHERE id = 1")) {
                if (rs.next()) {
                    plugin.getIslandManager().setNextGridIndex(rs.getInt("next_grid_index"));
                }
            }

            try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT grid_index FROM sb_available_grids")) {
                List<Integer> grids = new ArrayList<>();
                while (rs.next()) {
                    grids.add(rs.getInt("grid_index"));
                }
                plugin.getIslandManager().getAvailableGridIndices().clear();
                plugin.getIslandManager().getAvailableGridIndices().addAll(grids);
            }

            try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM sb_islands")) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("owner_uuid"));
                    Island island = plugin.getIslandManager().createIsland(uuid);

                    island.setGridIndex(rs.getInt("grid_index"));
                    island.setIslandSize(rs.getInt("island_size"));
                    island.setLevel(rs.getInt("island_level"));
                    island.setIslandName(rs.getString("island_name"));
                    island.setLocked(rs.getBoolean("is_locked"));
                    island.loadBannedPlayersFromString(rs.getString("banned_players"));

                    try {
                        island.setBiome(Biome.valueOf(rs.getString("biome")));
                    } catch (Exception ignored) {}

                    String worldName = rs.getString("center_world");
                    if (worldName != null) {
                        World world = Bukkit.getWorld(worldName);
                        if (world != null) {
                            island.setCenterLocation(new Location(world, rs.getDouble("center_x"), rs.getDouble("center_y"), rs.getDouble("center_z")));
                            island.setSpawnLocation(new Location(world, rs.getDouble("spawn_x"), rs.getDouble("spawn_y"), rs.getDouble("spawn_z"), rs.getFloat("spawn_yaw"), rs.getFloat("spawn_pitch")));
                        }
                    }
                }
            }

            try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM sb_warps")) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("owner_uuid"));
                    Island island = plugin.getIslandManager().getIsland(uuid);
                    if (island != null && island.getCenterLocation() != null) {
                        World world = island.getCenterLocation().getWorld();
                        String name = rs.getString("name");
                        Material icon = Material.OAK_SIGN;
                        try {
                            icon = Material.valueOf(rs.getString("icon"));
                        } catch (Exception ignored) {}
                        boolean visible = rs.getBoolean("is_visible");
                        Location loc = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));

                        Warp warp = new Warp(name, loc, icon, visible);
                        island.addWarp(warp);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            loadingData = false;
        }
    }
}