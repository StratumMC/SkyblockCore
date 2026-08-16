package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.listeners.AuthListener;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;

public class LoginCommand {

    public static void registerCommand(SkyblockCore plugin) {
        MessageManager msg = plugin.getMessageManager();

        new CommandAPICommand("login")
                .withArguments(new StringArgument("password"))
                .executesPlayer((player, args) -> {
                    if (!plugin.getDataManager().isRegistered(player.getName())) {
                        player.sendMessage(msg.getMessage("auth.register-prompt"));
                        return;
                    }

                    if (!AuthListener.isUnauthenticated(player.getUniqueId())) {
                        player.sendMessage(msg.getMessage("auth.already-logged-in"));
                        return;
                    }

                    String password = (String) args.get("password");

                    if (plugin.getDataManager().checkPassword(player.getName(), password)) {
                        AuthListener.authenticate(player.getUniqueId());
                        plugin.getDataManager().updateUuid(player.getName(), player.getUniqueId());

                        player.sendMessage(msg.getMessage("auth.login-success"));
                    } else {
                        player.sendMessage(msg.getMessage("auth.wrong-password"));
                    }
                })
                .register();
    }
}