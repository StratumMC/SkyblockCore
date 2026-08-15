package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.border.BorderManager;
import com.sallamadm.skyblockcore.events.IslandEvents;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.Warp;
import com.sallamadm.skyblockcore.listeners.WarpMenuListener;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IsCommand {

    private static final Map<String, String> HELP_MAP = new LinkedHashMap<>();

    public static void registerCommand(SkyblockCore plugin) {
        HELP_MAP.clear();

        ArgumentSuggestions<CommandSender> warpSuggestions = ArgumentSuggestions.strings(info -> {
            String targetName = info.previousArgs().get("target") != null ? (String) info.previousArgs().get("target") : "";
            if (targetName.isEmpty()) return new String[0];

            @SuppressWarnings("deprecation")
            OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(targetName);

            Island island = plugin.getIslandManager().getIsland(targetOwner.getUniqueId());
            if (island == null) return new String[0];

            List<String> visibleWarps = new ArrayList<>();
            for (Warp warp : island.getWarps().values()) {
                if (warp.isVisible() || info.sender().isOp()) {
                    visibleWarps.add(warp.getName());
                }
            }
            return visibleWarps.toArray(new String[0]);
        });

        new CommandAPICommand("is")
                .withAliases("island")

                // /is level
                .withSubcommand(createSubCommand("level", "Displays your island level.",
                        new CommandAPICommand("level")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(ChatColor.RED + "You don't have an island! Create one first with /is create.");
                                        return;
                                    }
                                    player.sendMessage(ChatColor.GREEN + "Your Island Level: " + ChatColor.YELLOW + island.getLevel());
                                })
                ))

                // /is setname <name>
                .withSubcommand(createSubCommand("setname <name>", "Change your islands name.",
                        new CommandAPICommand("setname")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(ChatColor.RED + "You don't have an island!");
                                        return;
                                    }
                                    String newName = (String) args.get("name");
                                    island.setIslandName(newName);
                                    plugin.getDataManager().saveData();
                                    player.sendMessage(ChatColor.GREEN + "Your island name has been changed to: " + ChatColor.YELLOW + newName);
                                })
                ))

                // /is biome
                .withSubcommand(createSubCommand("biome", "Change islands biome.",
                        new CommandAPICommand("biome")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(ChatColor.RED + "You don't have an island!");
                                        return;
                                    }
                                    com.sallamadm.skyblockcore.listeners.BiomeMenuListener.openBiomeMenu(player);
                                })
                ))

                // /is create
                .withSubcommand(createSubCommand("create", "Creates a new island.",
                        new CommandAPICommand("create")
                                .executesPlayer((player, args) -> {
                                    if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
                                        player.sendMessage(ChatColor.RED + "You already have an island.");
                                        return;
                                    }

                                    World skyblockWorld = plugin.getWorldManager().getSkyblockWorld();
                                    if (skyblockWorld == null) {
                                        player.sendMessage(ChatColor.RED + "Cannot load skyblock world!");
                                        return;
                                    }

                                    int gridIndex = plugin.getIslandManager().fetchNextGridIndex();
                                    Location islandLoc = plugin.getIslandManager().calculateLocationFromIndex(skyblockWorld, gridIndex);

                                    Island island = plugin.getIslandManager().createIsland(player.getUniqueId());
                                    island.setGridIndex(gridIndex);
                                    island.setCenterLocation(islandLoc);

                                    buildIslandStructure(skyblockWorld, islandLoc, player, island);
                                    BorderManager.applyIslandBorder(player, island);

                                    plugin.getScoreboardManager().updateScoreboard(player);
                                    island.setIslandName(player.getName() + "'s Island");

                                    Bukkit.getPluginManager().callEvent(new IslandEvents.Create(player, island));

                                    plugin.getDataManager().saveData();

                                    player.sendMessage(ChatColor.GREEN + "Created your island.");
                                })
                ))

                // /is delete
                .withSubcommand(createSubCommand("delete", "Deletes your island.",
                        new CommandAPICommand("delete")
                                .executesPlayer((player, args) -> {
                                    Island islandToDelete = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (islandToDelete == null) {
                                        player.sendMessage(ChatColor.RED + "You don't have an island.");
                                        return;
                                    }

                                    Bukkit.getPluginManager().callEvent(new IslandEvents.Delete(player, islandToDelete));
                                    BorderManager.removeBorder(player);
                                    plugin.getIslandManager().removeIsland(player.getUniqueId());

                                    plugin.getDataManager().saveData();

                                    plugin.getScoreboardManager().updateScoreboard(player);

                                    World mainWorld = Bukkit.getWorlds().get(0);
                                    player.teleport(mainWorld.getSpawnLocation());

                                    player.sendMessage(ChatColor.YELLOW + "Island deleted.");
                                })
                ))

                // /is go
                .withSubcommand(createSubCommand("go", "Teleports to your island.",
                        new CommandAPICommand("go")
                                .executesPlayer((player, args) -> {
                                    if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
                                        teleportToIsland(plugin, player);
                                    } else {
                                        player.sendMessage(ChatColor.RED + "You don't have an island. Create one first! /is create");
                                    }
                                })
                ))

                // /is help
                .withSubcommand(createSubCommand("help", "Displays this help menu.",
                        new CommandAPICommand("help")
                                .executesPlayer((player, args) -> {
                                    sendHelpMenu(player);
                                })
                ))

                // /is lock
                .withSubcommand(createSubCommand("lock", "Locks your island from visitors.",
                        new CommandAPICommand("lock")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(ChatColor.RED + "You don't have an island!");
                                        return;
                                    }

                                    if (island.isLocked()) {
                                        player.sendMessage(ChatColor.RED + "Your island is already locked!");
                                        return;
                                    }

                                    island.setLocked(true);
                                    player.sendMessage(ChatColor.GREEN + "Your island is now " + ChatColor.RED + "LOCKED" + ChatColor.GREEN + " to visitors.");
                                })
                ))

                // /is unlock
                .withSubcommand(createSubCommand("unlock", "Unlocks your island for visitors.",
                        new CommandAPICommand("unlock")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(ChatColor.RED + "You don't have an island!");
                                        return;
                                    }

                                    if (!island.isLocked()) {
                                        player.sendMessage(ChatColor.RED + "Your island is already unlocked!");
                                        return;
                                    }

                                    island.setLocked(false);
                                    player.sendMessage(ChatColor.GREEN + "Your island is now " + ChatColor.AQUA + "UNLOCKED" + ChatColor.GREEN + " to visitors.");
                                })
                ))

                // /is setwarp
                .withSubcommand(createSubCommand("setwarp <name>", "Creates a new warp on your island (Hidden by default).",
                        new CommandAPICommand("setwarp")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(ChatColor.RED + "You don't have an island!");
                                        return;
                                    }

                                    Location loc = player.getLocation();
                                    Location center = island.getCenterLocation();
                                    if (center == null || !loc.getWorld().equals(center.getWorld())) {
                                        player.sendMessage(ChatColor.RED + "You must be on your island to set a warp!");
                                        return;
                                    }

                                    int radius = island.getIslandSize() / 2;
                                    if (Math.abs(loc.getBlockX() - center.getBlockX()) > radius || Math.abs(loc.getBlockZ() - center.getBlockZ()) > radius) {
                                        player.sendMessage(ChatColor.RED + "You can only set warps within your island boundary!");
                                        return;
                                    }

                                    String warpName = (String) args.get("name");
                                    Warp warp = new Warp(warpName, loc);
                                    island.addWarp(warp);
                                    player.sendMessage(ChatColor.GREEN + "Warp " + ChatColor.YELLOW + warpName + ChatColor.GREEN + " created! It is currently " + ChatColor.RED + "HIDDEN" + ChatColor.GREEN + " from visitors.");
                                })
                ))

                // /is delwarp
                .withSubcommand(createSubCommand("delwarp <name>", "Deletes a warp by name.",
                        new CommandAPICommand("delwarp")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(ChatColor.RED + "You don't have an island!");
                                        return;
                                    }

                                    String warpName = (String) args.get("name");
                                    if (island.getWarp(warpName) == null) {
                                        player.sendMessage(ChatColor.RED + "Warp not found: " + warpName);
                                        return;
                                    }

                                    island.removeWarp(warpName);
                                    player.sendMessage(ChatColor.GREEN + "Warp " + ChatColor.YELLOW + warpName + ChatColor.GREEN + " has been deleted!");
                                })
                ))

                // /is warps
                .withSubcommand(createSubCommand("warps", "Opens your warp management GUI.",
                        new CommandAPICommand("warps")
                                .executesPlayer((player, args) -> {
                                    WarpMenuListener.openOwnerWarpMenu(player);
                                })
                ))

                // /is visit
                .withSubcommand(createSubCommand("visit [target] [warp]", "Visit someones island.",
                        new CommandAPICommand("visit")
                                .executesPlayer((player, args) -> {
                                    WarpMenuListener.openSelfTeleportWarpMenu(player);
                                })
                                .withOptionalArguments(new StringArgument("target"))
                                .withOptionalArguments(new StringArgument("warp").replaceSuggestions(warpSuggestions))
                                .executesPlayer((player, args) -> {
                                    handleWarpTeleportCommand(player, args, plugin);
                                })
                ))

                // /is warp
                .withSubcommand(createSubCommand("warp [target] [warp]", "Teleport to warps.",
                        new CommandAPICommand("warp")
                                .executesPlayer((player, args) -> {
                                    WarpMenuListener.openSelfTeleportWarpMenu(player);
                                })
                                .withOptionalArguments(new StringArgument("target"))
                                .withOptionalArguments(new StringArgument("warp").replaceSuggestions(warpSuggestions))
                                .executesPlayer((player, args) -> {
                                    handleWarpTeleportCommand(player, args, plugin);
                                })
                ))

                // ADMIN COMMANDS

                // /is set level <player> <amount>
                .withSubcommand(createSubCommand("set level <player> <amount>", "[Admin] Sets player island level.",
                        new CommandAPICommand("set")
                                .withSubcommand(
                                        new CommandAPICommand("level")
                                                .withArguments(new PlayerArgument("target"))
                                                .withArguments(new IntegerArgument("amount", 1))
                                                .withPermission(CommandPermission.OP)
                                                .executesPlayer((player, args) -> {
                                                    Player target = (Player) args.get("target");
                                                    int amount = (int) args.get("amount");

                                                    Island targetIsland = plugin.getIslandManager().getIsland(target.getUniqueId());
                                                    if (targetIsland == null) {
                                                        player.sendMessage(ChatColor.RED + target.getName() + " does not have an island!");
                                                        return;
                                                    }

                                                    targetIsland.setLevel(amount);
                                                    plugin.getScoreboardManager().updateScoreboard(target);

                                                    player.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s island level to " + amount + ".");
                                                    target.sendMessage(ChatColor.GREEN + "Your island level has been set to " + amount + ".");
                                                })
                                )
                ))

                // /is add level <player> <amount>
                .withSubcommand(createSubCommand("add level <player> <amount>", "[Admin] Adds level to player island.",
                        new CommandAPICommand("add")
                                .withSubcommand(
                                        new CommandAPICommand("level")
                                                .withArguments(new PlayerArgument("target"))
                                                .withArguments(new IntegerArgument("amount", 1))
                                                .withPermission(CommandPermission.OP)
                                                .executesPlayer((player, args) -> {
                                                    Player target = (Player) args.get("target");
                                                    int amount = (int) args.get("amount");

                                                    Island targetIsland = plugin.getIslandManager().getIsland(target.getUniqueId());
                                                    if (targetIsland == null) {
                                                        player.sendMessage(ChatColor.RED + target.getName() + " does not have an island!");
                                                        return;
                                                    }

                                                    targetIsland.addLevel(amount);
                                                    plugin.getScoreboardManager().updateScoreboard(target);

                                                    int newLevel = targetIsland.getLevel();
                                                    player.sendMessage(ChatColor.GREEN + "Added " + amount + " levels to " + target.getName() + ". New level: " + newLevel);
                                                    target.sendMessage(ChatColor.GREEN + "Added " + amount + " levels to your island! New level: " + newLevel);
                                                })
                                )
                ))

                // /is
                .executesPlayer((player, args) -> {
                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                    if (island == null) {
                        sendHelpMenu(player);
                    } else {
                        teleportToIsland(plugin, player);
                    }
                })
                .register();
    }

    private static CommandAPICommand createSubCommand(String usage, String description, CommandAPICommand command) {
        HELP_MAP.put("/is " + usage, description);
        return command;
    }

    private static void teleportToIsland(SkyblockCore plugin, Player player) {
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island != null && island.getSpawnLocation() != null) {
            player.setFallDistance(0);
            player.teleport(island.getSpawnLocation());
            BorderManager.applyIslandBorder(player, island);
            player.sendMessage(ChatColor.GREEN + "Teleported to your island.");
        } else {
            player.sendMessage(ChatColor.RED + "Cannot find your island!");
        }
    }

    private static void sendHelpMenu(Player player) {
        player.sendMessage(ChatColor.GOLD + "========== [ SKYBLOCK COMMANDS ] ==========");

        for (Map.Entry<String, String> entry : HELP_MAP.entrySet()) {
            player.sendMessage(ChatColor.YELLOW + entry.getKey() + ChatColor.GRAY + " - " + entry.getValue());
        }

        player.sendMessage(ChatColor.GOLD + "==========================================");
    }

    private static void buildIslandStructure(World world, Location center, Player player, Island island) {
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = -3; x <= 2; x++) {
            for (int z = -3; z <= 2; z++) {

                boolean isBaseRectangle = (x >= -3 && x <= 2) && (z >= -3 && z <= -1);
                boolean isTopExtension = (x >= -3 && x <= -1) && (z >= 0 && z <= 2);

                if (isBaseRectangle || isTopExtension) {
                    world.getBlockAt(cx + x, cy - 2, cz + z).setType(Material.DIRT);
                    world.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.DIRT);
                    world.getBlockAt(cx + x, cy, cz + z).setType(Material.GRASS_BLOCK);
                }
            }
        }

        // Tree
        Location treeLoc = new Location(world, cx - 2, cy + 1, cz + 1);
        world.generateTree(treeLoc, TreeType.TREE);

        // Chest
        Location chestLoc = new Location(world, cx + 2, cy + 1, cz - 2);
        Block chestBlock = chestLoc.getBlock();
        chestBlock.setType(Material.CHEST);

        // Chest Items
        if (chestBlock.getState() instanceof Chest chest) {
            Inventory inv = chest.getInventory();
            inv.addItem(new ItemStack(Material.LAVA_BUCKET, 1));
            inv.addItem(new ItemStack(Material.ICE, 2));
        }

        // Chest Facing Direction
        if (chestBlock.getBlockData() instanceof Directional directional) {
            directional.setFacing(BlockFace.WEST);
            chestBlock.setBlockData(directional);
        }

        // spawnpoint
        Location spawnLocation = new Location(world, cx + 0.5, cy + 1, cz - 1.5, -90.0f, 15.0f);
        island.setSpawnLocation(spawnLocation);
        player.setBedSpawnLocation(spawnLocation, true);
        player.teleport(spawnLocation);
    }

    private static void handleWarpTeleportCommand(Player player, dev.jorel.commandapi.executors.CommandArguments args, SkyblockCore plugin) {
        String targetName = (String) args.get("target");
        String warpName = (String) args.get("warp");

        if (targetName == null || targetName.isEmpty()) {
            WarpMenuListener.openSelfTeleportWarpMenu(player);
            return;
        }

        if (warpName == null || warpName.isEmpty()) {
            WarpMenuListener.openVisitorWarpMenu(player, targetName);
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(targetName);
        Island island = plugin.getIslandManager().getIsland(targetOwner.getUniqueId());

        if (island == null) {
            player.sendMessage(ChatColor.RED + "Player or island not found!");
            return;
        }

        if (island.isLocked() && !player.isOp()) {
            player.sendMessage(ChatColor.RED + "This island is locked by its owner!");
            return;
        }

        Warp warp = island.getWarp(warpName);
        if (warp == null || (!warp.isVisible() && !player.isOp())) {
            player.sendMessage(ChatColor.RED + "Warp not found or disabled!");
            return;
        }

        player.setFallDistance(0);
        player.teleport(warp.getLocation());
        BorderManager.applyIslandBorder(player, island);
        player.sendMessage(ChatColor.GREEN + "Teleported to " + targetName + "'s warp: " + ChatColor.YELLOW + warp.getName());
    }
}