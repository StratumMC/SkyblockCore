package com.sallamadm.skyblockcore.island.enums;

import org.bukkit.Material;

public enum IslandGamerules {

    CREEPER_EXPLOSION("gamerule.creeperexplosion", "Creeper Patlaması", Material.CREEPER_HEAD, false),
    HARMFUL_MOB_SPAWN("gamerule.harmfulmobspawn", "Zararlı Mob Doğması", Material.ZOMBIE_HEAD, true),
    HARMLESS_MOB_SPAWN("gamerule.harmlessmobspawn", "Zararsız Mob Doğması", Material.SHEEP_SPAWN_EGG, true),
    TNT_EXPLOSION("gamerule.tntexplosion", "TNT Patlaması", Material.TNT, false),
    FIRE_SPREAD("gamerule.firespread", "Ateş Yayılması", Material.FLINT_AND_STEEL, false),
    LEAF_DECAY("gamerule.leafdecay", "Yaprak Çürümesi", Material.OAK_LEAVES, true);
    /* aklınıza gelen diğer oyun kurallarını buraya ekleyin
     * ust kısma ekleme yapabilirsiniz yada buraya yazın ben eklerim.
     */

    private final String node;
    private final String displayName;
    private final Material icon;
    private final boolean defaultValue;

    IslandGamerules(String node, String displayName, Material icon, boolean defaultValue) {
        this.node = node;
        this.displayName = displayName;
        this.icon = icon;
        this.defaultValue = defaultValue;
    }

    public String getNode() {
        return node;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public static IslandGamerules fromNode(String node) {
        for (IslandGamerules rule : values()) {
            if (rule.node.equalsIgnoreCase(node)) return rule;
        }
        return null;
    }
}
