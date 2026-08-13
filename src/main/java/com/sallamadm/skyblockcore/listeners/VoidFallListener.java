package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidFallListener implements Listener {

    private final SkyblockCore plugin;

    public VoidFallListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().getName().equals("skyblock_world") && player.getLocation().getY() < 0) {
            Island island = plugin.getIslandManager().getIsland(player.getUniqueId());

            if (island != null && island.getSpawnLocation() != null) {
                player.setFallDistance(0);
                player.teleport(island.getSpawnLocation());
            } else {
                player.setFallDistance(0);
                player.teleport(player.getWorld().getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getWorld().getName().equals("skyblock_world")) {
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID || event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);
                }
            }
        }
    }
}