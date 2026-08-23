package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.fly.FlightManager;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.World;

public class FlyCommand {

    public static void registerCommand(SkyblockCore plugin) {
        MessageManager msg = plugin.getMessageManager();

        new CommandAPICommand("fly")
                .executesPlayer((player, args) -> {
                    FlightManager flightManager = plugin.getFlightManager();
                    World skyblockWorld = plugin.getWorldManager().getSkyblockWorld();
                    if(!player.isOp()) {
                        if (skyblockWorld == null || !player.getWorld().equals(skyblockWorld)) {
                            player.sendMessage(msg.getMessage("fly.wrong-world"));
                            return;
                        }

                        Island island = plugin.getIslandManager().getIslandAt(player.getLocation());
                        if (island == null) {
                            player.sendMessage(msg.getMessage("fly.no-island-here"));
                            return;
                        }

                        if (!island.hasPermission(player.getUniqueId(), IslandPermissions.FLY.getNode())) {
                            player.sendMessage(msg.getMessage("fly.no-permission"));
                            return;
                        }



                        if (flightManager.isFlying(player.getUniqueId())) {
                            flightManager.disableFly(player, null);
                            player.sendMessage(msg.getMessage("fly.disabled"));
                            return;
                        }

                        if (!player.isOp() && flightManager.getRemainingSeconds(player.getUniqueId()) <= 0) {
                            player.sendMessage(msg.getMessage("fly.no-time-left"));
                            return;
                        }
                        flightManager.enableFly(player);
                        player.sendMessage(msg.getMessage("fly.enabled"));
                    } else {

                        flightManager.enableFly(player);
                        player.sendMessage(msg.getMessage("fly.enabled"));
                    }
                })
                .register();
    }
}