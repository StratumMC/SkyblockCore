package com.sallamadm.skyblockcore.fly;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class FlightManager implements Listener {

    private final SkyblockCore plugin;
    private final Map<UUID, Long> remainingSeconds = new HashMap<>();
    private final Set<UUID> activeFlyers = new HashSet<>();

    public FlightManager(SkyblockCore plugin) {
        this.plugin = plugin;
        startTickTask();
    }

    public boolean isFlying(UUID uuid) {
        return activeFlyers.contains(uuid);
    }

    public long getRemainingSeconds(UUID uuid) {
        return remainingSeconds.computeIfAbsent(uuid, plugin.getDataManager()::loadFlyTimeSync);
    }

    public void setRemainingSeconds(UUID uuid, long seconds) {
        long clamped = Math.max(0, seconds);
        remainingSeconds.put(uuid, clamped);
        plugin.getDataManager().saveFlyTimeAsync(uuid, clamped);
    }

    public void addSeconds(UUID uuid, long secondsToAdd) {
        setRemainingSeconds(uuid, getRemainingSeconds(uuid) + secondsToAdd);
    }

    public void enableFly(Player player) {
        activeFlyers.add(player.getUniqueId());
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void disableFly(Player player, String reasonMessageKey) {
        activeFlyers.remove(player.getUniqueId());
        player.setFlying(false);
        player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE);

        if (!player.isOp()) {
            plugin.getDataManager().saveFlyTimeAsync(player.getUniqueId(), getRemainingSeconds(player.getUniqueId()));
        }

        if (reasonMessageKey != null) {
            player.sendMessage(plugin.getMessageManager().getMessage(reasonMessageKey));
        }
        plugin.getScoreboardManager().updateScoreboard(player);
    }

    private void startTickTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : new HashSet<>(activeFlyers)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    activeFlyers.remove(uuid);
                    continue;
                }

                Island island = plugin.getIslandManager().getIslandAt(player.getLocation());
                boolean hasFlyPermissionHere = island != null && island.hasPermission(uuid, IslandPermissions.FLY.getNode());

                if (!hasFlyPermissionHere) {
                    disableFly(player, "fly.left-island");
                    continue;
                }

                if (player.isOp()) {
                    plugin.getScoreboardManager().updateScoreboard(player);
                    continue;
                }

                if (!player.isFlying()) {
                    continue;
                }

                long current = getRemainingSeconds(uuid);
                if (current <= 1) {
                    setRemainingSeconds(uuid, 0);
                    disableFly(player, "fly.time-expired");
                } else {
                    remainingSeconds.put(uuid, current - 1);
                    plugin.getScoreboardManager().updateScoreboard(player);
                }
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        activeFlyers.remove(uuid);
        Long remaining = remainingSeconds.get(uuid);
        if (remaining != null && !event.getPlayer().isOp()) {
            plugin.getDataManager().saveFlyTimeSync(uuid, remaining);
        }
        remainingSeconds.remove(uuid);
    }

    public void saveAllSync() {
        for (Map.Entry<UUID, Long> entry : remainingSeconds.entrySet()) {
            plugin.getDataManager().saveFlyTimeSync(entry.getKey(), entry.getValue());
        }
    }
}
