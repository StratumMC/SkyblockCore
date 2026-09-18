package com.sallamadm.skyblockcore.gui;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.api.EconomyProvider;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IsBankMenu implements Listener {

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    public static final String MENU_TITLE = ChatColor.DARK_GRAY + "Ada Bankası";

    private static final Set<Integer> DEPOSIT_SLOTS = Set.of(8, 17, 26);
    private static final Set<Integer> WITHDRAW_SLOTS = Set.of(0, 9, 18);
    private static final int BALANCE_SLOT = 13;

    private static final long PROMPT_TIMEOUT_MILLIS = 30_000L;

    private enum BankActionType {
        DEPOSIT,
        WITHDRAW
    }

    private static final class PendingBankAction {
        private final UUID islandOwnerUuid;
        private final BankActionType type;
        private final long timestamp;

        private PendingBankAction(UUID islandOwnerUuid, BankActionType type) {
            this.islandOwnerUuid = islandOwnerUuid;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        private boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > PROMPT_TIMEOUT_MILLIS;
        }
    }

    private static final Map<UUID, PendingBankAction> PENDING_ACTIONS = new HashMap<>();

    // ---- GUI acma ----

    public static void openBankMenu(Player player, Island island) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        ItemStack filler = createFiller();
        for (int slot = 0; slot < 27; slot++) {
            inv.setItem(slot, filler);
        }

        for (int slot : DEPOSIT_SLOTS) {
            inv.setItem(slot, createActionItem(Material.LIME_STAINED_GLASS_PANE,
                    ChatColor.GREEN + "Para Yatır",
                    ChatColor.GRAY + "Ada bankasına para yatırmak için tıklayın."));
        }

        for (int slot : WITHDRAW_SLOTS) {
            inv.setItem(slot, createActionItem(Material.RED_STAINED_GLASS_PANE,
                    ChatColor.RED + "Para Çek",
                    ChatColor.GRAY + "Ada bankasından para çekmek için tıklayın."));
        }

        inv.setItem(BALANCE_SLOT, createBalanceItem(island.getBalance()));

        player.openInventory(inv);
    }

    private static ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createActionItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(java.util.Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createBalanceItem(double balance) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Ada Bankası");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Mevcut Bakiye: " + ChatColor.GREEN + formatAmount(balance));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String formatAmount(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    // ---- tıklama işlemleri ----

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) return;

        Island island = SkyblockCore.getInstance().getIslandManager().getIslandByMember(player.getUniqueId());
        if (island == null) {
            player.closeInventory();
            return;
        }

        // izin kontrolü komut katmaninda zaten yapildi (tek kaynak), burada tekrar kontrol etmiyoruz.

        int slot = event.getSlot();

        if (DEPOSIT_SLOTS.contains(slot)) {
            startPrompt(player, island, BankActionType.DEPOSIT);
        } else if (WITHDRAW_SLOTS.contains(slot)) {
            startPrompt(player, island, BankActionType.WITHDRAW);
        }
    }

    private void startPrompt(Player player, Island island, BankActionType type) {
        player.closeInventory();
        PENDING_ACTIONS.put(player.getUniqueId(), new PendingBankAction(island.getOwnerUUID(), type));

        if (type == BankActionType.DEPOSIT) {
            player.sendMessage(msg.getMessage("bank.deposit-prompt"));
        } else {
            player.sendMessage(msg.getMessage("bank.withdraw-prompt"));
        }
    }

    // ---- sohbet ile miktar girisi ----

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        PendingBankAction pending = PENDING_ACTIONS.get(playerUuid);
        if (pending == null) return;

        event.setCancelled(true);
        String rawMessage = event.getMessage().trim();

        Bukkit.getScheduler().runTask(SkyblockCore.getInstance(), () -> handleBankChatInput(player, pending, rawMessage));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PENDING_ACTIONS.remove(event.getPlayer().getUniqueId());
    }

    private void handleBankChatInput(Player player, PendingBankAction pending, String rawMessage) {
        UUID playerUuid = player.getUniqueId();

        // gecerlilik hala suruyor mu (baska bir islem devreye girmis olabilir)
        if (PENDING_ACTIONS.get(playerUuid) != pending) return;

        if (pending.isExpired()) {
            PENDING_ACTIONS.remove(playerUuid);
            player.sendMessage(msg.getMessage("bank.timeout"));
            return;
        }

        if (rawMessage.equalsIgnoreCase("iptal") || rawMessage.equalsIgnoreCase("cancel")) {
            PENDING_ACTIONS.remove(playerUuid);
            player.sendMessage(msg.getMessage("bank.cancelled"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(rawMessage.replace(",", "."));
        } catch (NumberFormatException e) {
            player.sendMessage(msg.getMessage("bank.invalid-amount"));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(msg.getMessage("bank.amount-must-be-positive"));
            return;
        }

        Island island = SkyblockCore.getInstance().getIslandManager().getIsland(pending.islandOwnerUuid);
        if (island == null) {
            PENDING_ACTIONS.remove(playerUuid);
            player.sendMessage(msg.getMessage("island.no-island"));
            return;
        }

        EconomyProvider economy = SkyblockCore.getEconomyProvider();
        if (economy == null) {
            PENDING_ACTIONS.remove(playerUuid);
            player.sendMessage(msg.getMessage("bank.economy-unavailable"));
            return;
        }

        PENDING_ACTIONS.remove(playerUuid);

        if (pending.type == BankActionType.DEPOSIT) {
            if (!economy.hasBalance(playerUuid, amount)) {
                player.sendMessage(msg.getMessage("bank.not-enough-player-balance"));
                return;
            }

            economy.removeBalance(playerUuid, amount);
            island.addBalance(amount);
            economy.addIslandBankBalance(amount, island.getIslandUuid());

            player.sendMessage(msg.getMessage("bank.deposit-success")
                    .replace("{amount}", formatAmount(amount))
                    .replace("{balance}", formatAmount(island.getBalance())));
        } else {
            if (!island.removeBalance(amount)) {
                player.sendMessage(msg.getMessage("bank.not-enough-island-balance"));
                return;
            }

            economy.addBalance(playerUuid, amount);
            economy.removeIslandBankBalance(amount, island.getIslandUuid());

            player.sendMessage(msg.getMessage("bank.withdraw-success")
                    .replace("{amount}", formatAmount(amount))
                    .replace("{balance}", formatAmount(island.getBalance())));
        }

        if (player.isOnline()) {
            openBankMenu(player, island);
        }
    }
}