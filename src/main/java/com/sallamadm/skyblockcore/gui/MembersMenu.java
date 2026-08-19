package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
import com.sallamadm.skyblockcore.island.enums.IslandRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class MembersMenu implements Listener {

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final String MENU_TITLE = ChatColor.DARK_AQUA + "Üye Yönetimi";

    public static void openMembersMenu(Player player) {
        openMembersMenu(player, 1);
    }

    public static void openMembersMenu(Player viewer, int page) {
        Island island = SkyblockCore.getInstance().getIslandManager().getIsland(viewer.getUniqueId());
        if (island == null) {
            viewer.sendMessage(msg.getMessage("island.no-island"));
            return;
        }

        if (!island.hasPermission(viewer.getUniqueId(), IslandPermissions.MEMBERS_ROLE_CHANGE.getNode())) {
            viewer.sendMessage(msg.getMessage("general.no-permission"));
            return;
        }

        int maxPages = Math.max(1, (int) Math.ceil((double) island.getMemberRoles().size() / 45.0));
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE + " #" + page);

        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = blackGlass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            blackGlass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, blackGlass);
        }

        List<Map.Entry<UUID, Integer>> members = new ArrayList<>(island.getMemberRoles().entrySet());
        members.sort(Comparator.comparingInt(Map.Entry::getValue));

        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, members.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<UUID, Integer> entry = members.get(i);
            UUID memberUuid = entry.getKey();
            int roleTier = entry.getValue();
            IslandRole role = IslandRole.fromTier(roleTier);

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUuid);
            ItemStack memberItem;

            if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                memberItem = new ItemStack(offlinePlayer.isOnline() ? Material.PLAYER_HEAD : Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) memberItem.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(offlinePlayer);
                    meta.setDisplayName(getRoleColor(role) + offlinePlayer.getName());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Rol: " + getRoleColor(role) + role.getDisplayName());
                    lore.add(ChatColor.GRAY + "Tıklayarak rolünü değiştirin");
                    meta.setLore(lore);
                    memberItem.setItemMeta(meta);
                }
            } else {
                memberItem = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = memberItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(getRoleColor(role) + offlinePlayer.getName());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Rol: " + getRoleColor(role) + role.getDisplayName());
                    lore.add(ChatColor.GRAY + "Tıklayarak rolünü değiştirin");
                    meta.setLore(lore);
                    memberItem.setItemMeta(meta);
                }
            }

            inv.setItem(slot, memberItem);
            slot++;
        }

        ItemStack prevItem = new ItemStack(Material.PAPER);
        ItemMeta prevMeta = prevItem.getItemMeta();
        if (prevMeta != null) {
            prevMeta.setDisplayName(ChatColor.RED + "Önceki Sayfa");
            prevMeta.setCustomModelData(1);
            prevItem.setItemMeta(prevMeta);
        }
        inv.setItem(45, prevItem);

        ItemStack homeItem = new ItemStack(Material.PAPER);
        ItemMeta homeMeta = homeItem.getItemMeta();
        if (homeMeta != null) {
            homeMeta.setDisplayName(ChatColor.YELLOW + "Ana Menü");
            homeMeta.setCustomModelData(2);
            homeItem.setItemMeta(homeMeta);
        }
        inv.setItem(49, homeItem);

        ItemStack nextItem = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextItem.getItemMeta();
        if (nextMeta != null) {
            nextMeta.setDisplayName(ChatColor.GREEN + "Sonraki Sayfa");
            nextMeta.setCustomModelData(3);
            nextItem.setItemMeta(nextMeta);
        }
        inv.setItem(53, nextItem);

        ItemStack signItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta signMeta = signItem.getItemMeta();
        if (signMeta != null) {
            signMeta.setDisplayName(ChatColor.YELLOW + "Yetkileri Düzenle");
            signMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "İzin menüsünü açmak için tıklayın",
                    ChatColor.GRAY + "Yetkileri rol bazlı düzenleyin"
            ));
            signItem.setItemMeta(signMeta);
        }
        inv.setItem(52, signItem);

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        String title = event.getView().getTitle();
        if (!title.startsWith(MENU_TITLE)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        int slot = event.getSlot();
        Island island = SkyblockCore.getInstance().getIslandManager().getIsland(viewer.getUniqueId());
        if (island == null) {
            viewer.closeInventory();
            return;
        }

        if (slot == 45 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 1) {
                int currentPage = 1;
                if (title.contains(" #")) {
                    try {
                        currentPage = Integer.parseInt(title.split(" #")[1]);
                    } catch (Exception ignored) {}
                }
                openMembersMenu(viewer, currentPage - 1);
                return;
            }
        } else if (slot == 49 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 2) {
                viewer.closeInventory();
                viewer.performCommand("is");
                return;
            }
        } else if (slot == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 3) {
                int currentPage = 1;
                if (title.contains(" #")) {
                    try {
                        currentPage = Integer.parseInt(title.split(" #")[1]);
                    } catch (Exception ignored) {}
                }
                openMembersMenu(viewer, currentPage + 1);
                return;
            }
        } else if (slot == 52 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.OAK_SIGN) {
            viewer.closeInventory();
            PermissionsMenu.openPermissionsMenu(viewer);
            return;
        }

        if (slot >= 0 && slot < 45) {
            int itemsPerPage = 45;
            int currentPage = 1;
            if (title.contains(" #")) {
                try {
                    currentPage = Integer.parseInt(title.split(" #")[1]);
                } catch (Exception ignored) {}
            }

            int startIndex = (currentPage - 1) * itemsPerPage;
            int memberIndex = startIndex + slot;

            List<Map.Entry<UUID, Integer>> members = new ArrayList<>(island.getMemberRoles().entrySet());
            members.sort(Comparator.comparingInt(Map.Entry::getValue));

            if (memberIndex < members.size()) {
                Map.Entry<UUID, Integer> entry = members.get(memberIndex);
                UUID targetUuid = entry.getKey();
                int currentRoleTier = entry.getValue();
                IslandRole currentRole = IslandRole.fromTier(currentRoleTier);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetUuid);
                int viewerRoleTier = island.getRoleTier(viewer.getUniqueId());
                boolean isOwner = viewer.getUniqueId().equals(island.getOwnerUUID());

                if (!isOwner && viewerRoleTier >= currentRoleTier) {
                    viewer.sendMessage(msg.getMessage("general.no-permission"));
                    viewer.closeInventory();
                    return;
                }

                RoleSelectionMenu.openRoleSelectionMenu(viewer, targetUuid, targetPlayer.getName(), currentRole);
            }
        }
    }

    private static String getRoleColor(IslandRole role) {
        switch (role) {
            case OWNER: return ChatColor.RED.toString();
            case ADMIN: return ChatColor.DARK_RED.toString();
            case MOD: return ChatColor.GOLD.toString();
            case MEMBER: return ChatColor.AQUA.toString();
            case COOP: return ChatColor.LIGHT_PURPLE.toString();
            case VISITOR: return ChatColor.GRAY.toString();
            default: return ChatColor.WHITE.toString();
        }
    }
}