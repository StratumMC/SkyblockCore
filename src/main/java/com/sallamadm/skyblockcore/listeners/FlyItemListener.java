package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.fly.FlyItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FlyItemListener implements Listener {

    private final SkyblockCore plugin;
    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    public FlyItemListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.PAPER || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(FlyItem.REWARD_KEY, PersistentDataType.LONG)) return;

        event.setCancelled(true);

        long seconds = meta.getPersistentDataContainer().get(FlyItem.REWARD_KEY, PersistentDataType.LONG);
        plugin.getFlightManager().addSeconds(player.getUniqueId(), seconds);
        plugin.getScoreboardManager().updateScoreboard(player);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        player.sendMessage(msg.getMessage("fly.item-used").replace("{minutes}", String.valueOf(seconds / 60)));
    }
}