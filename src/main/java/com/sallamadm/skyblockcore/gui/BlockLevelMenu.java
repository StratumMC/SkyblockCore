package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.gui.util.GuiUtils;
import com.sallamadm.skyblockcore.island.enums.IslandBlockLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlockLevelMenu implements Listener {
    private static final String MENU_PREFIX = ChatColor.DARK_GRAY + "Ada Blok Seviyeleri";
    private static final int BLOCKS_PER_PAGE = 45;

    private static final Map<UUID, Integer> CURRENT_PAGE = new HashMap<>();

    public static void openBlockLevelMenu(Player player) {
        openBlockLevelMenu(player, 1);
    }

    public static void openBlockLevelMenu(Player player, int page) {
        List<IslandBlockLevel> blocks = Arrays.asList(IslandBlockLevel.values());
        int totalPages = Math.max(1, (int) Math.ceil((double) blocks.size() / BLOCKS_PER_PAGE));
        page = clampPage(page, totalPages);

        Inventory inv = Bukkit.createInventory(null, 54, MENU_PREFIX + " (" + page + "/" + totalPages + ")");

        int start = (page - 1) * BLOCKS_PER_PAGE;
        int end = Math.min(start + BLOCKS_PER_PAGE, blocks.size());
        int slot = 0;

        for (int i = start; i < end; i++) {
            IslandBlockLevel blockData = blocks.get(i);
            inv.setItem(slot++, createBlockItem(blockData));
        }

        GuiUtils.applyNavigationBar(inv, page, totalPages);
        CURRENT_PAGE.put(player.getUniqueId(), page);
        player.openInventory(inv);
    }

    private static ItemStack createBlockItem(IslandBlockLevel blockData) {
        ItemStack item = new ItemStack(blockData.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String formattedName = formatMaterialName(blockData.getMaterial().name());
            meta.setDisplayName(ChatColor.GREEN + formattedName);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Verdiği Seviye: " + ChatColor.YELLOW + "+" + blockData.getLevel());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static int clampPage(int page, int totalPages) {
        if (page < 1) return 1;
        return Math.min(page, totalPages);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(MENU_PREFIX)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        int currentPage = CURRENT_PAGE.getOrDefault(player.getUniqueId(), 1);

        if (GuiUtils.isNavigationSlot(slot)) {
            if (slot == 49) {
                player.closeInventory();
                CURRENT_PAGE.remove(player.getUniqueId());
            } else if (slot == 45) {
                openBlockLevelMenu(player, currentPage - 1);
            } else if (slot == 53) {
                openBlockLevelMenu(player, currentPage + 1);
            }
        }
    }

    private static String formatMaterialName(String rawName) {
        String[] words = rawName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
