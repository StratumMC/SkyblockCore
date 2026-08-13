package com.sallamadm.skyblockcore.world;

import com.sallamadm.skyblockcore.SkyblockCore;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.List;

public class WorldManager {
    private final SkyblockCore plugin;
    private World skyblockWorld;

    public WorldManager(SkyblockCore plugin) {
        this.plugin = plugin;
        createSkyblockWorld();
    }

    private void createSkyblockWorld() {
        WorldCreator creator = new WorldCreator("skyblock_world");
        creator.generator(new VoidChunkGenerator());


        creator.biomeProvider(new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return Biome.PLAINS;
            }

            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return List.of(Biome.PLAINS);
            }
        });
        this.skyblockWorld = Bukkit.createWorld(creator);

        if (skyblockWorld != null) {
            skyblockWorld.setSpawnLocation(0, 70, 0);

            skyblockWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
            skyblockWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        }
    }

    public World getSkyblockWorld() {
        return skyblockWorld;
    }
}
