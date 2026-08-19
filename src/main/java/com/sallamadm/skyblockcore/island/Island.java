package com.sallamadm.skyblockcore.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.enums.IslandRole;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.*;
import java.util.stream.Collectors;

public class Island {
    private final UUID ownerUUID;
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

    private final Map<Integer, Set<String>> permissionCache = new HashMap<>();
    private final Map<UUID, Integer> memberRoles = new HashMap<>();
    private final Map<UUID, UUID> coopAddedBy = new HashMap<>();

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

    public int getRoleTier(UUID playerUuid) {
        if (playerUuid.equals(ownerUUID)) return IslandRole.OWNER.getTier();
        return memberRoles.getOrDefault(playerUuid, IslandRole.VISITOR.getTier());
    }

    public IslandRole getRole(UUID playerUuid) {
        return IslandRole.fromTier(getRoleTier(playerUuid));
    }

    public boolean hasPermission(int roleTier, String permissionNode) {
        if (roleTier == IslandRole.OWNER.getTier()) return true;
        Set<String> nodes = permissionCache.get(roleTier);
        return nodes != null && nodes.contains(permissionNode);
    }

    public boolean hasPermission(UUID playerUuid, String permissionNode) {
        return hasPermission(getRoleTier(playerUuid), permissionNode);
    }

    public void grantPermission(int roleTier, String node) {
        permissionCache.computeIfAbsent(roleTier, k -> new HashSet<>()).add(node);
        SkyblockCore.getInstance().getDataManager().setPermissionAsync(islandUuid, roleTier, node, true);
    }

    public void revokePermission(int roleTier, String node) {
        Set<String> set = permissionCache.get(roleTier);
        if (set != null) set.remove(node);
        SkyblockCore.getInstance().getDataManager().setPermissionAsync(islandUuid, roleTier, node, false);
    }

    public Set<String> getPermissionsForTier(int roleTier) {
        return permissionCache.getOrDefault(roleTier, Collections.emptySet());
    }

    public void setPermissionCache(Map<Integer, Set<String>> cache) {
        this.permissionCache.clear();
        this.permissionCache.putAll(cache);
    }

    public Map<UUID, Integer> getMemberRoles() {
        return memberRoles;
    }
    public Map<UUID, UUID> getCoopAddedBy() {
        return coopAddedBy;
    }
    public void addOrUpdateMember(UUID playerUuid, IslandRole role, UUID addedBy) {
        memberRoles.put(playerUuid, role.getTier());
        if (role == IslandRole.COOP && addedBy != null) {
            coopAddedBy.put(playerUuid, addedBy);
        } else {
            coopAddedBy.remove(playerUuid);
        }
        SkyblockCore.getInstance().getDataManager().saveMemberAsync(islandUuid, playerUuid, role.getTier(), addedBy);
    }
    public void removeMember(UUID playerUuid) {
        memberRoles.remove(playerUuid);
        coopAddedBy.remove(playerUuid);
        SkyblockCore.getInstance().getDataManager().removeMemberAsync(islandUuid, playerUuid);
    }
    public void loadMemberFromDb(UUID playerUuid, int roleTier, UUID addedBy) {
        memberRoles.put(playerUuid, roleTier);
        if (roleTier == IslandRole.COOP.getTier() && addedBy != null) {
            coopAddedBy.put(playerUuid, addedBy);
        }
    }
}