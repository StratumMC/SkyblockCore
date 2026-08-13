package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class IslandProtectionListener implements Listener {

    private final SkyblockCore plugin;

    public IslandProtectionListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    private boolean canInteract(Player player, Location location) {
        if (location == null || location.getWorld() == null) return true;
        if (plugin.getWorldManager().getSkyblockWorld() == null) return true;

        if (!location.getWorld().getName().equalsIgnoreCase(plugin.getWorldManager().getSkyblockWorld().getName())) {
            return true;
        }

        if (player.isOp()) return true;

        Island island = getIslandAt(location);
        if (island == null) {
            return false;
        }

        return island.getOwnerUUID().equals(player.getUniqueId());
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks on this island!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks on this island!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!canInteract(event.getPlayer(), event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot interact on this island!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (!canInteract(attacker, event.getEntity().getLocation())) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "You cannot attack entities here!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim) {
            Island island = getIslandAt(victim.getLocation());
            if (island != null && !island.getOwnerUUID().equals(victim.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player targetPlayer) {
            Island island = getIslandAt(targetPlayer.getLocation());
            if (island != null && !island.getOwnerUUID().equals(targetPlayer.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}