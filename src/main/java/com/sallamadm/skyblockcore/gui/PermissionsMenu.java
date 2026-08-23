package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.gui.util.GuiUtils;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
import com.sallamadm.skyblockcore.island.enums.IslandRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.*;

public class PermissionsMenu implements Listener {

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final String ROLE_SELECT_TITLE = ChatColor.DARK_GRAY + "Yetki Yönetimi: Rol Seç";
    private static final String PERMISSION_EDIT_PREFIX = ChatColor.DARK_GRAY + "Yetkiler» ";

    private static final Map<Integer, IslandRole> ROLE_SLOTS = new LinkedHashMap<>();
    static {
        ROLE_SLOTS.put(11, IslandRole.ADMIN);
        ROLE_SLOTS.put(13, IslandRole.MOD);
        ROLE_SLOTS.put(15, IslandRole.MEMBER);
        ROLE_SLOTS.put(29, IslandRole.COOP);
        ROLE_SLOTS.put(31, IslandRole.VISITOR);
    }

    public static void openRoleSelectMenu(Player actor, Island island) {
        int actorTier = island.getRoleTier(actor.getUniqueId());

        if (actorTier > IslandRole.ADMIN.getTier()) {
            actor.sendMessage(msg.getMessage("general.no-permission"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, ROLE_SELECT_TITLE);

        for (Map.Entry<Integer, IslandRole> entry : ROLE_SLOTS.entrySet()) {
            IslandRole role = entry.getValue();

            boolean editable = role.getTier() > actorTier;

            ItemStack item = new ItemStack(editable ? Material.PLAYER_HEAD : Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((editable ? ChatColor.AQUA : ChatColor.DARK_GRAY) + role.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Tier: " + role.getTier());
                lore.add(editable ? ChatColor.YELLOW + "Yetkilerini düzenlemek için tıklayın."
                        : ChatColor.RED + "Bu rolü düzenleme yetkiniz yok.");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(entry.getKey(), item);
        }

        GuiUtils.applyNavigationBar(inv, 1, 1);
        actor.openInventory(inv);
    }

    public static void openPermissionEditMenu(Player actor, Island island, IslandRole targetRole) {
        Inventory inv = Bukkit.createInventory(null, 54, PERMISSION_EDIT_PREFIX + targetRole.name());

        int slot = 0;
        for (IslandPermissions perm : IslandPermissions.values()) {
            if (slot >= 45) break;

            boolean granted = island.hasPermission(targetRole.getTier(), perm.getNode());

            ItemStack item = new ItemStack(perm.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((granted ? ChatColor.GREEN : ChatColor.RED) + perm.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Durum: " + (granted ? ChatColor.GREEN + "Açık" : ChatColor.RED + "Kapalı"));
                lore.add(ChatColor.YELLOW + "Açmak/kapatmak için tıklayın.");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        GuiUtils.applyNavigationBar(inv, 1, 1);
        actor.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(ROLE_SELECT_TITLE) && !title.startsWith(PERMISSION_EDIT_PREFIX)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getSlot();
        Island island = SkyblockCore.getInstance().getIslandManager().getIslandByMember(player.getUniqueId());
        if (island == null) return;

        if (GuiUtils.isNavigationSlot(slot)) {
            if (slot == 49) {
                player.closeInventory();
                IsMenu.openIsMenu(player);
            }
            return;
        }

        if (title.equals(ROLE_SELECT_TITLE)) {
            IslandRole clickedRole = ROLE_SLOTS.get(slot);
            if (clickedRole == null) return;

            int actorTier = island.getRoleTier(player.getUniqueId());
            if (clickedRole.getTier() <= actorTier) {
                player.sendMessage(msg.getMessage("general.no-permission"));
                return;
            }

            openPermissionEditMenu(player, island, clickedRole);
            return;
        }

        if (title.startsWith(PERMISSION_EDIT_PREFIX)) {
            String roleName = title.replace(PERMISSION_EDIT_PREFIX, "");
            IslandRole targetRole;
            try {
                targetRole = IslandRole.valueOf(roleName);
            } catch (IllegalArgumentException ex) {
                return;
            }

            ItemMeta clickedMeta = event.getCurrentItem().getItemMeta();
            if (clickedMeta == null || clickedMeta.getDisplayName() == null) return;

            String cleanName = ChatColor.stripColor(clickedMeta.getDisplayName());
            IslandPermissions perm = findByDisplayName(cleanName);
            if (perm == null) return;

            int actorTier = island.getRoleTier(player.getUniqueId());

            boolean actorIsOwner = actorTier == IslandRole.OWNER.getTier();
            if (!actorIsOwner && !island.hasPermission(actorTier, perm.getNode())) {
                player.sendMessage(msg.getMessage("permissions.cannot-grant-unowned"));
                return;
            }
            if (targetRole.getTier() <= actorTier) {
                player.sendMessage(msg.getMessage("general.no-permission"));
                return;
            }

            boolean currentlyGranted = island.hasPermission(targetRole.getTier(), perm.getNode());
            if (currentlyGranted) {
                island.revokePermission(targetRole.getTier(), perm.getNode());
            } else {
                island.grantPermission(targetRole.getTier(), perm.getNode());
            }

            player.sendMessage(msg.getMessage("permissions.updated")
                    .replace("{role}", targetRole.getDisplayName())
                    .replace("{permission}", perm.getDisplayName())
                    .replace("{status}", currentlyGranted ? "KAPALI" : "AÇIK"));

            openPermissionEditMenu(player, island, targetRole);
        }
    }

    private static IslandPermissions findByDisplayName(String cleanName) {
        for (IslandPermissions perm : IslandPermissions.values()) {
            if (perm.getDisplayName().equals(cleanName)) return perm;
        }
        return null;
    }
}