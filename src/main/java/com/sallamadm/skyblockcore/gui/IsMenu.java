package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.commands.IsCommand;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Set;

public class IsMenu implements Listener {

    public static final String MENU_TITLE = ChatColor.WHITE + "\uF808\uE001";

    private static final Set<Integer> WARPS_SLOTS = Set.of(30, 31, 32, 39, 40, 41);
    private static final Set<Integer> ISLAND_GO_SLOTS = Set.of(45, 46, 47, 48, 49);
    private static final Set<Integer> MEMBERS_SLOTS = Set.of(9, 10, 11, 12, 18, 19, 20, 21);
    private static final Set<Integer> TOP_ISLANDS_SLOTS = Set.of(13, 14, 15, 16, 17, 22, 23, 24, 25, 26);
    private static final Set<Integer> SETTINGS_SLOTS = Set.of(27, 28, 29, 36, 37, 38);

    public static void openIsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);
        player.openInventory(inv);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int clickedSlot = event.getSlot();
        if (WARPS_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            WarpMenu.openOwnerWarpMenu(player);
        } else if (ISLAND_GO_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            IsCommand.teleportToIsland(SkyblockCore.getInstance(), player);
        } else if (MEMBERS_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            MembersMenu.openMembersMenu(player,
                    SkyblockCore.getInstance().getIslandManager().getIslandByMember(player.getUniqueId()));
        } else if (TOP_ISLANDS_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            IsTopMenu.openTopMenu(player);
        } else if (SETTINGS_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            GameruleMenu.openGameruleMenu(player,
                    SkyblockCore.getInstance().getIslandManager().getIslandByMember(player.getUniqueId()));
        }
    }
}