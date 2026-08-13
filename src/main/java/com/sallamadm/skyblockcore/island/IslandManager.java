package com.sallamadm.skyblockcore.island;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandManager {

    private final Map<UUID, Island> islands = new HashMap<>();
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
        if (island != null && island.getCenterLocation() != null) {
            clearIslandBlocks(island);
        }
        islands.remove(ownerUUID);
    }

    private void clearIslandBlocks(Island island) {
        Location loc = island.getCenterLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                }
            }
        }
    }


    public Location getNextIslandLocation(World skyblockWorld) {
        int distance = 1000;
        int x = 0, z = 0;

        if (nextGridIndex > 0) {
            int n = (int) Math.ceil((Math.sqrt(nextGridIndex + 1) - 1) / 2);
            int p = 2 * n;
            int k = nextGridIndex - (p - 1) * (p - 1);

            if (k < p) { x = n * distance; z = (-n + k + 1) * distance; }
            else if (k < 2 * p) { x = (n - (k - p + 1)) * distance; z = n * distance; }
            else if (k < 3 * p) { x = -n * distance; z = (n - (k - 2 * p + 1)) * distance; }
            else { x = (-n + (k - 3 * p + 1)) * distance; z = -n * distance; }
        }

        nextGridIndex++;
        return new Location(skyblockWorld, x, 70, z);
    }

    public Map<UUID, Island> getAllIslands() {
        return islands;
    }

    public int getNextGridIndex() {
        return nextGridIndex;
    }

    public void setNextGridIndex(int index) {
        this.nextGridIndex = index;
    }

}
