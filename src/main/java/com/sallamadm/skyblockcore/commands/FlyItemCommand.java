package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.fly.FlyItem;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;

public class FlyItemCommand {

    public static void registerCommand(SkyblockCore plugin) {
        MessageManager msg = plugin.getMessageManager();

        new CommandAPICommand("flyitem")
                .withArguments(new PlayerArgument("target"))
                .withArguments(new IntegerArgument("minutes"))
                .executesPlayer((player, args) -> {
                    if (!player.isOp()) {
                        player.sendMessage(msg.getMessage("general.no-permission"));
                        return;
                    }

                    Player target = (Player) args.get("target");
                    int minutes = (Integer) args.get("minutes");

                    if (minutes != 10 && minutes != 30) {
                        player.sendMessage(msg.getMessage("fly.invalid-item-amount"));
                        return;
                    }

                    target.getInventory().addItem(FlyItem.create(minutes));

                    player.sendMessage(msg.getMessage("fly.item-given")
                            .replace("{target}", target.getName())
                            .replace("{minutes}", String.valueOf(minutes)));
                    target.sendMessage(msg.getMessage("fly.item-received")
                            .replace("{minutes}", String.valueOf(minutes)));
                })
                .register();
    }
}