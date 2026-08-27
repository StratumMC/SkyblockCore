package com.sallamadm.skyblockcore.island.enums;

import org.bukkit.Material;

import java.util.*;
public enum IslandPermissions {

    KICK("island.kick", "Oyuncu Atma", Material.BARRIER),
    BAN("island.ban", "Oyuncu Banlama", Material.REDSTONE_BLOCK),
    UNBAN("island.unban", "Ban Kaldırma", Material.REDSTONE),
    MANAGE_WARP("island.managewarp", "Warp Yönetimi", Material.ENDER_PEARL),
    BLOCK_BREAK("island.blockbreak", "Blok Kırma", Material.IRON_PICKAXE),
    BLOCK_PLACE("island.blockplace", "Blok Koyma", Material.DIRT),
    CONTAINER_ACCESS("island.container", "Sandık Kullanımı", Material.CHEST),
    INTERACT("island.interact", "Eşya Etkileşimi", Material.STICK),
    SET_SPAWN("island.setspawn", "Spawn Ayarlama", Material.RED_BED),
    SET_BIOME("island.setbiome", "Biyom Değiştirme", Material.GRASS_BLOCK),
    SET_NAME("island.setname", "İsim Değiştirme", Material.NAME_TAG),
    LOCK_ISLAND("island.lock", "Ada Kilitleme", Material.TRIPWIRE_HOOK),
    INVITE("island.invite", "Davet Etme", Material.PAPER),
    MANAGE_ROLES("island.manageroles", "Rol Yönetimi", Material.MINECART),
    MANAGE_GAMERULES("island.managegamerules", "Gamerule Yönetimi", Material.COMMAND_BLOCK),
    MANAGE_WEATHER("island.manageweather", "Hava Durumu Yönetimi", Material.SUNFLOWER),
    FLY("island.fly", "Uçuş İzni", Material.FEATHER);

    private final String node;
    private final String displayName;
    private final Material icon;

    IslandPermissions(String node, String displayName, Material icon) {
        this.node = node;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getNode() { return node; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }

    public static IslandPermissions fromNode(String node) {
        for (IslandPermissions perm : values()) {
            if (perm.node.equalsIgnoreCase(node)) return perm;
        }
        return null;
    }
    public static Set<String> getDefaultPermissionsForRole(IslandRole role) {
        Set<String> all = new HashSet<>();
        for (IslandPermissions p : values()) all.add(p.getNode());

        switch (role) {
            case ADMIN:
                return all;

            case MOD: {
                Set<String> mod = new HashSet<>(all);
                mod.remove(BAN.getNode());
                mod.remove(UNBAN.getNode());
                mod.remove(SET_SPAWN.getNode());
                mod.remove(MANAGE_GAMERULES.getNode());
                return mod;
            }

            case MEMBER:
                return new HashSet<>(Arrays.asList(
                        BLOCK_BREAK.getNode(), BLOCK_PLACE.getNode(),
                        CONTAINER_ACCESS.getNode(), INTERACT.getNode(),
                        FLY.getNode()
                ));

            case COOP:
                return new HashSet<>(Arrays.asList(
                        BLOCK_BREAK.getNode(), BLOCK_PLACE.getNode(),
                        FLY.getNode()
                ));

            case OWNER:
            case VISITOR:
            default:
                return Collections.emptySet();
        }
    }
}