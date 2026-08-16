package com.sallamadm.skyblockcore.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

import java.util.*;

public class AuthListener implements Listener {
    private final SkyblockCore plugin;
    private static final Set<UUID> unauthenticatedPlayers = new HashSet<>();
    private final Location LOBBY_SPAWN = new Location(Bukkit.getWorlds().get(0), 71, 73, 188, -90f, -1.6f);

    private static final Map<UUID, org.bukkit.scheduler.BukkitTask> reminderTasks = new HashMap<>();

    public AuthListener(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    public static void authenticate(UUID uuid) {
        unauthenticatedPlayers.remove(uuid);
        stopReminder(uuid);
    }

    public static void unauthenticate(UUID uuid) {
        unauthenticatedPlayers.add(uuid);
    }

    private static void stopReminder(UUID uuid) {
        org.bukkit.scheduler.BukkitTask task = reminderTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public static boolean isUnauthenticated(UUID uuid) {
        return unauthenticatedPlayers.contains(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.teleport(LOBBY_SPAWN);

        UUID uuid = player.getUniqueId();
        unauthenticatedPlayers.add(uuid);

        boolean isRegistered = plugin.getDataManager().isRegistered(player.getName());

        if (!isRegistered) {
            player.sendMessage(plugin.getMessageManager().getMessage("auth.register-prompt"));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage("auth.login-prompt"));
        }
        org.bukkit.scheduler.BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || !unauthenticatedPlayers.contains(uuid)) {
                return;
            }

            if (!plugin.getDataManager().isRegistered(player.getName())) {
                player.sendMessage(plugin.getMessageManager().getMessage("auth.register-prompt"));
            } else {
                player.sendMessage(plugin.getMessageManager().getMessage("auth.login-prompt"));
            }
        }, 100L, 100L);

        reminderTasks.put(uuid, task);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        unauthenticatedPlayers.remove(uuid);
        stopReminder(uuid);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (unauthenticatedPlayers.contains(player.getUniqueId())) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (unauthenticatedPlayers.contains(player.getUniqueId())) {
            String message = event.getMessage().toLowerCase();
            if (!message.startsWith("/register") && !message.startsWith("/login")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessageManager().getMessage("auth.plese-register"));
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (unauthenticatedPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageManager().getMessage("auth.plese-register"));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (unauthenticatedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (unauthenticatedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}