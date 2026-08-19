package com.sallamadm.skyblockcore.island.enums;

import java.util.EnumSet;
import java.util.Set;
public enum IslandRole {

    OWNER(1, "Owner"),
    ADMIN(2, "Admin"),
    MOD(3, "Mod"),
    MEMBER(4, "Üye"),
    COOP(5, "Co-op"),
    VISITOR(6, "Ziyaretçi");

    private final int tier;
    private final String displayName;

    IslandRole(int tier, String displayName) {
        this.tier = tier;
        this.displayName = displayName;
    }

    public int getTier() {
        return tier;
    }

    public String getDisplayName() {
        return displayName;
    }
    public static IslandRole fromTier(int tier) {
        for (IslandRole role : values()) {
            if (role.tier == tier) return role;
        }
        return VISITOR;
    }
    public boolean isHigherThan(IslandRole other) {
        return this.tier < other.tier;
    }

    public boolean isManagerialByDefault() {
        return this == OWNER || this == ADMIN;
    }
    public Set<IslandRole> getSubordinateRoles() {
        Set<IslandRole> result = EnumSet.noneOf(IslandRole.class);
        for (IslandRole role : values()) {
            if (role.tier > this.tier) result.add(role);
        }
        return result;
    }
}