package com.sallamadm.skyblockcore.gui;
import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
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

    private static final String SORT_LEVEL = "level";
    private static final String SORT_RATING = "rating";
    private static final String SORT_LIKES = "likes";

    private static final String PERIOD_WEEKLY = "hafta";
    private static final String PERIOD_MONTHLY = "ay";
    private static final String PERIOD_ALL_TIME = "hepsi";

    private static final int ISLANDS_PER_PAGE = 25;

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

        fillPyramidLayout(inv, pageIslands, sortBy, likePeriod);
        fillSortButtons(inv, sortBy, likePeriod);
        GuiUtils.applyNavigationBar(inv, page, totalPages);

        UUID playerUUID = player.getUniqueId();
        CURRENT_PAGE.put(playerUUID, page);
        CURRENT_SORT.put(playerUUID, sortBy);
        CURRENT_LIKE_PERIOD.put(playerUUID, likePeriod);

        player.openInventory(inv);
    }
    private static void fillPyramidLayout(Inventory inv, List<IslandRankData> islands, String sortBy, String likePeriod) {
        int islandIndex = 0;

        if (islandIndex < islands.size()) {
            inv.setItem(4, createIslandHead(islands.get(islandIndex++), sortBy, likePeriod));
        }

        for (int slot : new int[]{3, 4, 5}) {
            if (islandIndex < islands.size()) {
                inv.setItem(9 + slot, createIslandHead(islands.get(islandIndex++), sortBy, likePeriod));
            }
        }

        for (int slot : new int[]{2, 3, 4, 5, 6}) {
            if (islandIndex < islands.size()) {
                inv.setItem(18 + slot, createIslandHead(islands.get(islandIndex++), sortBy, likePeriod));
            }
        }

        for (int slot : new int[]{1, 2, 3, 4, 5, 6, 7}) {
            if (islandIndex < islands.size()) {
                inv.setItem(27 + slot, createIslandHead(islands.get(islandIndex++), sortBy, likePeriod));
            }
        }

        for (int slot : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}) {
            if (islandIndex < islands.size()) {
                inv.setItem(36 + slot, createIslandHead(islands.get(islandIndex++), sortBy, likePeriod));
            }
        }
    }
    private static ItemStack createIslandHead(IslandRankData data, String sortBy, String likePeriod) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null && data.island != null && data.island.getOwnerUUID() != null) {
            @SuppressWarnings("deprecation")
            OfflinePlayer owner = Bukkit.getOfflinePlayer(data.island.getOwnerUUID());
            meta.setOwningPlayer(owner);

            meta.setDisplayName(ChatColor.GOLD + "#" + data.rank + ChatColor.RESET + " " + ChatColor.YELLOW + data.island.getIslandName());

            List<String> lore = new ArrayList<>();
            lore.add(" ");

            if (sortBy.equals(SORT_LEVEL)) {
                lore.add(ChatColor.AQUA + "Ada Leveli: " + ChatColor.WHITE + String.format("%.2f", data.island.getLevel()));
            } else if (sortBy.equals(SORT_RATING)) {
                lore.add(ChatColor.AQUA + "Rating: " + ChatColor.WHITE + String.format("%.2f", data.value));
            } else if (sortBy.equals(SORT_LIKES)) {
                lore.add(ChatColor.AQUA + "Like:  (" + likePeriod + "): " + ChatColor.WHITE + (int) data.value);
            }

            lore.add(" ");
            lore.add(ChatColor.GRAY + "Warp menüsü açmak için tıklayın.");

            meta.setLore(lore);
            head.setItemMeta(meta);
        }

        return head;
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
        String currentSort = CURRENT_SORT.getOrDefault(player.getUniqueId(), SORT_LEVEL);
        String likePeriod = CURRENT_LIKE_PERIOD.getOrDefault(player.getUniqueId(), PERIOD_WEEKLY);
        int currentPage = CURRENT_PAGE.getOrDefault(player.getUniqueId(), 1);

        if (GuiUtils.isNavigationSlot(slot)) {
            if (slot == 49) {
                player.closeInventory();
                IsMenu.openIsMenu(player);
                CURRENT_PAGE.remove(player.getUniqueId());
                CURRENT_SORT.remove(player.getUniqueId());
                CURRENT_LIKE_PERIOD.remove(player.getUniqueId());
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

        if (slot >= 0 && slot <= 44) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta() != null) {
                String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                String islandName = displayName.replaceAll("#\\d+\\s+", "").trim();

                Island targetIsland = null;
                for (Island island : SkyblockCore.getInstance().getIslandManager().getAllIslands().values()) {
                    if (island.getIslandName().equalsIgnoreCase(islandName)) {
                        targetIsland = island;
                        break;
                    }
                }

                if (targetIsland != null && targetIsland.getOwnerUUID() != null) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(targetIsland.getOwnerUUID());
                    if (owner != null && owner.getName() != null) {
                        player.closeInventory();
                        WarpMenu.openVisitorWarpMenu(player, owner.getName());
                        CURRENT_PAGE.remove(player.getUniqueId());
                        CURRENT_SORT.remove(player.getUniqueId());
                        CURRENT_LIKE_PERIOD.remove(player.getUniqueId());
                    }
                }
            }
        }
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