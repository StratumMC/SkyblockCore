package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.gui.util.GuiUtils;
import com.sallamadm.skyblockcore.island.Island;
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

    private static final String MENU_TITLE = ChatColor.DARK_AQUA + "Ada Üyeleri";

    private static final int PERMISSIONS_SIGN_SLOT = 44;
    private static final int MEMBERS_PER_PAGE = 44;

    private static final List<IslandRole> CYCLE_ROLES = Arrays.asList(IslandRole.MEMBER, IslandRole.MOD, IslandRole.ADMIN);

    private static final Map<UUID, Integer> CURRENT_PAGE = new HashMap<>();

    public static void openMembersMenu(Player viewer, Island island) {
        openMembersMenu(viewer, island, 1);
    }

    public static void openMembersMenu(Player viewer, Island island, int page) {
        List<UUID> displayList = buildDisplayList(island);

        int totalPages = Math.max(1, (int) Math.ceil((double) displayList.size() / MEMBERS_PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        int start = (page - 1) * MEMBERS_PER_PAGE;
        int end = Math.min(start + MEMBERS_PER_PAGE, displayList.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            inv.setItem(slot++, createMemberHead(island, viewer, displayList.get(i)));
        }

        inv.setItem(PERMISSIONS_SIGN_SLOT, createPermissionsSignItem());

        GuiUtils.applyNavigationBar(inv, page, totalPages);
        CURRENT_PAGE.put(viewer.getUniqueId(), page);
        viewer.openInventory(inv);
    }

    private static List<UUID> buildDisplayList(Island island) {
        List<UUID> list = new ArrayList<>();
        list.add(island.getOwnerUUID());
        list.addAll(island.getMemberRoles().keySet());

        list.sort(Comparator.comparingInt((UUID u) -> island.getRoleTier(u))
                .thenComparing(u -> {
                    String name = Bukkit.getOfflinePlayer(u).getName();
                    return name != null ? name : "";
                }));
        return list;
    }

    private static ItemStack createMemberHead(Island island, Player viewer, UUID memberUuid) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(memberUuid);
        IslandRole targetRole = IslandRole.fromTier(island.getRoleTier(memberUuid));
        int actorTier = island.getRoleTier(viewer.getUniqueId());

        boolean isOwner = targetRole == IslandRole.OWNER;
        boolean cyclable = CYCLE_ROLES.contains(targetRole);
        boolean editable = !isOwner && cyclable && targetRole.getTier() > actorTier;

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(target);
            String playerName = target.getName() != null ? target.getName() : "Bilinmeyen";
            meta.setDisplayName(roleColor(targetRole) + playerName);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Rol: " + roleColor(targetRole) + targetRole.getDisplayName());

            if (isOwner) {
                lore.add(ChatColor.DARK_GRAY + "Ada sahibinin rolü değiştirilemez.");
            } else if (!cyclable) {
                lore.add(ChatColor.DARK_GRAY + "Bu rol buradan değiştirilemez. (/is coop kullanın)");
            } else if (editable) {
                lore.add(ChatColor.YELLOW + "Rolü artırmak için tıklayın.");
            } else {
                lore.add(ChatColor.RED + "Bu oyuncunun rolünü değiştirme yetkiniz yok.");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createPermissionsSignItem() {
        ItemStack item = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Yetkileri Düzenle");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Rol yetkilerini düzenlemek için tıklayın."));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ChatColor roleColor(IslandRole role) {
        return switch (role) {
            case OWNER -> ChatColor.GOLD;
            case ADMIN -> ChatColor.RED;
            case MOD -> ChatColor.LIGHT_PURPLE;
            case MEMBER -> ChatColor.AQUA;
            case COOP -> ChatColor.YELLOW;
            case VISITOR -> ChatColor.GRAY;
        };
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        Island island = SkyblockCore.getInstance().getIslandManager().getIslandByMember(viewer.getUniqueId());
        if (island == null) return;

        int slot = event.getSlot();

        if (GuiUtils.isNavigationSlot(slot)) {
            if (slot == 49) {
                viewer.closeInventory();
                CURRENT_PAGE.remove(viewer.getUniqueId());
                IsMenu.openIsMenu(viewer);
                return;
            }
            int current = CURRENT_PAGE.getOrDefault(viewer.getUniqueId(), 1);
            if (slot == 45) {
                openMembersMenu(viewer, island, current - 1);
            } else if (slot == 53) {
                openMembersMenu(viewer, island, current + 1);
            }
            return;
        }

        if (slot == PERMISSIONS_SIGN_SLOT) {
            viewer.closeInventory();
            CURRENT_PAGE.remove(viewer.getUniqueId());
            PermissionsMenu.openRoleSelectMenu(viewer, island);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;
        if (!(clicked.getItemMeta() instanceof SkullMeta skullMeta) || skullMeta.getOwningPlayer() == null) return;

        UUID targetUuid = skullMeta.getOwningPlayer().getUniqueId();
        handleRoleCycle(viewer, island, targetUuid);
    }

    private void handleRoleCycle(Player viewer, Island island, UUID targetUuid) {
        if (targetUuid.equals(island.getOwnerUUID())) {
            viewer.sendMessage(msg.getMessage("members.owner-cannot-change"));
            return;
        }

        int actorTier = island.getRoleTier(viewer.getUniqueId());
        int targetTier = island.getRoleTier(targetUuid);
        IslandRole targetRole = IslandRole.fromTier(targetTier);

        if (!CYCLE_ROLES.contains(targetRole)) {
            viewer.sendMessage(msg.getMessage("members.cannot-edit-coop"));
            return;
        }

        if (targetTier <= actorTier) {
            viewer.sendMessage(msg.getMessage("general.no-permission"));
            return;
        }

        List<IslandRole> cycleOrder = new ArrayList<>();
        for (IslandRole role : CYCLE_ROLES) {
            if (role.getTier() > actorTier) cycleOrder.add(role);
        }

        int currentIndex = cycleOrder.indexOf(targetRole);
        if (currentIndex == -1) {
            viewer.sendMessage(msg.getMessage("general.no-permission"));
            return;
        }

        IslandRole nextRole = cycleOrder.get((currentIndex + 1) % cycleOrder.size());
        island.addOrUpdateMember(targetUuid, nextRole, null);

        String targetName = Bukkit.getOfflinePlayer(targetUuid).getName();
        viewer.sendMessage(msg.getMessage("members.role-changed")
                .replace("{target}", targetName != null ? targetName : "Oyuncu")
                .replace("{role}", nextRole.getDisplayName()));

        Player targetPlayer = Bukkit.getPlayer(targetUuid);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(msg.getMessage("members.role-changed-notify")
                    .replace("{role}", nextRole.getDisplayName()));
        }

        int page = CURRENT_PAGE.getOrDefault(viewer.getUniqueId(), 1);
        openMembersMenu(viewer, island, page);
    }
}