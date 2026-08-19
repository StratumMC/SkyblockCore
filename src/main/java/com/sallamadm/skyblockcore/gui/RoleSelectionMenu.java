package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class RoleSelectionMenu implements Listener {

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final String MENU_TITLE = ChatColor.LIGHT_PURPLE + "Rol Seçimi";

    private final UUID targetUuid;
    private final String targetName;
    private final IslandRole currentRole;

    public RoleSelectionMenu(UUID targetUuid, String targetName, IslandRole currentRole) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.currentRole = currentRole;
    }

    public static void openRoleSelectionMenu(Player viewer, UUID targetUuid, String targetName, IslandRole currentRole) {
        RoleSelectionMenu menu = new RoleSelectionMenu(targetUuid, targetName, currentRole);
        viewer.openInventory(menu.createInventory());
    }

    private Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE + " - " + getRoleColor(currentRole) + targetName);

        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = blackGlass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            blackGlass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, blackGlass);
        }

        int slot = 0;
        for (IslandRole role : IslandRole.values()) {
            if (role == IslandRole.VISITOR) continue;

            ItemStack roleItem;
            if (role == IslandRole.OWNER) {
                continue;
            }

            roleItem = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = roleItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(getRoleColor(role) + role.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Mevcut rol: " + getRoleColor(currentRole) + currentRole.getDisplayName());

                if (role == currentRole) {
                    lore.add(ChatColor.GREEN + "Şu anda bu rol seçili");
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else {
                    lore.add(ChatColor.GRAY + "Tıklayarak bu role ata");
                }
                meta.setLore(lore);
                roleItem.setItemMeta(meta);
            }

            inv.setItem(slot, roleItem);
            slot++;
        }
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Geri");
            backMeta.setCustomModelData(1);
            backItem.setItemMeta(backMeta);
        }
        inv.setItem(45, backItem);

        ItemStack homeItem = new ItemStack(Material.PAPER);
        ItemMeta homeMeta = homeItem.getItemMeta();
        if (homeMeta != null) {
            homeMeta.setDisplayName(ChatColor.YELLOW + "Ana Menü");
            homeMeta.setCustomModelData(2);
            homeItem.setItemMeta(homeMeta);
        }
        inv.setItem(49, homeItem);

        return inv;
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

        int viewerRoleTier = island.getRoleTier(viewer.getUniqueId());
        boolean isOwner = viewer.getUniqueId().equals(island.getOwnerUUID());
        if (!isOwner && viewerRoleTier >= currentRole.getTier()) {
            viewer.sendMessage(msg.getMessage("general.no-permission"));
            viewer.closeInventory();
            return;
        }

        if (slot == 45 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 1) {
                viewer.closeInventory();
                MembersMenu.openMembersMenu(viewer);
                return;
            }
        } else if (slot == 49 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 2) {
                viewer.closeInventory();
                viewer.performCommand("is");
                return;
            }
        }

        if (slot >= 0 && slot < 45) {
            List<IslandRole> selectableRoles = Arrays.stream(IslandRole.values())
                    .filter(r -> r != IslandRole.VISITOR && r != IslandRole.OWNER)
                    .collect(Collectors.toList());

            if (slot < selectableRoles.size()) {
                IslandRole selectedRole = selectableRoles.get(slot);

                if (!isOwner && selectedRole.getTier() <= viewerRoleTier) {
                    viewer.sendMessage(msg.getMessage("general.no-permission"));
                    return;
                }

                island.addOrUpdateMember(targetUuid, selectedRole, viewer.getUniqueId());

                viewer.sendMessage(msg.getMessage("island.role-changed")
                        .replace("{target}", targetName)
                        .replace("{role}", getRoleColor(selectedRole) + selectedRole.getDisplayName()));

                viewer.closeInventory();
                MembersMenu.openMembersMenu(viewer);
            }
        }
    }

    private String getRoleColor(IslandRole role) {
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