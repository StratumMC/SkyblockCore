package com.sallamadm.skyblockcore.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.*;
import java.util.stream.Collectors;

public class Island {
    private UUID ownerUUID;
    private String islandUuid;
    private int level;
    private int gridIndex;
    private Location centerLocation;
    private Location spawnLocation;
    private int islandSize;
    private String islandName;
    private Biome biome;
    private boolean locked = false;
    private final Map<String, Warp> warps = new HashMap<>();
    private final Set<UUID> bannedPlayers = new HashSet<>();


    public Island(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.islandUuid = UUID.randomUUID().toString();
        this.level = 0;
        this.islandSize = 50;

        org.bukkit.OfflinePlayer owner = org.bukkit.Bukkit.getOfflinePlayer(ownerUUID);
        String ownerName = owner.getName() != null ? owner.getName() : "Player";
        this.islandName = ownerName + "'s Island";

        this.biome = Biome.PLAINS;
    }

    private void autoSave() {
        if (SkyblockCore.getInstance() != null && SkyblockCore.getInstance().getDataManager() != null) {
            if (!SkyblockCore.getInstance().getDataManager().isLoading()) {
                SkyblockCore.getInstance().getDataManager().saveIslandAsync(this);
            }
        }
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        autoSave();
    }

    public String getIslandUuid() {
        return islandUuid;
    }
    public void setIslandUuid(String islandUuid) {
        this.islandUuid = islandUuid;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
        autoSave();
    }
    public void addLevel(int level) {
        this.level += level;
        autoSave();
    }

    public int getGridIndex() {
        return gridIndex;
    }
    public void setGridIndex(int gridIndex) {
        this.gridIndex = gridIndex;
        autoSave();
    }

    public Location getCenterLocation() {
        return centerLocation;
    }
    public void setCenterLocation(Location centerLocation) {
        this.centerLocation = centerLocation;
        autoSave();
    }
    public Location getSpawnLocation() {
        return spawnLocation != null ? spawnLocation : centerLocation;
    }
    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        autoSave();
    }

    public int getIslandSize() {
        return islandSize;
    }
    public void setIslandSize(int islandSize) {
        this.islandSize = islandSize;
        autoSave();
    }

    public String getIslandName() {
        return islandName;
    }
    public void setIslandName(String islandName) {
        this.islandName = islandName;
        autoSave();
    }

    public Biome getBiome() {
        return biome;
    }
    public void setBiome(Biome biome) {
        this.biome = biome;
        autoSave();
    }

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
        autoSave();
    }

    public Map<String, Warp> getWarps() {
        return warps;
    }
    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }
    public void addWarp(Warp warp) {
        warps.put(warp.getName().toLowerCase(), warp);
        autoSave();
    }
    public void removeWarp(String name) {
        warps.remove(name.toLowerCase());
        autoSave();
    }
    public void renameWarp(String oldName, String newName) {
        Warp warp = warps.remove(oldName.toLowerCase());
        if (warp != null) {
            warp.setName(newName);
            warps.put(newName.toLowerCase(), warp);
            autoSave();
        }
    }

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }
    public boolean isBanned(UUID uuid) {
        return bannedPlayers.contains(uuid);
    }
    public void banPlayer(UUID uuid) {
        bannedPlayers.add(uuid);
        autoSave();
    }
    public void unbanPlayer(UUID uuid) {
        bannedPlayers.remove(uuid);
        autoSave();
    }
    public String getBannedPlayersAsString() {
        return bannedPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }
    public void loadBannedPlayersFromString(String data) {
        bannedPlayers.clear();
        if (data == null || data.isEmpty()) return;
        for (String uuidStr : data.split(",")) {
            try {
                bannedPlayers.add(UUID.fromString(uuidStr.trim()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

}