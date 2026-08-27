package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;

public class WebConnectCommand {

    public static void registerCommand(SkyblockCore plugin) {
        MessageManager msg = plugin.getMessageManager();

        new CommandAPICommand("site-bagla")
                .withArguments(new StringArgument("code"))
                .executesPlayer((player, args) -> {
                    String rawCode = (String) args.get("code");
                    boolean matches = plugin.getDataManager().consumeMinecraftLinkCode(player.getUniqueId(), player.getName(), rawCode);

                    if (matches) {
                        player.sendMessage(msg.getMessage("webconnect.success"));
                        return;
                    }

                    player.sendMessage(msg.getMessage("webconnect.failure"));
                })
                .register();
    }
}