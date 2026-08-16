package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.border.BorderManager;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.events.IslandEvents;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.IslandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Set;

public class IslandDeleteMenu implements Listener {
    public static final String MENU_TITLE = ChatColor.WHITE + "\uF806\uE002";

    private static final Set<Integer> CONFIRM_SLOTS = Set.of(
            10,11,12
    );

    private static final Set<Integer> CANCEL_SLOTS = Set.of(
            14,15,16
    );

    public static void openConfirmMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);
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

        SkyblockCore plugin = SkyblockCore.getInstance();
        MessageManager msg = plugin.getMessageManager();
        IslandManager islandManager = plugin.getIslandManager();
        int clickedSlot = event.getSlot();

        if (CONFIRM_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            Island islandToDelete = islandManager.getIsland(player.getUniqueId());

            if (islandToDelete == null) {
                player.sendMessage(msg.getMessage("island.no-island"));
                return;
            }

            if (islandToDelete.getCenterLocation() != null && islandToDelete.getCenterLocation().getWorld() != null) {
                org.bukkit.World world = islandToDelete.getCenterLocation().getWorld();
                int radius = islandToDelete.getIslandSize() / 2;
                int centerX = islandToDelete.getCenterLocation().getBlockX();
                int centerZ = islandToDelete.getCenterLocation().getBlockZ();

                for (Player p : world.getPlayers()) {
                    if (Math.abs(p.getLocation().getBlockX() - centerX) <= radius &&
                            Math.abs(p.getLocation().getBlockZ() - centerZ) <= radius) {
                        BorderManager.removeBorder(p);
                        Island pIsland = islandManager.getIsland(p.getUniqueId());
                        if (pIsland != null && pIsland.getSpawnLocation() != null) {
                            p.setFallDistance(0);
                            p.teleport(pIsland.getSpawnLocation());
                        } else {
                            p.setFallDistance(0);
                            p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                        }
                        p.sendMessage(msg.getMessage("island.deleted"));
                    }
                }
            }

            Bukkit.getPluginManager().callEvent(new IslandEvents.Delete(player, islandToDelete));
            BorderManager.removeBorder(player);
            islandManager.removeIsland(player.getUniqueId());

            plugin.getDataManager().saveData();
            plugin.getScoreboardManager().updateScoreboard(player);

            World mainWorld = Bukkit.getWorlds().get(0);
            player.teleport(mainWorld.getSpawnLocation());

            player.sendMessage(msg.getMessage("island.deleted"));
        }
        else if (CANCEL_SLOTS.contains(clickedSlot)) {
            player.closeInventory();
            player.sendMessage(msg.getMessage("island.delete-cancelled"));
        }
    }
}
