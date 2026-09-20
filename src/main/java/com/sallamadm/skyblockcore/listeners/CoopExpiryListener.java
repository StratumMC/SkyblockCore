package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class CoopExpiryListener implements Listener {

    private final SkyblockCore plugin;

    public CoopExpiryListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getIslandManager().removeCoopsAddedBy(player.getUniqueId());
    }
}
