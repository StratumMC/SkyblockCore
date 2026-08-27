package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;

public class DiscordConnectCommand {

    public static void registerCommand(SkyblockCore plugin) {
        MessageManager msg = plugin.getMessageManager();

        new CommandAPICommand("discord-bagla")
                .withArguments(new StringArgument("code"))
                .executesPlayer((player, args) -> {
                    String rawCode = (String) args.get("code");
                    boolean matches = plugin.getDataManager().consumeDiscordLinkCode(player.getUniqueId(), player.getName(), rawCode);
                    boolean checkIfDiscordConnect = plugin.getDataManager().checkIfDiscordLinked(player.getUniqueId());
                    if(checkIfDiscordConnect) {
                        player.sendMessage(msg.getMessage("discordconnect.already_linked"));
                        return;
                    }
                    if (matches) {
                        player.sendMessage(msg.getMessage("discordconnect.success"));
                        return;
                    }
                    player.sendMessage(msg.getMessage("discordconnect.failure"));
                })
                .register();
    }
}