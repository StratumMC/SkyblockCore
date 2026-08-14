package com.sallamadm.skyblockcore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Set;

public class IslandSettingsMenuListener implements Listener {

    public static final String MENU_TITLE = ChatColor.WHITE + "\uF808\uF801\uE001";

    private static final Set<Integer> MEMBERS_HITBOX_SLOTS = Set.of(0, 1, 2, 9, 10, 11);

    public static void openSettingsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int clickedSlot = event.getSlot();

        if (MEMBERS_HITBOX_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            BiomeMenuListener.openBiomeMenu(player);
        }
    }
}