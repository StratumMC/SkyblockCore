package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.gui.util.GuiUtils;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandGamerules;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
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

public class GameruleMenu implements Listener {

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final String MENU_PREFIX = ChatColor.DARK_GRAY + "Ada Gamerule Ayarları";
    private static final int RULES_PER_PAGE = 45;

    private static final Map<UUID, Integer> CURRENT_PAGE = new HashMap<>();

    public static void openGameruleMenu(Player player, Island island) {
        openGameruleMenu(player, island, 1);
    }

    public static void openGameruleMenu(Player player, Island island, int page) {
        List<IslandGamerules> rules = Arrays.asList(IslandGamerules.values());
        int totalPages = Math.max(1, (int) Math.ceil((double) rules.size() / RULES_PER_PAGE));
        page = clampPage(page, totalPages);

        Inventory inv = Bukkit.createInventory(null, 54, MENU_PREFIX + " (" + page + "/" + totalPages + ")");

        int start = (page - 1) * RULES_PER_PAGE;
        int end = Math.min(start + RULES_PER_PAGE, rules.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            IslandGamerules rule = rules.get(i);
            boolean enabled = island.getGamerule(rule.getNode());
            inv.setItem(slot++, createRuleItem(rule, enabled));
        }

        GuiUtils.applyNavigationBar(inv, page, totalPages);
        addWeatherButton(inv);
        CURRENT_PAGE.put(player.getUniqueId(), page);
        player.openInventory(inv);
    }

    private static ItemStack createRuleItem(IslandGamerules rule, boolean enabled) {
        ItemStack item = new ItemStack(rule.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((enabled ? ChatColor.GREEN : ChatColor.RED)
                    + rule.getDisplayName() + ": " + (enabled ? "Aktif" : "Deaktif"));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Durum: " + (enabled ? ChatColor.GREEN + "Aktif" : ChatColor.RED + "Deaktif"));
            lore.add(ChatColor.YELLOW + "Açmak/kapatmak için tıklayın.");
            lore.add(ChatColor.DARK_GRAY + "Bu ayar sadece ada sınırlarınızı etkiler.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static int clampPage(int page, int totalPages) {
        if (page < 1) return 1;
        return Math.min(page, totalPages);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(MENU_PREFIX)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Island island = SkyblockCore.getInstance().getIslandManager().getIslandByMember(player.getUniqueId());
        if (island == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();
        int currentPage = CURRENT_PAGE.getOrDefault(player.getUniqueId(), 1);

        if (GuiUtils.isNavigationSlot(slot)) {
            if (slot == 49) {
                player.closeInventory();
                CURRENT_PAGE.remove(player.getUniqueId());
                IsMenu.openIsMenu(player);
            } else if (slot == 45) {
                openGameruleMenu(player, island, currentPage - 1);
            } else if (slot == 53) {
                openGameruleMenu(player, island, currentPage + 1);
            } else if (slot == 44) {
                player.closeInventory();
                CURRENT_PAGE.remove(player.getUniqueId());
                WeatherMenu.openWeatherMenu(player, island);
            }
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        if (!island.hasPermission(player.getUniqueId(), IslandPermissions.MANAGE_GAMERULES.getNode())) {
            player.sendMessage(msg.getMessage("general.no-permission"));
            return;
        }

        ItemMeta clickedMeta = event.getCurrentItem().getItemMeta();
        if (clickedMeta == null || clickedMeta.getDisplayName() == null) return;

        String cleanName = ChatColor.stripColor(clickedMeta.getDisplayName());
        IslandGamerules clickedRule = findByDisplayPrefix(cleanName);
        if (clickedRule == null) return;

        boolean currentlyEnabled = island.getGamerule(clickedRule.getNode());
        island.setGamerule(clickedRule.getNode(), !currentlyEnabled);

        player.sendMessage(msg.getMessage("gamerule.updated")
                .replace("{gamerule}", clickedRule.getDisplayName())
                .replace("{status}", currentlyEnabled ? "DEAKTİF" : "AKTİF"));

        openGameruleMenu(player, island, currentPage);
    }

    private static IslandGamerules findByDisplayPrefix(String cleanName) {
        for (IslandGamerules rule : IslandGamerules.values()) {
            if (cleanName.startsWith(rule.getDisplayName() + ":")) return rule;
        }
        return null;
    }

    private static void addWeatherButton(Inventory inv) {
        ItemStack weatherBtn = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = weatherBtn.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Hava Durumu Ayarları");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Ada hava durumu ayarlarını",
                    ChatColor.GRAY + "düzenlemek için tıklayın."
            ));
            weatherBtn.setItemMeta(meta);
        }
        inv.setItem(44, weatherBtn);
    }
}
