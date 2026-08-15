package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BiomeMenu implements Listener {
    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    public static void openBiomeMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_BLUE + "Biome Selector");

        inv.setItem(11, createBiomeItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Plains Biome", "Click to set your island to Plains."));

        inv.setItem(13, createBiomeItem(Material.SAND, ChatColor.YELLOW + "Desert Biome", "Click to set your island to Desert."));

        inv.setItem(15, createBiomeItem(Material.WATER_BUCKET, ChatColor.AQUA + "Ocean Biome", "Click to set your island to Ocean."));

        player.openInventory(inv);
    }

    private static ItemStack createBiomeItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + description));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_BLUE + "Biome Selector")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        Material clickedType = event.getCurrentItem().getType();

        Biome selectedBiome = null;
        String biomeName = "";

        if (clickedType == Material.GRASS_BLOCK) {
            selectedBiome = Biome.PLAINS;
            biomeName = "Plains";
        } else if (clickedType == Material.SAND) {
            selectedBiome = Biome.DESERT;
            biomeName = "Desert";
        } else if (clickedType == Material.WATER_BUCKET) {
            selectedBiome = Biome.OCEAN;
            biomeName = "Ocean";
        }

        if (selectedBiome != null) {
            Island island = SkyblockCore.getInstance().getIslandManager().getIsland(player.getUniqueId());

            if (island == null) {
                player.sendMessage(msg.getMessage("island.no-island"));
                player.closeInventory();
                return;
            }

            island.setBiome(selectedBiome);

            SkyblockCore.getInstance().getDataManager().saveData();

            changeIslandBiome(island, selectedBiome);

            player.sendMessage(msg.getMessage("biome.updated").replace("{biome}", selectedBiome.name()));
            player.closeInventory();
        }
    }

    public static void changeIslandBiome(Island island, Biome biome) {
        Location center = island.getCenterLocation();
        if (center == null || center.getWorld() == null) return;

        World world = center.getWorld();
        int radius = island.getIslandSize() / 2;

        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minZ = center.getBlockZ() - radius;
        int maxZ = center.getBlockZ() + radius;

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        Set<Chunk> affectedChunks = new HashSet<>();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y < maxY; y += 4) {
                    world.setBiome(x, y, z, biome);
                }
                affectedChunks.add(world.getChunkAt(x >> 4, z >> 4));
            }
        }

        for (Chunk chunk : affectedChunks) {
            world.refreshChunk(chunk.getX(), chunk.getZ());
        }
    }
}