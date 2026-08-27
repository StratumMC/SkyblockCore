package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandBlockLevel;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockLevelListener implements Listener {
    private final SkyblockCore plugin;
    public BlockLevelListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }
    private boolean isProtectedWorld(Location location) {
        if (location == null || location.getWorld() == null) return false;
        if (plugin.getWorldManager().getSkyblockWorld() == null) return false;
        return location.getWorld().getName().equalsIgnoreCase(plugin.getWorldManager().getSkyblockWorld().getName());
    }
    private Island getIslandAt(Location location) {
        if (!isProtectedWorld(location)) return null;
        return plugin.getIslandManager().getIslandAt(location);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Island island = getIslandAt(block.getLocation());
        if (island == null) return;
        if (!IslandBlockLevel.isLevelBlock(block.getType())) return;
        IslandBlockLevel blockLevel = IslandBlockLevel.fromMaterial(block.getType());
        if (blockLevel != null) {
            double levelGain = blockLevel.getLevel();
            island.addLevel(levelGain);
            if (plugin.getScoreboardManager() != null) {
                plugin.getScoreboardManager().updateScoreboard(event.getPlayer());
            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Island island = getIslandAt(block.getLocation());
        if (island == null) return;
        if (!IslandBlockLevel.isLevelBlock(block.getType())) return;
        IslandBlockLevel blockLevel = IslandBlockLevel.fromMaterial(block.getType());
        if (blockLevel != null) {
            double levelLoss = blockLevel.getLevel();
            island.addLevel(-levelLoss);
            if (plugin.getScoreboardManager() != null) {
                plugin.getScoreboardManager().updateScoreboard(event.getPlayer());
            }
        }
    }
    private void updateScoreboard(Player player) {
        if (player == null) return;
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().updateScoreboard(player);
        }
    }
}