package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.gui.BlockLevelMenu;
import dev.jorel.commandapi.CommandAPICommand;

public class BlockInfoCommand {
    public static void registerCommand(SkyblockCore plugin) {
        new CommandAPICommand("blokbilgi")
                .executesPlayer((player, args) -> {
                    BlockLevelMenu.openBlockLevelMenu(player);
                })
                .register();
    }
}
