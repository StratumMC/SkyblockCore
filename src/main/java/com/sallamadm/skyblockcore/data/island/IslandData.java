package com.sallamadm.skyblockcore.data.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IslandData {

    private final SkyblockCore plugin;
    private final DatabaseConnection db;
    private final IslandPermissionData permissionData;
    private final IslandGameruleData gameruleData;
    private final IslandWeatherData weatherData;
    private final IslandMemberData memberData;

    private boolean loadingData = false;

    public IslandData(SkyblockCore plugin,
                      DatabaseConnection db,
                      IslandPermissionData permissionData,
                      IslandGameruleData gameruleData,
                      IslandWeatherData weatherData,
                      IslandMemberData memberData) {
        this.plugin = plugin;
        this.db = db;
        this.permissionData = permissionData;
        this.gameruleData = gameruleData;
        this.weatherData = weatherData;
        this.memberData = memberData;
    }

    private Connection connection() {
        return db.getConnection();
    }

    public boolean isLoading() {
        return loadingData;
    }

    public Map<String, Double> getTopLeveledIslands() {
        Connection connection = connection();
        if (connection == null) return Collections.emptyMap();
        Map<String, Double> levels = new LinkedHashMap<>();
        String sql = "SELECT island_uuid, island_level FROM sb_islands ORDER BY island_level DESC LIMIT 10";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                levels.put(rs.getString("island_uuid"), rs.getDouble("island_level"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return levels;
    }

    public void saveData() {
        Connection connection = connection();
        if (loadingData || connection == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveDataSync);
    }

    public void saveIslandAsync(Island island) {
        Connection connection = connection();
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
        Connection connection = connection();
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
        Connection connection = connection();
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
        Connection connection = connection();
        String sql = "REPLACE INTO sb_islands (owner_uuid, island_uuid, grid_index, island_size, island_level, island_name, is_locked, biome, center_world, center_x, center_y, center_z, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, banned_players) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, island.getOwnerUUID().toString());
            ps.setString(2, island.getIslandUuid());
            ps.setInt(3, island.getGridIndex());
            ps.setInt(4, island.getIslandSize());
            ps.setDouble(5, island.getLevel());
            ps.setString(6, island.getIslandName());
            ps.setBoolean(7, island.isLocked());
            ps.setString(8, island.getBiome() != null ? island.getBiome().name() : Biome.PLAINS.name());

            if (island.getCenterLocation() != null) {
                ps.setString(9, island.getCenterLocation().getWorld().getName());
                ps.setDouble(10, island.getCenterLocation().getX());
                ps.setDouble(11, island.getCenterLocation().getY());
                ps.setDouble(12, island.getCenterLocation().getZ());
            } else {
                ps.setString(9, null); ps.setDouble(10, 0); ps.setDouble(11, 0); ps.setDouble(12, 0);
            }

            if (island.getSpawnLocation() != null) {
                ps.setDouble(13, island.getSpawnLocation().getX());
                ps.setDouble(14, island.getSpawnLocation().getY());
                ps.setDouble(15, island.getSpawnLocation().getZ());
                ps.setFloat(16, island.getSpawnLocation().getYaw());
                ps.setFloat(17, island.getSpawnLocation().getPitch());
            } else {
                ps.setDouble(13, 0); ps.setDouble(14, 0); ps.setDouble(15, 0); ps.setFloat(16, 0); ps.setFloat(17, 0);
            }

            ps.setString(18, island.getBannedPlayersAsString());
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
        Connection connection = connection();
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
                    island.setLevel(rs.getDouble("island_level"));
                    island.setIslandName(rs.getString("island_name"));
                    island.setLocked(rs.getBoolean("is_locked"));
                    island.loadBannedPlayersFromString(rs.getString("banned_players"));

                    String dbIslandUuid = rs.getString("island_uuid");
                    if (dbIslandUuid != null && !dbIslandUuid.isEmpty()) {
                        island.setIslandUuid(dbIslandUuid);
                    }

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

                    island.setPermissionCache(permissionData.loadPermissionsSync(island.getIslandUuid()));
                    island.setGameruleCache(gameruleData.loadGamerulesSync(island.getIslandUuid()));
                    String savedWeather = weatherData.loadWeatherOptionSync(island.getIslandUuid());
                    if (savedWeather != null) {
                        island.setWeatherOption(savedWeather);
                    }
                    memberData.loadMembersSync(island, island.getIslandUuid());
                    island.seedDefaultPermissionsIfEmpty();
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