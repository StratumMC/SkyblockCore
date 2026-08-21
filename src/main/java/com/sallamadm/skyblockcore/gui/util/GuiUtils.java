package com.sallamadm.skyblockcore.gui.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiUtils {

    private static final int HOME_MODEL_DATA = 2;
    private static final int PREV_MODEL_DATA = 1;
    private static final int NEXT_MODEL_DATA = 3;

    public static void applyNavigationBar(Inventory inv, int currentPage, int totalPages) {
        ItemStack blackGlass = createGlassFiller();
        for (int slot = 45; slot <= 53; slot++) {
            inv.setItem(slot, blackGlass);
        }

        inv.setItem(49, createNavButton(Material.PAPER, HOME_MODEL_DATA,
                ChatColor.YELLOW + "Ana Menü", "Ana menüye dönmek için tıklayın."));

        if (currentPage > 1) {
            inv.setItem(45, createNavButton(Material.PAPER, PREV_MODEL_DATA,
                    ChatColor.GREEN + "Önceki Sayfa", "Sayfa " + (currentPage - 1) + "'e gider."));
        }

        if (currentPage < totalPages) {
            inv.setItem(53, createNavButton(Material.PAPER, NEXT_MODEL_DATA,
                    ChatColor.GREEN + "Sonraki Sayfa", "Sayfa " + (currentPage + 1) + "'e gider."));
        }
    }

    private static ItemStack createGlassFiller() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createNavButton(Material base, int modelData, String name, String lore) {
        ItemStack item = new ItemStack(base);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setCustomModelData(modelData);
            meta.setLore(java.util.Collections.singletonList(ChatColor.GRAY + lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isNavigationSlot(int slot) {
        return slot >= 45 && slot <= 53;
    }
}