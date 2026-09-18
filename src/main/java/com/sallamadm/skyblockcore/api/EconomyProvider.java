package com.sallamadm.skyblockcore.api;

import java.util.UUID;

public interface EconomyProvider {
    double getBalance(UUID uuid);
    void addBalance(UUID uuid, double amount);
    void removeBalance(UUID uuid, double amount);
    boolean hasBalance(UUID uuid, double amount);
    default double getIslandBankBalance(String islandUuid) {
        return 0D;
    }
    default void addIslandBankBalance(double amount, String islandUuid) {
    }
    default boolean removeIslandBankBalance(double amount, String islandUuid) {
        return false;
    }
}