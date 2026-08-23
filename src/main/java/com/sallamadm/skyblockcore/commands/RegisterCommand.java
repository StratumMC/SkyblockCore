package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.listeners.AuthListener;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

import java.util.regex.Pattern;

public class RegisterCommand {

    // example@example.com örnegı harıcı kjabul etmiyor artık
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static void registerCommand(SkyblockCore plugin) {
        MessageManager msg = plugin.getMessageManager();

        new CommandAPICommand("register")
                .withArguments(new StringArgument("password"))
                .withArguments(new GreedyStringArgument("email"))
                .executesPlayer((player, args) -> {
                    if (plugin.getDataManager().isRegistered(player.getName())) {
                        player.sendMessage(msg.getMessage("auth.already-registered"));
                        return;
                    }

                    String password = (String) args.get("password");
                    String email = (String) args.get("email");

                    if (!EMAIL_PATTERN.matcher(email).matches()) {
                        player.sendMessage(msg.getMessage("auth.invalid-email"));
                        return;
                    }

                    plugin.getDataManager().registerAccount(player.getUniqueId(), player.getName(), email, password);
                    AuthListener.authenticate(player.getUniqueId());

                    player.sendMessage(msg.getMessage("auth.success"));
                })
                .register();
    }
}