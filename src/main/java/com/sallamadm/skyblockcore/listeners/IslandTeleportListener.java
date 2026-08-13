package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.border.BorderManager;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class IslandTeleportListener implements Listener {

    private final SkyblockCore plugin;

    public IslandTeleportListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to == null || to.getWorld() == null) return;
        if (plugin.getWorldManager().getSkyblockWorld() == null) return;

        if (!to.getWorld().getName().equalsIgnoreCase(plugin.getWorldManager().getSkyblockWorld().getName())) {
            BorderManager.removeBorder(player);
            return;
        }

        Island targetIsland = getIslandAt(to);

        if (targetIsland != null) {
            boolean isOwner = targetIsland.getOwnerUUID().equals(player.getUniqueId());

            if (!isOwner && targetIsland.isLocked() && !player.isOp()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "This island is locked by its owner!");
                return;
            }
            BorderManager.applyIslandBorder(player, targetIsland);
        } else {
            BorderManager.removeBorder(player);
        }
    }

    private Island getIslandAt(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;

        for (Island island : plugin.getIslandManager().getAllIslands().values()) {
            if (island.getCenterLocation() == null) continue;

            Location center = island.getCenterLocation();
            int radius = island.getIslandSize() / 2;

            int minX = center.getBlockX() - radius;
            int maxX = center.getBlockX() + radius;
            int minZ = center.getBlockZ() - radius;
            int maxZ = center.getBlockZ() + radius;

            if (loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                    loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ) {
                return island;
            }
        }
        return null;
    }
}