package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.border.BorderManager;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.Warp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WarpMenu implements Listener {
    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final String VISITOR_MENU_PREFIX = ChatColor.DARK_GREEN + "Island Warps: ";
    private static final String OWNER_MENU_TITLE = ChatColor.DARK_PURPLE + "Your Island Warps";
    private static final String MANAGE_MENU_PREFIX = ChatColor.DARK_BLUE + "Manage Warp: ";

    private static final Map<UUID, String> PENDING_RENAME_WARPS = new HashMap<>();
    private static final Map<UUID, BlockData> ORIGINAL_BLOCK_DATA = new HashMap<>();

    public static void openSelfTeleportWarpMenu(Player player) {
        Island island = SkyblockCore.getInstance().getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(msg.getMessage("island.no-island"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, VISITOR_MENU_PREFIX + player.getName());

        int slot = 0;
        for (Warp warp : island.getWarps().values()) {
            if (!warp.isVisible() && !player.isOp()) continue;

            ItemStack item = new ItemStack(warp.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + warp.getName());
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to teleport to this warp."));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
            if (slot >= 27) break;
        }

        player.openInventory(inv);
    }

    public static void openVisitorWarpMenu(Player visitor, String targetName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(targetName);

        if (targetOwner == null || (!targetOwner.hasPlayedBefore() && !targetOwner.isOnline())) {
            visitor.sendMessage(msg.getMessage("general.player-not-found"));
            return;
        }

        Island island = SkyblockCore.getInstance().getIslandManager().getIsland(targetOwner.getUniqueId());
        if (island == null) {
            visitor.sendMessage(msg.getMessage("general.island-not-found"));
            return;
        }

        if (island.isLocked() && !visitor.isOp()) {
            visitor.sendMessage(msg.getMessage("island.locked-by-owner"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, VISITOR_MENU_PREFIX + targetOwner.getName());

        int slot = 0;
        for (Warp warp : island.getWarps().values()) {
            if (!warp.isVisible()) continue;

            ItemStack item = new ItemStack(warp.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + warp.getName());
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to teleport to this warp."));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
            if (slot >= 27) break;
        }

        visitor.openInventory(inv);
    }

    public static void openOwnerWarpMenu(Player owner) {
        Island island = SkyblockCore.getInstance().getIslandManager().getIsland(owner.getUniqueId());
        if (island == null) {
            owner.sendMessage(msg.getMessage("island.no-island"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, OWNER_MENU_TITLE);

        int slot = 0;
        for (Warp warp : island.getWarps().values()) {
            ItemStack item = new ItemStack(warp.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + warp.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Status: " + (warp.isVisible() ? ChatColor.GREEN + "Visible" : ChatColor.RED + "Hidden"));
                lore.add(ChatColor.GOLD + "Click to manage this warp.");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
            if (slot >= 27) break;
        }

        owner.openInventory(inv);
    }

    public static void openManageWarpMenu(Player owner, Warp warp) {
        Inventory inv = Bukkit.createInventory(null, 9, MANAGE_MENU_PREFIX + warp.getName());

        // name
        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nameItem.getItemMeta();
        if (nameMeta != null) {
            nameMeta.setDisplayName(ChatColor.GOLD + "Change Name");
            nameMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Current Name: " + ChatColor.YELLOW + warp.getName(),
                    ChatColor.YELLOW + "Click to write a name on sign."
            ));
            nameItem.setItemMeta(nameMeta);
        }
        inv.setItem(0, nameItem);

        // location
        ItemStack locItem = new ItemStack(Material.COMPASS);
        ItemMeta locMeta = locItem.getItemMeta();
        if (locMeta != null) {
            locMeta.setDisplayName(ChatColor.AQUA + "Change Location");
            locMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Click to update warp location",
                    ChatColor.GRAY + "to your current standing position."
            ));
            locItem.setItemMeta(locMeta);
        }
        inv.setItem(2, locItem);

        // icon
        ItemStack iconItem = new ItemStack(warp.getIcon());
        ItemMeta iconMeta = iconItem.getItemMeta();
        if (iconMeta != null) {
            iconMeta.setDisplayName(ChatColor.GREEN + "Change Icon");
            iconMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Hold an item in your main hand",
                    ChatColor.GRAY + "and click to set it as warp icon."
            ));
            iconItem.setItemMeta(iconMeta);
        }
        inv.setItem(4, iconItem);

        // visitor
        ItemStack visItem = new ItemStack(warp.isVisible() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta visMeta = visItem.getItemMeta();
        if (visMeta != null) {
            if (warp.isVisible()) {
                visMeta.setDisplayName(ChatColor.RED + "Disable Warp");
                visMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to hide this warp from visitors."));
            } else {
                visMeta.setDisplayName(ChatColor.GREEN + "Enable Warp");
                visMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to show this warp to visitors."));
            }
            visItem.setItemMeta(visMeta);
        }
        inv.setItem(6, visItem);

        // del
        ItemStack delItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta delMeta = delItem.getItemMeta();
        if (delMeta != null) {
            delMeta.setDisplayName(ChatColor.RED + "Delete Warp");
            delMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to permanently delete this warp."));
            delItem.setItemMeta(delMeta);
        }
        inv.setItem(8, delItem);

        owner.openInventory(inv);
    }

    private static void openSignRenameGUI(Player player, Warp warp) {
        PENDING_RENAME_WARPS.put(player.getUniqueId(), warp.getName());
        player.closeInventory();

        Location loc = player.getLocation().clone().add(0, 1, 0);
        Block block = loc.getBlock();

        ORIGINAL_BLOCK_DATA.put(player.getUniqueId(), block.getBlockData());

        block.setType(Material.OAK_SIGN);

        if (block.getState() instanceof Sign sign) {
            sign.setLine(0, "");
            sign.setLine(1, "^^^^^^^^^^^^^^^");
            sign.setLine(2, "Enter New Name");
            sign.setLine(3, "for: " + warp.getName());
            sign.update(true);

            player.openSign(sign, Side.FRONT);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (PENDING_RENAME_WARPS.containsKey(player.getUniqueId())) {
            String oldWarpName = PENDING_RENAME_WARPS.remove(player.getUniqueId());
            String newWarpName = event.getLine(0);

            Block block = event.getBlock();
            BlockData originalData = ORIGINAL_BLOCK_DATA.remove(player.getUniqueId());
            if (originalData != null) {
                block.setBlockData(originalData);
            } else {
                block.setType(Material.AIR);
            }

            if (newWarpName == null || newWarpName.trim().isEmpty()) {
                player.sendMessage(msg.getMessage("warp.empty-name"));
                return;
            }

            newWarpName = newWarpName.trim();

            Island island = SkyblockCore.getInstance().getIslandManager().getIsland(player.getUniqueId());
            if (island != null) {
                island.renameWarp(oldWarpName, newWarpName);
                player.sendMessage(msg.getMessage("warp.renamed").replace("{name}", newWarpName));

                Warp updatedWarp = island.getWarp(newWarpName);
                if (updatedWarp != null) {
                    Warp finalUpdatedWarp = updatedWarp;
                    Bukkit.getScheduler().runTask(SkyblockCore.getInstance(), () -> {
                        openManageWarpMenu(player, finalUpdatedWarp);
                    });
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!title.startsWith(VISITOR_MENU_PREFIX) && !title.equals(OWNER_MENU_TITLE) && !title.startsWith(MANAGE_MENU_PREFIX)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        if (title.startsWith(VISITOR_MENU_PREFIX)) {
            String targetName = title.replace(VISITOR_MENU_PREFIX, "");

            @SuppressWarnings("deprecation")
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);

            Island island = SkyblockCore.getInstance().getIslandManager().getIsland(targetPlayer.getUniqueId());
            if (island == null) {
                player.sendMessage(msg.getMessage("general.island-not-found"));
                player.closeInventory();
                return;
            }

            String warpName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            Warp warp = island.getWarp(warpName);

            if (warp != null && warp.getLocation() != null) {
                player.setFallDistance(0);
                player.teleport(warp.getLocation());
                BorderManager.applyIslandBorder(player, island);
                player.sendMessage(msg.getMessage("warp.teleported")
                        .replace("{target}", targetName)
                        .replace("{warp}", warp.getName()));
            }
            player.closeInventory();
        }

        else if (title.equals(OWNER_MENU_TITLE)) {
            Island island = SkyblockCore.getInstance().getIslandManager().getIsland(player.getUniqueId());
            if (island == null) return;

            String warpName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            Warp warp = island.getWarp(warpName);

            if (warp != null) {
                openManageWarpMenu(player, warp);
            }
        }

        else if (title.startsWith(MANAGE_MENU_PREFIX)) {
            Island island = SkyblockCore.getInstance().getIslandManager().getIsland(player.getUniqueId());
            if (island == null) return;

            String warpName = title.replace(MANAGE_MENU_PREFIX, "");
            Warp warp = island.getWarp(warpName);

            if (warp == null) {
                player.closeInventory();
                return;
            }

            int slot = event.getSlot();

            // name change
            if (slot == 0) {
                openSignRenameGUI(player, warp);
            }

            // location change
            else if (slot == 2) {
                Location playerLoc = player.getLocation();
                Location center = island.getCenterLocation();

                if (center == null || !playerLoc.getWorld().equals(center.getWorld())) {
                    player.sendMessage(msg.getMessage("warp.must-be-on-island"));
                    return;
                }

                int radius = island.getIslandSize() / 2;
                if (Math.abs(playerLoc.getBlockX() - center.getBlockX()) > radius || Math.abs(playerLoc.getBlockZ() - center.getBlockZ()) > radius) {
                    player.sendMessage(msg.getMessage("warp.outside-boundary"));
                    return;
                }

                warp.setLocation(playerLoc);
                player.sendMessage(msg.getMessage("warp.location-updated"));
                openManageWarpMenu(player, warp);
            }

            // icon change
            else if (slot == 4) {
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem.getType() == Material.AIR) {
                    player.sendMessage(msg.getMessage("warp.hold-item-error"));
                } else {
                    warp.setIcon(handItem.getType());
                    player.sendMessage(msg.getMessage("warp.icon-updated")
                            .replace("{icon}", handItem.getType().name()));
                    openManageWarpMenu(player, warp);
                }
            }

            // visitor visible
            else if (slot == 6) {
                warp.setVisible(!warp.isVisible());
                player.sendMessage(msg.getMessage("warp.visibility-toggle")
                        .replace("{status}", warp.isVisible() ? "ENABLED" : "DISABLED"));
                openManageWarpMenu(player, warp);
            }

            // warp del
            else if (slot == 8) {
                island.removeWarp(warp.getName());
                player.sendMessage(msg.getMessage("warp.deleted")
                        .replace("{warp}", warp.getName()));
                openOwnerWarpMenu(player);
            }
        }
    }
}