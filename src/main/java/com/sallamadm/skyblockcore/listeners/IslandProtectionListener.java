package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.InventoryHolder;

public class IslandProtectionListener implements Listener {
    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private final SkyblockCore plugin;

    public IslandProtectionListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    private boolean isProtectedWorld(Location location) {
        if (location == null || location.getWorld() == null) return false;
        if (plugin.getWorldManager().getSkyblockWorld() == null) return false;
        return location.getWorld().getName().equalsIgnoreCase(plugin.getWorldManager().getSkyblockWorld().getName());
    }

    private boolean hasIslandPermission(Player player, Location location, IslandPermissions permission) {
        if (!isProtectedWorld(location)) return true;
        if (player.isOp()) return true;

        Island island = getIslandAt(location);
        if (island == null) return false;

        return island.hasPermission(player.getUniqueId(), permission.getNode());
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
        if (!hasIslandPermission(event.getPlayer(), event.getBlock().getLocation(), IslandPermissions.BLOCK_BREAK)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(msg.getMessage("protection.cannot-build"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!hasIslandPermission(event.getPlayer(), event.getBlock().getLocation(), IslandPermissions.BLOCK_PLACE)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(msg.getMessage("protection.cannot-build"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        boolean isContainer = block.getState() instanceof InventoryHolder;
        IslandPermissions required = isContainer ? IslandPermissions.CONTAINER_ACCESS : IslandPermissions.INTERACT;

        if (!hasIslandPermission(event.getPlayer(), block.getLocation(), required)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(msg.getMessage(isContainer ? "protection.cannot-container" : "protection.cannot-interact"));
        }
    }

    // saldırı ıcın permission node accam unutma

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (!isProtectedWorld(event.getEntity().getLocation())) return;
            if (attacker.isOp()) return;

            Island island = getIslandAt(event.getEntity().getLocation());
            if (island != null && !island.getOwnerUUID().equals(attacker.getUniqueId())) {
                event.setCancelled(true);
                attacker.sendMessage(msg.getMessage("protection.cannot-attack"));
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