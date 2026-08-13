package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class JoinListener implements Listener {
    private final SkyblockCore plugin;

    public JoinListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        plugin.getScoreboardManager().updateScoreboard(event.getPlayer());
    }
}
