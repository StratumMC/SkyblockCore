package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.commands.IsCommand;
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

    private static final Set<Integer> WARPS_SLOTS = Set.of(30,31,32,39,40,41);
    private static final Set<Integer> ISLAND_GO_SLOTS = Set.of(45,46,47,48,49);
    private static final Set<Integer> MEMBERS_GO_SLOTS = Set.of(9,10,11,12,18,19,20,21);

    public static void openIsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);
        player.openInventory(inv);
    }


    /*
          "ascent": 13,
      "height": 254,
      {
      "type": "space",
      "advances": {
        "\uF808": -8
      }
    },

    warps 30-31-32 41-40-39
    island go 45-46-47-48-49
     */
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
        } else if(ISLAND_GO_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            IsCommand.teleportToIsland(SkyblockCore.getInstance(), player);
        } else if(MEMBERS_GO_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            IsCommand.teleportToIsland(SkyblockCore.getInstance(), player);
        }
    }
}