package com.sallamadm.skyblockcore.data;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final SkyblockCore plugin;
    private File file;
    private FileConfiguration config;
    private boolean loadingData = false;

    public DataManager(SkyblockCore plugin) {
        this.plugin = plugin;
        createFile();
    }

    public boolean isLoading() {
        return loadingData;
    }

    private void createFile() {
        file = new File(plugin.getDataFolder(), "islandData.yml");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveData() {
        if (loadingData) return;

        config.set("islands", null);
        config.set("nextGridIndex", plugin.getIslandManager().getNextGridIndex());
        config.set("availableGridIndices", plugin.getIslandManager().getAvailableGridIndices());

        for (Island island : plugin.getIslandManager().getAllIslands().values()) {
            String path = "islands." + island.getOwnerUUID().toString();
            config.set(path + ".gridIndex", island.getGridIndex());
            config.set(path + ".size", island.getIslandSize());
            config.set(path + ".level", island.getLevel());
            config.set(path + ".name", island.getIslandName());
            config.set(path + ".locked", island.isLocked());

            if (island.getBiome() != null) {
                config.set(path + ".biome", island.getBiome().name());
            }

            if (island.getCenterLocation() != null) {
                config.set(path + ".world", island.getCenterLocation().getWorld().getName());
                config.set(path + ".x", island.getCenterLocation().getX());
                config.set(path + ".y", island.getCenterLocation().getY());
                config.set(path + ".z", island.getCenterLocation().getZ());
            }

            if (island.getSpawnLocation() != null) {
                config.set(path + ".spawn.x", island.getSpawnLocation().getX());
                config.set(path + ".spawn.y", island.getSpawnLocation().getY());
                config.set(path + ".spawn.z", island.getSpawnLocation().getZ());
                config.set(path + ".spawn.yaw", island.getSpawnLocation().getYaw());
                config.set(path + ".spawn.pitch", island.getSpawnLocation().getPitch());
            }

            if (!island.getWarps().isEmpty()) {
                for (Warp warp : island.getWarps().values()) {
                    String warpPath = path + ".warps." + warp.getName().toLowerCase();
                    config.set(warpPath + ".name", warp.getName());
                    config.set(warpPath + ".icon", warp.getIcon().name());
                    config.set(warpPath + ".visible", warp.isVisible());
                    if (warp.getLocation() != null) {
                        config.set(warpPath + ".x", warp.getLocation().getX());
                        config.set(warpPath + ".y", warp.getLocation().getY());
                        config.set(warpPath + ".z", warp.getLocation().getZ());
                        config.set(warpPath + ".yaw", warp.getLocation().getYaw());
                        config.set(warpPath + ".pitch", warp.getLocation().getPitch());
                    }
                }
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        if (!file.exists()) return;

        loadingData = true;

        try {
            if (config.contains("nextGridIndex")) {
                plugin.getIslandManager().setNextGridIndex(config.getInt("nextGridIndex"));
            }

            if (config.contains("availableGridIndices")) {
                List<Integer> savedIndices = config.getIntegerList("availableGridIndices");
                plugin.getIslandManager().getAvailableGridIndices().clear();
                plugin.getIslandManager().getAvailableGridIndices().addAll(savedIndices);
            }

            if (!config.contains("islands")) return;

            for (String uuidStr : config.getConfigurationSection("islands").getKeys(false)) {

                String path = "islands." + uuidStr;
                UUID uuid = UUID.fromString(uuidStr);
                Island island = plugin.getIslandManager().createIsland(uuid);

                island.setGridIndex(config.getInt(path + ".gridIndex", 0));
                String islandName = config.getString(path + ".name", "Island");
                island.setIslandName(islandName);

                int size = config.getInt(path + ".size", 50);
                island.setIslandSize(size);
                island.setLocked(config.getBoolean(path + ".locked", false));

                if (config.contains(path + ".biome")) {
                    try {
                        Biome biome = Biome.valueOf(config.getString(path + ".biome"));
                        island.setBiome(biome);
                    } catch (IllegalArgumentException ignored) {}
                }

                if (config.contains(path + ".world")) {
                    World world = Bukkit.getWorld(config.getString(path + ".world"));
                    double x = config.getDouble(path + ".x");
                    double y = config.getDouble(path + ".y");
                    double z = config.getDouble(path + ".z");

                    if (world != null) {
                        island.setCenterLocation(new Location(world, x, y, z));

                        if (config.contains(path + ".spawn.x")) {
                            double sx = config.getDouble(path + ".spawn.x");
                            double sy = config.getDouble(path + ".spawn.y");
                            double sz = config.getDouble(path + ".spawn.z");
                            float syaw = (float) config.getDouble(path + ".spawn.yaw", -90.0f);
                            float spitch = (float) config.getDouble(path + ".spawn.pitch", 15.0f);
                            island.setSpawnLocation(new Location(world, sx, sy, sz, syaw, spitch));
                        }

                        if (config.contains(path + ".warps")) {
                            for (String warpKey : config.getConfigurationSection(path + ".warps").getKeys(false)) {
                                String warpPath = path + ".warps." + warpKey;
                                String wName = config.getString(warpPath + ".name", warpKey);
                                Material wIcon = Material.getMaterial(config.getString(warpPath + ".icon", "OAK_SIGN"));
                                if (wIcon == null) wIcon = Material.OAK_SIGN;
                                boolean wVis = config.getBoolean(warpPath + ".visible", false);

                                double wx = config.getDouble(warpPath + ".x");
                                double wy = config.getDouble(warpPath + ".y");
                                double wz = config.getDouble(warpPath + ".z");
                                float wyaw = (float) config.getDouble(warpPath + ".yaw");
                                float wpitch = (float) config.getDouble(warpPath + ".pitch");

                                Location wLoc = new Location(world, wx, wy, wz, wyaw, wpitch);
                                Warp warp = new Warp(wName, wLoc, wIcon, wVis);
                                island.addWarp(warp);
                            }
                        }
                    }
                }
            }
        } finally {
            loadingData = false;
        }
    }
}