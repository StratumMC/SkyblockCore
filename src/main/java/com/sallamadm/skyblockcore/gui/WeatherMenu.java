package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.gui.util.GuiUtils;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
import com.sallamadm.skyblockcore.island.enums.IslandWeather;
import com.sallamadm.skyblockcore.island.IslandWeatherManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WeatherMenu implements Listener {

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final String MENU_PREFIX = ChatColor.DARK_GRAY + "Ada Hava Durumu";
    private static final int OPTIONS_PER_PAGE = 45;

    private static final Map<UUID, Integer> CURRENT_PAGE = new HashMap<>();

    public static void openWeatherMenu(Player player, Island island) {
        openWeatherMenu(player, island, 1);
    }

    public static void openWeatherMenu(Player player, Island island, int page) {
        List<IslandWeather> options = Arrays.asList(IslandWeather.values());
        int totalPages = Math.max(1, (int) Math.ceil((double) options.size() / OPTIONS_PER_PAGE));
        page = clampPage(page, totalPages);

        Inventory inv = Bukkit.createInventory(null, 54, MENU_PREFIX + " (" + page + "/" + totalPages + ")");

        String currentNode = island.getWeatherOption();

        int start = (page - 1) * OPTIONS_PER_PAGE;
        int end = Math.min(start + OPTIONS_PER_PAGE, options.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            IslandWeather option = options.get(i);
            boolean selected = option.getNode().equalsIgnoreCase(currentNode);
            inv.setItem(slot++, createWeatherItem(option, selected));
        }

        GuiUtils.applyNavigationBar(inv, page, totalPages);
        CURRENT_PAGE.put(player.getUniqueId(), page);
        player.openInventory(inv);
    }

    private static ItemStack createWeatherItem(IslandWeather option, boolean selected) {
        ItemStack item = new ItemStack(option.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((selected ? ChatColor.GREEN : ChatColor.YELLOW) + option.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add(selected ? ChatColor.GREEN + "✔ Şu anda seçili" : ChatColor.GRAY + "Seçmek için tıkla");
            if (option == IslandWeather.NORMAL) {
                lore.add(ChatColor.DARK_GRAY + "Sunucunun gerçek zaman/hava durumunu kullanır.");
            } else if (option.isThunder()) {
                lore.add(ChatColor.DARK_GRAY + "Yıldırım ve yağmur.");
            }
            lore.add(ChatColor.DARK_GRAY + "Bu ayar sadece ada sınırlarınızı etkiler.");
            meta.setLore(lore);

            if (selected) {
                meta.addEnchant(Enchantment.LUCK, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

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
                openWeatherMenu(player, island, currentPage - 1);
            } else if (slot == 53) {
                openWeatherMenu(player, island, currentPage + 1);
            }
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        if (!island.hasPermission(player.getUniqueId(), IslandPermissions.MANAGE_WEATHER.getNode())) {
            player.sendMessage(msg.getMessage("general.no-permission"));
            return;
        }

        ItemMeta clickedMeta = event.getCurrentItem().getItemMeta();
        if (clickedMeta == null || clickedMeta.getDisplayName() == null) return;

        String cleanName = ChatColor.stripColor(clickedMeta.getDisplayName());
        IslandWeather clickedOption = findByDisplayName(cleanName);
        if (clickedOption == null) return;

        island.setWeatherOption(clickedOption.getNode());
        IslandWeatherManager.refreshIslandPlayers(SkyblockCore.getInstance(), island);

        player.sendMessage(msg.getMessage("weather.updated").replace("{weather}", clickedOption.getDisplayName()));

        openWeatherMenu(player, island, currentPage);
    }

    private static IslandWeather findByDisplayName(String cleanName) {
        for (IslandWeather option : IslandWeather.values()) {
            if (cleanName.equals(option.getDisplayName())) return option;
        }
        return null;
    }
}
