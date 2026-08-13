package com.sallamadm.skyblockcore.border;

import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

public class BorderManager {


    public static void applyIslandBorder(Player player, Island island) {
        if (player == null || island == null || island.getCenterLocation() == null) return;

        WorldBorder border = player.getWorldBorder();
        if (border == null) {
            border = player.getServer().createWorldBorder();
        }

        border.setCenter(island.getCenterLocation());
        border.setSize(island.getIslandSize());
        border.setWarningDistance(0);

        player.setWorldBorder(border);
    }


    public static void removeBorder(Player player) {
        if (player != null) {
            player.setWorldBorder(null);
        }
    }
}