package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.border.BorderManager;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.gui.util.GuiUtils;
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

    private static final String VISITOR_MENU_PREFIX = ChatColor.DARK_GREEN + "Ada warpları: ";
    private static final String OWNER_MENU_TITLE = ChatColor.DARK_PURPLE + "Adanızdaki warplar: ";
    private static final String MANAGE_MENU_PREFIX = ChatColor.DARK_BLUE + "Warp düzenle: ";

    private static final int WARPS_PER_PAGE = 45;

    private static final Map<UUID, String> PENDING_RENAME_WARPS = new HashMap<>();
    private static final Map<UUID, BlockData> ORIGINAL_BLOCK_DATA = new HashMap<>();
    private static final Map<UUID, Integer> CURRENT_PAGE = new HashMap<>();


    public static void openSelfTeleportWarpMenu(Player player) {
        openSelfTeleportWarpMenu(player, 1);
    }

    public static void openSelfTeleportWarpMenu(Player player, int page) {
        Island island = SkyblockCore.getInstance().getIslandManager().getIslandByMember(player.getUniqueId());
        if (island == null) {
            player.sendMessage(msg.getMessage("island.no-island"));
            return;
        }

        List<Warp> visibleWarps = new ArrayList<>();
        for (Warp warp : island.getWarps().values()) {
            if (warp.isVisible() || player.isOp()) visibleWarps.add(warp);
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) visibleWarps.size() / WARPS_PER_PAGE));
        page = clampPage(page, totalPages);

        Inventory inv = Bukkit.createInventory(null, 54, VISITOR_MENU_PREFIX + player.getName());
        fillTeleportItems(inv, visibleWarps, page);
        GuiUtils.applyNavigationBar(inv, page, totalPages);

        CURRENT_PAGE.put(player.getUniqueId(), page);
        player.openInventory(inv);
    }


    public static void openVisitorWarpMenu(Player visitor, String targetName) {
        openVisitorWarpMenu(visitor, targetName, 1);
    }

    public static void openVisitorWarpMenu(Player visitor, String targetName, int page) {
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

        List<Warp> visibleWarps = new ArrayList<>();
        for (Warp warp : island.getWarps().values()) {
            if (warp.isVisible()) visibleWarps.add(warp);
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) visibleWarps.size() / WARPS_PER_PAGE));
        page = clampPage(page, totalPages);

        Inventory inv = Bukkit.createInventory(null, 54, VISITOR_MENU_PREFIX + targetOwner.getName());
        fillTeleportItems(inv, visibleWarps, page);
        GuiUtils.applyNavigationBar(inv, page, totalPages);

        CURRENT_PAGE.put(visitor.getUniqueId(), page);
        visitor.openInventory(inv);
    }


    public static void openOwnerWarpMenu(Player owner) {
        openOwnerWarpMenu(owner, 1);
    }

    public static void openOwnerWarpMenu(Player owner, int page) {
        Island island = SkyblockCore.getInstance().getIslandManager().getIslandByMember(owner.getUniqueId());
        if (island == null) {
            owner.sendMessage(msg.getMessage("island.no-island"));
            return;
        }

        List<Warp> allWarps = new ArrayList<>(island.getWarps().values());
        int totalPages = Math.max(1, (int) Math.ceil((double) allWarps.size() / WARPS_PER_PAGE));
        page = clampPage(page, totalPages);

        Inventory inv = Bukkit.createInventory(null, 54, OWNER_MENU_TITLE);

        int start = (page - 1) * WARPS_PER_PAGE;
        int end = Math.min(start + WARPS_PER_PAGE, allWarps.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            Warp warp = allWarps.get(i);
            ItemStack item = new ItemStack(warp.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + warp.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Durum: " + (warp.isVisible() ? ChatColor.GREEN + "Açık" : ChatColor.RED + "Kapalı"));
                lore.add(ChatColor.GOLD + "Warpı düzenlemek için tıklayın.");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        GuiUtils.applyNavigationBar(inv, page, totalPages);
        CURRENT_PAGE.put(owner.getUniqueId(), page);
        owner.openInventory(inv);
    }

    private static void fillTeleportItems(Inventory inv, List<Warp> warps, int page) {
        int start = (page - 1) * WARPS_PER_PAGE;
        int end = Math.min(start + WARPS_PER_PAGE, warps.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            Warp warp = warps.get(i);
            ItemStack item = new ItemStack(warp.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + warp.getName());
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Warpa ışınlanmak için tıklayın."));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }
    }

    private static int clampPage(int page, int totalPages) {
        if (page < 1) return 1;
        return Math.min(page, totalPages);
    }

    public static void openManageWarpMenu(Player owner, Warp warp) {
        Inventory inv = Bukkit.createInventory(null, 9, MANAGE_MENU_PREFIX + warp.getName());

        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nameItem.getItemMeta();
        if (nameMeta != null) {
            nameMeta.setDisplayName(ChatColor.GOLD + "Warp ismi değiştirin");
            nameMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Şuanki isim: " + ChatColor.YELLOW + warp.getName(),
                    ChatColor.YELLOW + "Yeni isim koymak için tıklayın."
            ));
            nameItem.setItemMeta(nameMeta);
        }
        inv.setItem(0, nameItem);

        ItemStack locItem = new ItemStack(Material.COMPASS);
        ItemMeta locMeta = locItem.getItemMeta();
        if (locMeta != null) {
            locMeta.setDisplayName(ChatColor.AQUA + "Warp lokasyonu");
            locMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Warp lokasyonunu şuanki",
                    ChatColor.GRAY + "konumuz ile değiştirmek için tıklayın."
            ));
            locItem.setItemMeta(locMeta);
        }
        inv.setItem(2, locItem);

        ItemStack iconItem = new ItemStack(warp.getIcon());
        ItemMeta iconMeta = iconItem.getItemMeta();
        if (iconMeta != null) {
            iconMeta.setDisplayName(ChatColor.GREEN + "İkon değiştirme");
            iconMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Warp ikonunu ayarlamak için",
                    ChatColor.GRAY + "ana elinizde bir eşya tutunuz."
            ));
            iconItem.setItemMeta(iconMeta);
        }
        inv.setItem(4, iconItem);

        ItemStack visItem = new ItemStack(warp.isVisible() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta visMeta = visItem.getItemMeta();
        if (visMeta != null) {
            if (warp.isVisible()) {
                visMeta.setDisplayName(ChatColor.RED + "Warpı kapat");
                visMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Ziyaretçilere kapatmak için tıklayın."));
            } else {
                visMeta.setDisplayName(ChatColor.GREEN + "Warpı aç");
                visMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Ziyaretçilere açmak için tıklayın."));
            }
            visItem.setItemMeta(visMeta);
        }
        inv.setItem(6, visItem);

        ItemStack delItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta delMeta = delItem.getItemMeta();
        if (delMeta != null) {
            delMeta.setDisplayName(ChatColor.RED + "Warp sil");
            delMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Warpı silmek için tıklayın."));
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
            sign.setLine(2, "Yeni isim giriniz");
            sign.setLine(3, "Eski isim: " + warp.getName());
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

        boolean isListMenu = title.startsWith(VISITOR_MENU_PREFIX) || title.equals(OWNER_MENU_TITLE);
        boolean isManageMenu = title.startsWith(MANAGE_MENU_PREFIX);

        if (!isListMenu && !isManageMenu) return;

        event.setCancelled(true);

        if (isListMenu && GuiUtils.isNavigationSlot(event.getSlot())) {
            handleNavigationClick(player, title, event.getSlot());
            return;
        }

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
            CURRENT_PAGE.remove(player.getUniqueId());
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

        else if (isManageMenu) {
            Island island = SkyblockCore.getInstance().getIslandManager().getIsland(player.getUniqueId());
            if (island == null) return;

            String warpName = title.replace(MANAGE_MENU_PREFIX, "");
            Warp warp = island.getWarp(warpName);

            if (warp == null) {
                player.closeInventory();
                return;
            }

            int slot = event.getSlot();

            if (slot == 0) {
                openSignRenameGUI(player, warp);
            }
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

                if(player.isFlying()) {
                    player.sendMessage(msg.getMessage("fly.dont-fly"));
                    return;
                }

                warp.setLocation(playerLoc);
                player.sendMessage(msg.getMessage("warp.location-updated"));
                openManageWarpMenu(player, warp);
            }
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
            else if (slot == 6) {
                warp.setVisible(!warp.isVisible());
                player.sendMessage(msg.getMessage("warp.visibility-toggle")
                        .replace("{status}", warp.isVisible() ? "AÇIK" : "KAPALI"));
                openManageWarpMenu(player, warp);
            }
            else if (slot == 8) {
                island.removeWarp(warp.getName());
                player.sendMessage(msg.getMessage("warp.deleted")
                        .replace("{warp}", warp.getName()));
                openOwnerWarpMenu(player);
            }
        }
    }

    private void handleNavigationClick(Player player, String title, int slot) {
        if (slot == 49) {
            player.closeInventory();
            CURRENT_PAGE.remove(player.getUniqueId());
            IsMenu.openIsMenu(player);
            return;
        }

        int current = CURRENT_PAGE.getOrDefault(player.getUniqueId(), 1);
        int targetPage = (slot == 45) ? current - 1 : current + 1;

        if (title.equals(OWNER_MENU_TITLE)) {
            openOwnerWarpMenu(player, targetPage);
        } else if (title.startsWith(VISITOR_MENU_PREFIX)) {
            String targetName = title.replace(VISITOR_MENU_PREFIX, "");
            if (targetName.equalsIgnoreCase(player.getName())) {
                openSelfTeleportWarpMenu(player, targetPage);
            } else {
                openVisitorWarpMenu(player, targetName, targetPage);
            }
        }
    }
}