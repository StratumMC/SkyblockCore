package com.sallamadm.skyblockcore.data;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.data.accounts.AccountData;
import com.sallamadm.skyblockcore.data.core.DatabaseConnection;
import com.sallamadm.skyblockcore.data.island.IslandGameruleData;
import com.sallamadm.skyblockcore.data.island.IslandMemberData;
import com.sallamadm.skyblockcore.data.island.IslandPermissionData;
import com.sallamadm.skyblockcore.data.island.IslandData;
import com.sallamadm.skyblockcore.data.island.IslandWeatherData;
import com.sallamadm.skyblockcore.data.likes.IslandLikeData;
import com.sallamadm.skyblockcore.data.likes.IslandRatingData;
import com.sallamadm.skyblockcore.data.link.LinkCodeData;
import com.sallamadm.skyblockcore.island.Island;

import java.util.Map;
import java.util.UUID;

public class DataManager {

    public enum LikeResult {
        SUCCESS,
        ALREADY_LIKED_THIS_WEEK,
        MONTHLY_LIMIT_REACHED,
        DATABASE_ERROR
    }

    public enum RateResult {
        SUCCESS,
        ALREADY_RATED,
        DATABASE_ERROR
    }

    private final DatabaseConnection databaseConnection;

    private final AccountData accountData;
    private final LinkCodeData linkCodeData;
    private final IslandPermissionData permissionData;
    private final IslandGameruleData gameruleData;
    private final IslandWeatherData weatherData;
    private final IslandMemberData memberData;
    private final IslandData islandData;
    private final IslandLikeData likeData;
    private final IslandRatingData ratingData;

    public DataManager(SkyblockCore plugin) {
        this.databaseConnection = new DatabaseConnection(plugin);

        this.accountData = new AccountData(plugin, databaseConnection);
        this.linkCodeData = new LinkCodeData(databaseConnection);
        this.permissionData = new IslandPermissionData(plugin, databaseConnection);
        this.gameruleData = new IslandGameruleData(plugin, databaseConnection);
        this.weatherData = new IslandWeatherData(plugin, databaseConnection);
        this.memberData = new IslandMemberData(plugin, databaseConnection);
        this.islandData = new IslandData(plugin, databaseConnection,
                permissionData, gameruleData, weatherData, memberData);
        this.likeData = new IslandLikeData(databaseConnection);
        this.ratingData = new IslandRatingData(databaseConnection);
    }

    public boolean isLoading() {
        return islandData.isLoading();
    }

    public void closeConnection() {
        databaseConnection.closeConnection();
    }

    // ---- accounts / auth ----

    public boolean consumeMinecraftLinkCode(UUID playerUuid, String username, String inputCode) {
        return linkCodeData.consumeMinecraftLinkCode(playerUuid, username, inputCode);
    }

    public boolean checkIfDiscordLinked(UUID playerUuid) {
        return linkCodeData.checkIfDiscordLinked(playerUuid);
    }

    public boolean checkIfWebLinked(UUID playerUuid) {
        return linkCodeData.checkIfWebLinked(playerUuid);
    }

    public boolean consumeDiscordLinkCode(UUID playerUuid, String username, String inputCode) {
        return linkCodeData.consumeDiscordLinkCode(playerUuid, username, inputCode);
    }

    public boolean consumeWebLinkCode(UUID playerUuid, String username, String inputCode) {
        return linkCodeData.consumeWebLinkCode(playerUuid, username, inputCode);
    }

    public void updateUuid(String username, UUID uuid) {
        accountData.updateUuid(username, uuid);
    }

    public long loadFlyTimeSync(UUID uuid) {
        return accountData.loadFlyTimeSync(uuid);
    }

    public void saveFlyTimeAsync(UUID uuid, long remainingSeconds) {
        accountData.saveFlyTimeAsync(uuid, remainingSeconds);
    }

    public void saveFlyTimeSync(UUID uuid, long remainingSeconds) {
        accountData.saveFlyTimeSync(uuid, remainingSeconds);
    }

    public boolean isRegistered(String username) {
        return accountData.isRegistered(username);
    }

    public void registerAccount(UUID uuid, String username, String email, String password) {
        accountData.registerAccount(uuid, username, email, password);
    }

    public boolean checkPassword(String username, String inputPassword) {
        return accountData.checkPassword(username, inputPassword);
    }

    // ---- islands ----

    public void saveData() {
        islandData.saveData();
    }

    public void saveDataSync() {
        islandData.saveDataSync();
    }

    public void saveIslandAsync(Island island) {
        islandData.saveIslandAsync(island);
    }

    public void deleteIsland(UUID ownerUUID) {
        islandData.deleteIsland(ownerUUID);
    }

    public void loadData() {
        islandData.loadData();
    }

    public Map<String, Double> getTopLeveledIslands() {
        return islandData.getTopLeveledIslands();
    }

    // ---- island permissions / gamerules / weather / members ----

    public void setPermissionAsync(String islandUuid, int roleTier, String node, boolean granted) {
        permissionData.setPermissionAsync(islandUuid, roleTier, node, granted);
    }

    public void setGameruleAsync(String islandUuid, String node, boolean value) {
        gameruleData.setGameruleAsync(islandUuid, node, value);
    }

    public void setWeatherOptionAsync(String islandUuid, String node) {
        weatherData.setWeatherOptionAsync(islandUuid, node);
    }

    public void saveMemberAsync(String islandUuid, UUID playerUuid, int roleTier, UUID addedBy) {
        memberData.saveMemberAsync(islandUuid, playerUuid, roleTier, addedBy);
    }

    public void removeMemberAsync(String islandUuid, UUID playerUuid) {
        memberData.removeMemberAsync(islandUuid, playerUuid);
    }

    // ---- likes / ratings ----

    public LikeResult addIslandLike(UUID voterUuid, String islandUuid) {
        return likeData.addIslandLike(voterUuid, islandUuid);
    }

    public int getWeeklyLikeCount(String islandUuid) {
        return likeData.getWeeklyLikeCount(islandUuid);
    }

    public int getMonthlyLikeCount(String islandUuid) {
        return likeData.getMonthlyLikeCount(islandUuid);
    }

    public int getTotalLikeCount(String islandUuid) {
        return likeData.getTotalLikeCount(islandUuid);
    }

    public Map<String, Integer> getTopLikedIslands(String period) {
        return likeData.getTopLikedIslands(period);
    }

    public RateResult addIslandRating(UUID voterUuid, String islandUuid, double rating) {
        return ratingData.addIslandRating(voterUuid, islandUuid, rating);
    }

    public double getIslandRating(String islandUuid) {
        return ratingData.getIslandRating(islandUuid);
    }

    public Map<String, Double> getTopRatedIslands() {
        return ratingData.getTopRatedIslands();
    }
}