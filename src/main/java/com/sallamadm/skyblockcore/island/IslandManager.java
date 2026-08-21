package com.sallamadm.skyblockcore.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.gui.BiomeMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.*;

import static org.bukkit.block.Biome.PLAINS;

public class IslandManager {

    private final Map<UUID, Island> islands = new HashMap<>();
    private final List<Integer> availableGridIndices = new ArrayList<>();
    private int nextGridIndex = 0;

    public Island getIsland(UUID ownerUUID) {
        return islands.get(ownerUUID);
    }

    public boolean hasIsland(UUID ownerUUID) {
        return islands.containsKey(ownerUUID);
    }

    public Island createIsland(UUID ownerUUID) {
        Island island = new Island(ownerUUID);
        islands.put(ownerUUID, island);
        return island;
    }

    public void removeIsland(UUID ownerUUID) {
        Island island = islands.get(ownerUUID);
        if (island != null) {
            islands.remove(ownerUUID);
            SkyblockCore.getInstance().getDataManager().deleteIsland(ownerUUID);

            if (island.getCenterLocation() != null) {
                BiomeMenu.changeIslandBiome(island, org.bukkit.block.Biome.PLAINS);
                clearIslandBlocks(island);
            } else {
                availableGridIndices.add(island.getGridIndex());
                SkyblockCore.getInstance().getDataManager().saveData();
            }
        }
    }

    public void clearIslandBlocks(Island island) {
        Location center = island.getCenterLocation();
        if (center == null || center.getWorld() == null) return;

        org.bukkit.World world = center.getWorld();
        int radius = island.getIslandSize() / 2;

        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minZ = center.getBlockZ() - radius;
        int maxZ = center.getBlockZ() + radius;

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        new org.bukkit.scheduler.BukkitRunnable() {
            int currentX = minX;

            @Override
            public void run() {
                int blocksProcessedThisTick = 0;
                int maxBlocksPerTick = 15000;

                while (currentX <= maxX) {
                    for (int z = minZ; z <= maxZ; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            org.bukkit.block.Block block = world.getBlockAt(currentX, y, z);
                            if (block.getType() != org.bukkit.Material.AIR) {
                                block.setType(org.bukkit.Material.AIR, false);
                            }
                        }
                    }
                    currentX++;

                    blocksProcessedThisTick += (maxZ - minZ) * (maxY - minY);

                    if (blocksProcessedThisTick >= maxBlocksPerTick) {
                        return;
                    }
                }
                this.cancel();
                availableGridIndices.add(island.getGridIndex());
                SkyblockCore.getInstance().getDataManager().saveData();
                SkyblockCore.getInstance().getLogger().info("Grid " + island.getGridIndex() + " temizlendi ve yeni oyunculara acildi.");
            }
        }.runTaskTimer(SkyblockCore.getInstance(), 0L, 1L);
    }

    public int fetchNextGridIndex() {
        if (!availableGridIndices.isEmpty()) {
            return availableGridIndices.remove(0);
        }
        int indexToUse = nextGridIndex;
        nextGridIndex++;
        return indexToUse;
    }

    public Location calculateLocationFromIndex(World skyblockWorld, int gridIndex) {
        int distance = 1000;
        int x = 0, z = 0;

        if (gridIndex > 0) {
            int n = (int) Math.ceil((Math.sqrt(gridIndex + 1) - 1) / 2);
            int p = 2 * n;
            int k = gridIndex - (p - 1) * (p - 1);

            if (k < p) { x = n * distance; z = (-n + k + 1) * distance; }
            else if (k < 2 * p) { x = (n - (k - p + 1)) * distance; z = n * distance; }
            else if (k < 3 * p) { x = -n * distance; z = (n - (k - 2 * p + 1)) * distance; }
            else { x = (-n + (k - 3 * p + 1)) * distance; z = -n * distance; }
        }

        return new Location(skyblockWorld, x, 70, z);
    }

    public Map<UUID, Island> getAllIslands() {
        return islands;
    }

    public Island getIslandByMember(UUID uuid) {
        if (islands.containsKey(uuid)) return islands.get(uuid);
        for (Island island : islands.values()) {
            if (island.getMemberRoles().containsKey(uuid)) {
                return island;
            }
        }
        return null;
    }

    public int getNextGridIndex() {
        return nextGridIndex;
    }

    public void setNextGridIndex(int index) {
        this.nextGridIndex = index;
    }
    public List<Integer> getAvailableGridIndices() {
        return availableGridIndices;
    }
}
