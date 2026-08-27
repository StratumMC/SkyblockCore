package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.border.BorderManager;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.IslandWeatherManager;
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
        Player player = event.getPlayer();

        plugin.getScoreboardManager().updateScoreboard(player);

        Island island = plugin.getIslandManager().getIslandAt(player.getLocation());
        if (island != null) {
            BorderManager.applyIslandBorder(player, island);
        }
    }
}
