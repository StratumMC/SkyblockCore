package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.gui.util.GuiUtils;
import com.sallamadm.skyblockcore.island.Island;
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

public class IsTopMenu implements Listener {

    private static final String MENU_TITLE = ChatColor.GOLD + "Ada Sıralaması";

    private static final Map<UUID, Integer> CURRENT_PAGE = new HashMap<>();
    private static final Map<UUID, String> CURRENT_SORT = new HashMap<>();
    private static final Map<UUID, String> CURRENT_LIKE_PERIOD = new HashMap<>();

    private static final Map<UUID, Map<Integer, Island>> SLOT_ISLANDS = new HashMap<>();

    private static final String SORT_LEVEL = "level";
    private static final String SORT_RATING = "rating";
    private static final String SORT_LIKES = "likes";

    private static final String PERIOD_WEEKLY = "hafta";
    private static final String PERIOD_MONTHLY = "ay";
    private static final String PERIOD_ALL_TIME = "hepsi";

    private static final int ISLANDS_PER_PAGE = 16;

    private static final int SORT_LEVEL_SLOT = 37;
    private static final int SORT_RATING_SLOT = 40;
    private static final int SORT_LIKES_SLOT = 43;

    public static void openTopMenu(Player player) {
        openTopMenu(player, SORT_LEVEL, PERIOD_WEEKLY, 1);
    }

    public static void openTopMenu(Player player, String sortBy, String likePeriod, int page) {
        SkyblockCore plugin = SkyblockCore.getInstance();

        List<IslandRankData> rankedIslands = getRankedIslands(plugin, sortBy, likePeriod);

        if (rankedIslands.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Ada sıralaması boş.");
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) rankedIslands.size() / ISLANDS_PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));

        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        int startIdx = (page - 1) * ISLANDS_PER_PAGE;
        int endIdx = Math.min(startIdx + ISLANDS_PER_PAGE, rankedIslands.size());
        List<IslandRankData> pageIslands = rankedIslands.subList(startIdx, endIdx);

        UUID playerUUID = player.getUniqueId();
        Map<Integer, Island> slotMap = new HashMap<>();

        fillPyramidLayout(inv, pageIslands, sortBy, likePeriod, slotMap);
        fillSortButtons(inv, sortBy, likePeriod);
        GuiUtils.applyNavigationBar(inv, page, totalPages);

        CURRENT_PAGE.put(playerUUID, page);
        CURRENT_SORT.put(playerUUID, sortBy);
        CURRENT_LIKE_PERIOD.put(playerUUID, likePeriod);
        SLOT_ISLANDS.put(playerUUID, slotMap);

        player.openInventory(inv);

        for (Map.Entry<Integer, Island> entry : slotMap.entrySet()) {
            Island island = entry.getValue();
            if (island != null && island.getOwnerUUID() != null) {
                applyOwnerSkinAsync(plugin, inv, entry.getKey(), island.getOwnerUUID());
            }
        }
    }

    private static void fillPyramidLayout(Inventory inv, List<IslandRankData> islands, String sortBy, String likePeriod, Map<Integer, Island> slotMap) {
        int islandIndex = 0;

        if (islandIndex < islands.size()) {
            int slot = 4;
            IslandRankData data = islands.get(islandIndex++);
            inv.setItem(slot, createIslandHead(data, sortBy, likePeriod));
            slotMap.put(slot, data.island);
        }

        for (int offset : new int[]{3, 4, 5}) {
            if (islandIndex < islands.size()) {
                int slot = 9 + offset;
                IslandRankData data = islands.get(islandIndex++);
                inv.setItem(slot, createIslandHead(data, sortBy, likePeriod));
                slotMap.put(slot, data.island);
            }
        }

        for (int offset : new int[]{2, 3, 4, 5, 6}) {
            if (islandIndex < islands.size()) {
                int slot = 18 + offset;
                IslandRankData data = islands.get(islandIndex++);
                inv.setItem(slot, createIslandHead(data, sortBy, likePeriod));
                slotMap.put(slot, data.island);
            }
        }

        for (int offset : new int[]{1, 2, 3, 4, 5, 6, 7}) {
            if (islandIndex < islands.size()) {
                int slot = 27 + offset;
                IslandRankData data = islands.get(islandIndex++);
                inv.setItem(slot, createIslandHead(data, sortBy, likePeriod));
                slotMap.put(slot, data.island);
            }
        }
    }

    private static ItemStack createIslandHead(IslandRankData data, String sortBy, String likePeriod) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null && data.island != null) {
            meta.setDisplayName(ChatColor.GOLD + "#" + data.rank + ChatColor.RESET + " " + ChatColor.YELLOW + data.island.getIslandName());

            List<String> lore = new ArrayList<>();
            lore.add(" ");

            if (sortBy.equals(SORT_LEVEL)) {
                lore.add(ChatColor.AQUA + "Ada Leveli: " + ChatColor.WHITE + String.format("%.2f", data.island.getLevel()));
            } else if (sortBy.equals(SORT_RATING)) {
                lore.add(ChatColor.AQUA + "Rating: " + ChatColor.WHITE + String.format("%.2f", data.value));
            } else if (sortBy.equals(SORT_LIKES)) {
                lore.add(ChatColor.AQUA + "Like (" + likePeriod + "): " + ChatColor.WHITE + (int) data.value);
            }

            lore.add(" ");
            lore.add(ChatColor.GRAY + "Warp menüsü açmak için tıklayın.");

            meta.setLore(lore);
            head.setItemMeta(meta);
        }

        return head;
    }

    private static void applyOwnerSkinAsync(SkyblockCore plugin, Inventory inv, int slot, UUID ownerUuid) {
        Bukkit.createProfile(ownerUuid).update().thenAccept(profile ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    ItemStack current = inv.getItem(slot);
                    if (current == null || current.getType() != Material.PLAYER_HEAD) return;

                    ItemMeta rawMeta = current.getItemMeta();
                    if (!(rawMeta instanceof SkullMeta meta)) return;

                    meta.setOwnerProfile(profile);
                    current.setItemMeta(meta);
                    inv.setItem(slot, current);
                })
        );
    }

    private static void fillSortButtons(Inventory inv, String sortBy, String likePeriod) {
        ItemStack levelBtn = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta levelMeta = levelBtn.getItemMeta();
        if (levelMeta != null) {
            levelMeta.setDisplayName(sortBy.equals(SORT_LEVEL) ? ChatColor.GOLD + "Level (Aktif)" : ChatColor.GRAY + "Level");
            levelMeta.setLore(Arrays.asList(ChatColor.GRAY + "Level sıralaması", ChatColor.GRAY + "için tıklayın."));
            levelBtn.setItemMeta(levelMeta);
        }
        inv.setItem(SORT_LEVEL_SLOT, levelBtn);

        ItemStack ratingBtn = new ItemStack(Material.GOLD_INGOT);
        ItemMeta ratingMeta = ratingBtn.getItemMeta();
        if (ratingMeta != null) {
            ratingMeta.setDisplayName(sortBy.equals(SORT_RATING) ? ChatColor.GOLD + "Rating (Aktif)" : ChatColor.GRAY + "Rating");
            ratingMeta.setLore(Arrays.asList(ChatColor.GRAY + "Rating sıralaması", ChatColor.GRAY + "için tıklayın."));
            ratingBtn.setItemMeta(ratingMeta);
        }
        inv.setItem(SORT_RATING_SLOT, ratingBtn);

        ItemStack likesBtn = new ItemStack(Material.REDSTONE);
        ItemMeta likesMeta = likesBtn.getItemMeta();
        if (likesMeta != null) {
            String likeBtnLabel = sortBy.equals(SORT_LIKES)
                    ? ChatColor.GOLD + "Beğeni (Aktif)"
                    : ChatColor.GRAY + "Beğeni";
            likesMeta.setDisplayName(likeBtnLabel);
            List<String> likesList = Arrays.asList(
                    ChatColor.GRAY + "Beğeni sıralaması",
                    ChatColor.DARK_GRAY + "Dönem: " + ChatColor.GRAY + likePeriod,
                    ChatColor.GRAY + "için tıklayın."
            );
            likesMeta.setLore(likesList);
            likesBtn.setItemMeta(likesMeta);
        }
        inv.setItem(SORT_LIKES_SLOT, likesBtn);
    }

    private static List<IslandRankData> getRankedIslands(SkyblockCore plugin, String sortBy, String likePeriod) {
        List<IslandRankData> rankedIslands = new ArrayList<>();
        Map<UUID, Island> allIslands = plugin.getIslandManager().getAllIslands();

        if (sortBy.equals(SORT_LEVEL)) {
            Map<String, Double> topLevels = plugin.getDataManager().getTopLeveledIslands();
            int rank = 1;
            for (Map.Entry<String, Double> entry : topLevels.entrySet()) {
                Island island = findIslandByUuid(allIslands, entry.getKey());
                if (island != null) {
                    rankedIslands.add(new IslandRankData(rank++, island, entry.getValue()));
                }
            }
        } else if (sortBy.equals(SORT_RATING)) {
            Map<String, Double> topRatings = plugin.getDataManager().getTopRatedIslands();
            int rank = 1;
            for (Map.Entry<String, Double> entry : topRatings.entrySet()) {
                Island island = findIslandByUuid(allIslands, entry.getKey());
                if (island != null) {
                    rankedIslands.add(new IslandRankData(rank++, island, entry.getValue()));
                }
            }
        } else if (sortBy.equals(SORT_LIKES)) {
            Map<String, Integer> topLikes = plugin.getDataManager().getTopLikedIslands(likePeriod);
            int rank = 1;
            for (Map.Entry<String, Integer> entry : topLikes.entrySet()) {
                Island island = findIslandByUuid(allIslands, entry.getKey());
                if (island != null) {
                    rankedIslands.add(new IslandRankData(rank++, island, entry.getValue()));
                }
            }
        }

        return rankedIslands;
    }

    private static Island findIslandByUuid(Map<UUID, Island> allIslands, String uuid) {
        for (Island island : allIslands.values()) {
            if (island.getIslandUuid().equals(uuid)) {
                return island;
            }
        }
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        UUID playerUUID = player.getUniqueId();
        String currentSort = CURRENT_SORT.getOrDefault(playerUUID, SORT_LEVEL);
        String likePeriod = CURRENT_LIKE_PERIOD.getOrDefault(playerUUID, PERIOD_WEEKLY);
        int currentPage = CURRENT_PAGE.getOrDefault(playerUUID, 1);

        if (GuiUtils.isNavigationSlot(slot)) {
            if (slot == 49) {
                player.closeInventory();
                IsMenu.openIsMenu(player);
                clearPlayerState(playerUUID);
                return;
            }

            if (slot == 45) {
                if (currentPage > 1) {
                    openTopMenu(player, currentSort, likePeriod, currentPage - 1);
                }
                return;
            }

            if (slot == 53) {
                List<IslandRankData> allIslands = getRankedIslands(SkyblockCore.getInstance(), currentSort, likePeriod);
                int totalPages = Math.max(1, (int) Math.ceil((double) allIslands.size() / ISLANDS_PER_PAGE));
                if (currentPage < totalPages) {
                    openTopMenu(player, currentSort, likePeriod, currentPage + 1);
                }
                return;
            }
            return;
        }

        if (slot == SORT_LEVEL_SLOT) {
            openTopMenu(player, SORT_LEVEL, likePeriod, 1);
            return;
        }

        if (slot == SORT_RATING_SLOT) {
            openTopMenu(player, SORT_RATING, likePeriod, 1);
            return;
        }

        if (slot == SORT_LIKES_SLOT) {
            String nextPeriod = likePeriod.equals(PERIOD_WEEKLY) ? PERIOD_MONTHLY
                    : likePeriod.equals(PERIOD_MONTHLY) ? PERIOD_ALL_TIME
                    : PERIOD_WEEKLY;
            openTopMenu(player, SORT_LIKES, nextPeriod, 1);
            return;
        }

        Map<Integer, Island> slotMap = SLOT_ISLANDS.get(playerUUID);
        if (slotMap == null) return;

        Island targetIsland = slotMap.get(slot);
        if (targetIsland == null || targetIsland.getOwnerUUID() == null) return;

        OfflinePlayer owner = Bukkit.getOfflinePlayer(targetIsland.getOwnerUUID());
        String ownerName = owner.getName();
        if (ownerName == null) return;

        player.closeInventory();
        WarpMenu.openVisitorWarpMenu(player, ownerName);
        clearPlayerState(playerUUID);
    }

    private static void clearPlayerState(UUID playerUUID) {
        CURRENT_PAGE.remove(playerUUID);
        CURRENT_SORT.remove(playerUUID);
        CURRENT_LIKE_PERIOD.remove(playerUUID);
        SLOT_ISLANDS.remove(playerUUID);
    }

    private static class IslandRankData {
        int rank;
        Island island;
        double value;
        IslandRankData(int rank, Island island, double value) {
            this.rank = rank;
            this.island = island;
            this.value = value;
        }
    }
}