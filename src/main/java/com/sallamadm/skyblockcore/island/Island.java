package com.sallamadm.skyblockcore.island;

import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Island {
    private final UUID ownerUUID;
    private int level;
    private Location centerLocation;
    private Location spawnLocation;
    private int islandSize;
    private String islandName;
    private Biome biome;
    private boolean locked = false;
    private final Map<String, Warp> warps = new HashMap<>();

    public Island(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.level = 0;
        this.islandSize = 50;

        org.bukkit.OfflinePlayer owner = org.bukkit.Bukkit.getOfflinePlayer(ownerUUID);
        String ownerName = owner.getName() != null ? owner.getName() : "Player";
        this.islandName = ownerName + "'s Island";

        this.biome = Biome.PLAINS;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addLevel(int level) {
        this.level += level;
    }

    public Location getCenterLocation() {
        return centerLocation;
    }

    public void setCenterLocation(Location centerLocation) {
        this.centerLocation = centerLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation != null ? spawnLocation : centerLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public int getIslandSize() {
        return islandSize;
    }
    public void setIslandSize(int islandSize) {
        this.islandSize = islandSize;
    }

    public String getIslandName() {
        return islandName;
    }
    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    public Biome getBiome() {
        return biome;
    }
    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }


    public Map<String, Warp> getWarps() {
        return warps;
    }
    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }
    public void addWarp(Warp warp) {
        warps.put(warp.getName().toLowerCase(), warp);
    }
    public void removeWarp(String name) {
        warps.remove(name.toLowerCase());
    }
    public void renameWarp(String oldName, String newName) {
        Warp warp = warps.remove(oldName.toLowerCase());
        if (warp != null) {
            warp.setName(newName);
            warps.put(newName.toLowerCase(), warp);
        }
    }

}
