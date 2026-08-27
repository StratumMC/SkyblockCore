package com.sallamadm.skyblockcore.island.enums;
import org.bukkit.Material;
public enum IslandBlockLevel {
    DIRT(Material.DIRT, 0.01),
    GRASS_BLOCK(Material.GRASS_BLOCK, 0.03),
    STONE(Material.STONE, 0.2),
    COBBLESTONE(Material.COBBLESTONE, 0.15),

    DEEPSLATE(Material.DEEPSLATE, 0.2),
    COBBLED_DEEPSLATE(Material.COBBLED_DEEPSLATE, 0.15),

    LAPIS_BLOCK(Material.LAPIS_BLOCK, 0.33),
    REDSTONE_BLOCK(Material.REDSTONE_BLOCK, 0.33),
    COPPER_BLOCK(Material.COPPER_BLOCK, 0.44),

    IRON_BLOCK(Material.IRON_BLOCK, 1.2),
    GOLD_BLOCK(Material.GOLD_BLOCK, 1.7),
    DIAMOND_BLOCK(Material.DIAMOND_BLOCK, 2.3),
    EMERALD_BLOCK(Material.EMERALD_BLOCK, 2.8),
    NETHERITE_BLOCK(Material.NETHERITE_BLOCK, 3.4);

    private final Material material;
    private final double level;
    IslandBlockLevel(Material material, double level) {
        this.material = material;
        this.level = level;
    }
    public Material getMaterial() {
        return material;
    }
    public double getLevel() {
        return level;
    }
    public static IslandBlockLevel fromMaterial(Material material) {
        for (IslandBlockLevel block : values()) {
            if (block.material == material) {
                return block;
            }
        }
        return null;
    }
    public static boolean isLevelBlock(Material material) {
        return fromMaterial(material) != null;
    }
}