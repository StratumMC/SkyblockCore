package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandRole;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class QuitListener implements Listener {

    private final SkyblockCore plugin;

    public QuitListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());

        if (island != null && island.getRole(player.getUniqueId()) == IslandRole.COOP) {
            UUID addedBy = island.getCoopAddedBy().get(player.getUniqueId());
            if (addedBy != null && !addedBy.equals(player.getUniqueId())) {
                island.removeMember(player.getUniqueId());
                Player adder = plugin.getServer().getPlayer(addedBy);
                if (adder != null && adder.isOnline()) {
                    adder.sendMessage(plugin.getMessageManager().getMessage("island.coop-left")
                            .replace("{player}", player.getName()));
                }
            }
        }
    }
}