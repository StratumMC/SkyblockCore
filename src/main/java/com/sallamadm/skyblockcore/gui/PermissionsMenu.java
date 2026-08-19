package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
import com.sallamadm.skyblockcore.island.enums.IslandRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionsMenu implements Listener {

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final String MENU_TITLE = ChatColor.DARK_PURPLE + "İzin Menüsü";

    public static void openPermissionsMenu(Player player) {
        Island island = SkyblockCore.getInstance().getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(msg.getMessage("island.no-island"));
            return;
        }

        if (!player.getUniqueId().equals(island.getOwnerUUID())) {
            player.sendMessage(msg.getMessage("general.no-permission"));
            return;
        }

        openPermissionsMenu(player, IslandRole.OWNER);
    }

    public static void openPermissionsMenu(Player viewer, IslandRole role) {
        Island island = SkyblockCore.getInstance().getIslandManager().getIsland(viewer.getUniqueId());
        if (island == null) {
            viewer.sendMessage(msg.getMessage("island.no-island"));
            return;
        }

        if (!viewer.getUniqueId().equals(island.getOwnerUUID())) {
            viewer.sendMessage(msg.getMessage("general.no-permission"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE + " - " + getRoleColor(role) + role.getDisplayName());

        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = blackGlass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            blackGlass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, blackGlass);
        }

        Set<String> currentPermissions = island.getPermissionsForTier(role.getTier());

        List<PermissionItem> permissionItems = getPermissionItems();

        int slot = 0;
        for (PermissionItem item : permissionItems) {
            ItemStack permissionItem = new ItemStack(item.getMaterial());
            ItemMeta meta = permissionItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(item.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Açıklama: " + item.getDescription());
                lore.add(""); // Empty line
                lore.add(ChatColor.YELLOW + "Durum: " + (currentPermissions.contains(item.getPermission().getNode()) ?
                        ChatColor.GREEN + "AKTİF" : ChatColor.RED + "PASİF"));
                lore.add(ChatColor.GRAY + "Tıklayarak izin değiştirin");
                meta.setLore(lore);
                permissionItem.setItemMeta(meta);
            }
            inv.setItem(slot, permissionItem);
            slot++;
        }

        ItemStack prevRoleItem = new ItemStack(Material.PAPER);
        ItemMeta prevRoleMeta = prevRoleItem.getItemMeta();
        if (prevRoleMeta != null) {
            prevRoleMeta.setDisplayName(ChatColor.RED + "Önceki Rol");
            prevRoleMeta.setCustomModelData(1);
            prevRoleItem.setItemMeta(prevRoleMeta);
        }
        inv.setItem(45, prevRoleItem);

        ItemStack homeItem = new ItemStack(Material.PAPER);
        ItemMeta homeMeta = homeItem.getItemMeta();
        if (homeMeta != null) {
            homeMeta.setDisplayName(ChatColor.YELLOW + "Ana Menü");
            homeMeta.setCustomModelData(2);
            homeItem.setItemMeta(homeMeta);
        }
        inv.setItem(49, homeItem);

        ItemStack nextRoleItem = new ItemStack(Material.PAPER);
        ItemMeta nextRoleMeta = nextRoleItem.getItemMeta();
        if (nextRoleMeta != null) {
            nextRoleMeta.setDisplayName(ChatColor.GREEN + "Sonraki Rol");
            nextRoleMeta.setCustomModelData(3);
            nextRoleItem.setItemMeta(nextRoleMeta);
        }
        inv.setItem(53, nextRoleItem);

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

        if (!viewer.getUniqueId().equals(island.getOwnerUUID())) {
            viewer.sendMessage(msg.getMessage("general.no-permission"));
            viewer.closeInventory();
            return;
        }

        if (slot == 45 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 1) {
                // Previous role
                IslandRole currentRole = getRoleFromTitle(title);
                IslandRole[] roles = IslandRole.values();
                int currentIndex = -1;
                for (int i = 0; i < roles.length; i++) {
                    if (roles[i] == currentRole) {
                        currentIndex = i;
                        break;
                    }
                }
                if (currentIndex > 0) {
                    IslandRole prevRole = roles[currentIndex - 1];
                    openPermissionsMenu(viewer, prevRole);
                }
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
                IslandRole currentRole = getRoleFromTitle(title);
                IslandRole[] roles = IslandRole.values();
                int currentIndex = -1;
                for (int i = 0; i < roles.length; i++) {
                    if (roles[i] == currentRole) {
                        currentIndex = i;
                        break;
                    }
                }
                if (currentIndex < roles.length - 1) {
                    IslandRole nextRole = roles[currentIndex + 1];
                    openPermissionsMenu(viewer, nextRole);
                }
                return;
            }
        }

        if (slot >= 0 && slot < 45) {
            List<PermissionItem> permissionItems = getPermissionItems();
            if (slot < permissionItems.size()) {
                PermissionItem item = permissionItems.get(slot);
                String permissionNode = item.getPermission().getNode();

                boolean currentlyHas = island.hasPermission(viewer.getUniqueId(), permissionNode);
                if (currentlyHas) {
                    island.revokePermission(viewer.getUniqueId().hashCode(), permissionNode);
                    viewer.sendMessage(msg.getMessage("island.permission-revoked").replace("{permission}", item.getDisplayName()));
                } else {
                    island.grantPermission(viewer.getUniqueId().hashCode(), permissionNode);
                    viewer.sendMessage(msg.getMessage("island.permission-granted").replace("{permission}", item.getDisplayName()));
                }

                IslandRole currentRole = getRoleFromTitle(title);
                openPermissionsMenu(viewer, currentRole);
            }
        }
    }

    private IslandRole getRoleFromTitle(String title) {
        if (title.contains(" - ")) {
            String rolePart = title.split(" - ")[1];
            String cleanRole = ChatColor.stripColor(rolePart);
            return IslandRole.valueOf(cleanRole.toUpperCase());
        }
        return IslandRole.OWNER;
    }

    private static List<PermissionItem> getPermissionItems() {
        List<PermissionItem> items = new ArrayList<>();

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_KICK,
                Material.BARRIER,
                ChatColor.RED + "Adadan Atma",
                "Oyuncuyu adadan atma izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_BAN,
                Material.ANVIL,
                ChatColor.DARK_RED + "Adaban",
                "Oyuncuyu adaya kalıcı olarak banlama izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_UNBAN,
                Material.HOPPER,
                ChatColor.GREEN + "Ban Kaldırma",
                "Oyuncunun ada bandını kaldırma izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_MANAGE_WARP,
                Material.COMPASS,
                ChatColor.YELLOW + "Warp Yönetimi",
                "Warpları oluşturma, silme, düzenleme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_BLOCK_BREAK,
                Material.DIAMOND_PICKAXE,
                ChatColor.AQUA + "Blok Kırma",
                "Ada sınırları içinde blok kırma izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_BLOCK_PLACE,
                Material.DIAMOND,
                ChatColor.AQUA + "Blok Koyma",
                "Ada sınırları içinde blok koyma izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_INTERACT,
                Material.EMERALD,
                ChatColor.GREEN + "Etkileşim",
                "Sınırlar içindeki öğelerle etkileşime geçme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_CHANGE_NAME,
                Material.NAME_TAG,
                ChatColor.LIGHT_PURPLE + "Adan Değiştirme",
                "Ada ismini değiştirme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ISLAND_SET_SPAWN,
                Material.RED_BED,
                ChatColor.DARK_BLUE + "Spawn Belirleme",
                "Ada spawn noktalarını ayarlama izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.MEMBERS_INVITE,
                Material.ARROW,
                ChatColor.YELLOW + "Davet Etme",
                "Oyuncuyu ada davet etme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.MEMBERS_KICK,
                Material.IRON_HOE,
                ChatColor.GOLD + "Üye Atma",
                "Adadan üye oyuncuyu atma izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.MEMBERS_BAN,
                Material.GOLDEN_HOE,
                ChatColor.GOLD + "Üye Ban",
                "Adadan üye oyuncuyu banlama izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.MEMBERS_ROLE_CHANGE,
                Material.CHAINMAIL_CHESTPLATE,
                ChatColor.DARK_PURPLE + "Üye Rol Değiştirme",
                "Adan üyenin rolünü değiştirme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.MEMBERS_MAKE_LEADER,
                Material.NETHER_STAR,
                ChatColor.RED + "Liderlik Transferi",
                "Ada liderliğini başka bir oyuncuya transfer etme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.COOP_ADD,
                Material.GOLDEN_APPLE,
                ChatColor.YELLOW + "Ek Coop Ekleme",
                "Yeni coop oyuncuları ekleme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.COOP_REMOVE,
                Material.APPLE,
                ChatColor.DARK_GREEN + "Ek Coop Kaldirma",
                "Mevcut coop oyuncularını kaldırma izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.WARP_CREATE,
                Material.FEATHER,
                ChatColor.LIGHT_PURPLE + "Warp Oluşturma",
                "Yeni warplar oluşturma izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.WARP_DELETE,
                Material.ROTTEN_FLESH,
                ChatColor.DARK_PURPLE + "Warp Silme",
                "Mevcut warpları silme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.WARP_RENAME,
                Material.PAPER,
                ChatColor.YELLOW + "Warp İsim Değiştirme",
                "Warp isimlerini değiştirme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.WARP_LOCATION_CHANGE,
                Material.COMPASS,
                ChatColor.AQUA + "Warp Konum Değiştirme",
                "Warp konumlarını değiştirme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.WARP_ICON_CHANGE,
                Material.PAINTING,
                ChatColor.LIGHT_PURPLE + "Warp İkon Değiştirme",
                "Warp ikonlarını değiştirme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.WARP_VISIBILITY_TOGGLE,
                Material.LIME_DYE,
                ChatColor.GREEN + "Warp Görünürlük",
                "Warp'ın ziyaretçilere görünürlüğünü değiştirme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.BIOME_CHANGE,
                Material.GRASS_BLOCK,
                ChatColor.DARK_GREEN + "Biyom Değiştirme",
                "Ada biyomunu değiştirme izni"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ADMIN_SET_LEVEL,
                Material.ENCHANTED_BOOK,
                ChatColor.DARK_PURPLE + "Admin Level Ayarla",
                "Oyuncu ada levelini ayarlama izni (Admin only)"
        ));

        items.add(new PermissionItem(
                IslandPermissions.ADMIN_ADD_LEVEL,
                Material.ENCHANTED_GOLDEN_APPLE,
                ChatColor.LIGHT_PURPLE + "Admin Level Ekle",
                "Oyuncu ada leveline ekleme izni (Admin only)"
        ));

        return items;
    }

    private static class PermissionItem {
        private final IslandPermissions permission;
        private final Material material;
        private final String displayName;
        private final String description;

        public PermissionItem(IslandPermissions permission, Material material, String displayName, String description) {
            this.permission = permission;
            this.material = material;
            this.displayName = displayName;
            this.description = description;
        }

        public IslandPermissions getPermission() {
            return permission;
        }

        public Material getMaterial() {
            return material;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
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