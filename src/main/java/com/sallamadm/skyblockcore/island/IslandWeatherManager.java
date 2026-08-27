package com.sallamadm.skyblockcore.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandWeather;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class IslandWeatherManager {

    private static final Random RANDOM = new Random();

    private static final Map<UUID, String> lastAppliedIslandUuid = new HashMap<>();

    public static void applyToPlayer(Player player, Island island) {
        if (player == null) return;
        IslandWeather option = island != null ? IslandWeather.fromNode(island.getWeatherOption()) : IslandWeather.NORMAL;
        apply(player, option);
        lastAppliedIslandUuid.put(player.getUniqueId(), island != null ? island.getIslandUuid() : null);
    }

    public static void apply(Player player, IslandWeather option) {
        if (player == null || option == null) return;

        if (option.getFixedTime() != null) {
            player.setPlayerTime(option.getFixedTime(), false);
        } else {
            player.resetPlayerTime();
        }

        if (option.getWeatherType() != null) {
            player.setPlayerWeather(option.getWeatherType());
        } else {
            player.resetPlayerWeather();
        }
    }

    public static void reset(Player player) {
        if (player == null) return;
        player.resetPlayerTime();
        player.resetPlayerWeather();
        lastAppliedIslandUuid.remove(player.getUniqueId());
    }

    public static void handleMove(SkyblockCore plugin, Player player, Location to) {
        if (to == null || to.getWorld() == null) return;
        if (plugin.getWorldManager().getSkyblockWorld() == null) return;

        if (!to.getWorld().getName().equalsIgnoreCase(plugin.getWorldManager().getSkyblockWorld().getName())) {
            if (lastAppliedIslandUuid.remove(player.getUniqueId()) != null) {
                reset(player);
            }
            return;
        }

        Island island = plugin.getIslandManager().getIslandAt(to);
        String newUuid = island != null ? island.getIslandUuid() : null;
        String oldUuid = lastAppliedIslandUuid.get(player.getUniqueId());

        if (Objects.equals(newUuid, oldUuid)) return;

        if (island != null) {
            applyToPlayer(player, island);
        } else {
            reset(player);
        }
    }

    public static void clearPlayer(Player player) {
        if (player != null) {
            lastAppliedIslandUuid.remove(player.getUniqueId());
        }
    }

    public static void refreshIslandPlayers(SkyblockCore plugin, Island island) {
        if (island == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            Island current = plugin.getIslandManager().getIslandAt(p.getLocation());
            if (current == island) {
                applyToPlayer(p, island);
            }
        }
    }

    public static void startThunderEffectTask(SkyblockCore plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Island island = plugin.getIslandManager().getIslandAt(player.getLocation());
                if (island == null) continue;

                IslandWeather option = IslandWeather.fromNode(island.getWeatherOption());
                if (!option.isThunder()) continue;
                if (RANDOM.nextInt(100) >= 12) continue;

                Location center = island.getCenterLocation();
                if (center == null || center.getWorld() == null) continue;

                int radius = Math.max(1, island.getIslandSize() / 2);
                double x = center.getX() + (RANDOM.nextDouble() * 2 - 1) * radius;
                double z = center.getZ() + (RANDOM.nextDouble() * 2 - 1) * radius;
                Location strikeLoc = new Location(center.getWorld(), x, center.getY() + 1, z);

                center.getWorld().strikeLightningEffect(strikeLoc);
            }
        }, 100L, 100L);
    }
}
