package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.border.BorderManager;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.events.IslandEvents;
import com.sallamadm.skyblockcore.gui.IslandDeleteMenu;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.Warp;
import com.sallamadm.skyblockcore.gui.IsMenu;
import com.sallamadm.skyblockcore.gui.BiomeMenu;
import com.sallamadm.skyblockcore.gui.WarpMenu;
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

import java.util.*;

import static org.bukkit.block.Biome.PLAINS;

public class IsCommand {
    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final Map<String, String> HELP_MAP = new LinkedHashMap<>();

    public static void registerCommand(SkyblockCore plugin) {
        HELP_MAP.clear();

        /*ArgumentSuggestions<CommandSender> onlinePlayerSuggestions = ArgumentSuggestions.strings(info ->
                Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)
        );
        is visit ve warpa auto complete fakat neden calısmadı bılmyorum
        */

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
                .withSubcommand(createSubCommand("level", "Ada levelinizi görün.",
                        new CommandAPICommand("level")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    player.sendMessage(msg.getMessage("island.level-info").replace("{level}", String.valueOf(island.getLevel())));
                                })
                ))

                // /is setname <name>
                .withSubcommand(createSubCommand("setname <name>", "Adanızın adını değiştirin.",
                        new CommandAPICommand("setname")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    String newName = (String) args.get("name");
                                    island.setIslandName(newName);
                                    plugin.getDataManager().saveData();
                                    player.sendMessage(msg.getMessage("island.name-changed").replace("{name}", newName));
                                })
                ))

                // /is biome
                .withSubcommand(createSubCommand("biome", "Adanızın biyomunu değiştirin.",
                        new CommandAPICommand("biome")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    BiomeMenu.openBiomeMenu(player);
                                })
                ))

                // /is create
                .withSubcommand(createSubCommand("create", "Yeni bir ada oluşturun.",
                        new CommandAPICommand("create")
                                .executesPlayer((player, args) -> {
                                    if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
                                        player.sendMessage(msg.getMessage("island.already-has-island"));
                                        return;
                                    }

                                    World skyblockWorld = plugin.getWorldManager().getSkyblockWorld();
                                    if (skyblockWorld == null) {
                                        player.sendMessage(msg.getMessage("island.cannot-load-world"));
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

                                    player.sendMessage(msg.getMessage("island.created"));
                                    BiomeMenu.changeIslandBiome(island, PLAINS);
                                })
                ))

                // /is delete
                .withSubcommand(createSubCommand("delete", "Adanızı silin.",
                        new CommandAPICommand("delete")
                                .executesPlayer((player, args) -> {
                                    Island islandToDelete = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (islandToDelete == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    IslandDeleteMenu.openConfirmMenu(player);
                                })
                ))

                // /is go
                .withSubcommand(createSubCommand("go", "Adanıza ışınlanın.",
                        new CommandAPICommand("go")
                                .executesPlayer((player, args) -> {
                                    if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
                                        teleportToIsland(plugin, player);
                                    } else {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                    }
                                })
                ))

                // /is help
                .withSubcommand(createSubCommand("help", "Ada komutları listeler.",
                        new CommandAPICommand("help")
                                .executesPlayer((player, args) -> {
                                    sendHelpMenu(player);
                                })
                ))

                // /is lock
                .withSubcommand(createSubCommand("lock", "Ziyaretçilere adanızı kapatın.",
                        new CommandAPICommand("lock")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (island.isLocked()) {
                                        player.sendMessage(msg.getMessage("island.already-locked"));
                                        return;
                                    }

                                    island.setLocked(true);
                                    player.sendMessage(msg.getMessage("island.locked"));
                                })
                ))

                // /is unlock
                .withSubcommand(createSubCommand("unlock", "Adanızı ziyaretçilere açın.",
                        new CommandAPICommand("unlock")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.isLocked()) {
                                        player.sendMessage(msg.getMessage("island.already-unlocked"));
                                        return;
                                    }

                                    island.setLocked(false);
                                    player.sendMessage(msg.getMessage("island.unlocked"));
                                })
                ))

                // /is setwarp
                .withSubcommand(createSubCommand("setwarp <name>", "Adanıza yeni warp oluşturun.",
                        new CommandAPICommand("setwarp")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    Location loc = player.getLocation();
                                    Location center = island.getCenterLocation();
                                    if (center == null || !loc.getWorld().equals(center.getWorld())) {
                                        player.sendMessage(msg.getMessage("warp.must-be-on-island"));
                                        return;
                                    }

                                    int radius = island.getIslandSize() / 2;
                                    if (Math.abs(loc.getBlockX() - center.getBlockX()) > radius || Math.abs(loc.getBlockZ() - center.getBlockZ()) > radius) {
                                        player.sendMessage(msg.getMessage("warp.outside-boundary"));
                                        return;
                                    }

                                    String warpName = (String) args.get("name");
                                    Warp warp = new Warp(warpName, loc);
                                    island.addWarp(warp);
                                    player.sendMessage(msg.getMessage("warp.created").replace("{warp}", warpName));
                                })
                ))

                // /is delwarp
                .withSubcommand(createSubCommand("delwarp <name>", "Belirtilen isimdeki warpınızı silin.",
                        new CommandAPICommand("delwarp")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    String warpName = (String) args.get("name");
                                    if (island.getWarp(warpName) == null) {
                                        player.sendMessage(msg.getMessage("warp.not-found").replace("{warp}", warpName));
                                        return;
                                    }

                                    island.removeWarp(warpName);
                                    player.sendMessage(msg.getMessage("warp.deleted").replace("{warp}", warpName));
                                })
                ))

                // /is warps
                .withSubcommand(createSubCommand("warps", "Ada warplarınızı düzenleyin.",
                        new CommandAPICommand("warps")
                                .executesPlayer((player, args) -> {
                                    WarpMenu.openOwnerWarpMenu(player);
                                })
                ))

                // /is visit
                .withSubcommand(createSubCommand("visit <target> [warp]", "Başka birinin adasını ziyaret edin.",
                        new CommandAPICommand("visit")
                                .withArguments(new StringArgument("target")) //replaceSuggestions(onlinePlayerSuggestions)
                                .withOptionalArguments(new StringArgument("warp").replaceSuggestions(warpSuggestions))
                                .executesPlayer((player, args) -> {
                                    handleWarpTeleportCommand(player, args, plugin);
                                })
                ))

                // /is warp
                .withSubcommand(createSubCommand("warp <target> [warp]", "Warp'a ışınlanın.",
                        new CommandAPICommand("warp")
                                .withArguments(new StringArgument("target")) //.replaceSuggestions(onlinePlayerSuggestions)
                                .withOptionalArguments(new StringArgument("warp").replaceSuggestions(warpSuggestions))
                                .executesPlayer((player, args) -> {
                                    handleWarpTeleportCommand(player, args, plugin);
                                })
                ))

                // ADMIN COMMANDS

                // /is set level <player> <amount>
                .withSubcommand(createSubCommand("set level <player> <amount>", "[Admin] Oyuncu ada levelini ayarlayın.",
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
                                                        player.sendMessage(msg.getMessage("admin.target-no-island").replace("{target}", target.getName()));
                                                        return;
                                                    }

                                                    targetIsland.setLevel(amount);
                                                    plugin.getScoreboardManager().updateScoreboard(target);

                                                    player.sendMessage(msg.getMessage("admin.level-set-sender")
                                                            .replace("{target}", target.getName())
                                                            .replace("{level}", String.valueOf(amount)));

                                                    target.sendMessage(msg.getMessage("admin.level-set-target")
                                                            .replace("{level}", String.valueOf(amount)));
                                                })
                                )
                ))

                // /is add level <player> <amount>
                .withSubcommand(createSubCommand("add level <player> <amount>", "[Admin] Oyuncuya ada leveli ekleyin.",
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
                                                        player.sendMessage(msg.getMessage("admin.target-no-island").replace("{target}", target.getName()));
                                                        return;
                                                    }

                                                    targetIsland.addLevel(amount);
                                                    plugin.getScoreboardManager().updateScoreboard(target);

                                                    int newLevel = targetIsland.getLevel();
                                                    player.sendMessage(msg.getMessage("admin.level-add-sender")
                                                            .replace("{target}", target.getName())
                                                            .replace("{amount}", String.valueOf(amount))
                                                            .replace("{level}", String.valueOf(newLevel)));

                                                    target.sendMessage(msg.getMessage("admin.level-add-target")
                                                            .replace("{amount}", String.valueOf(amount))
                                                            .replace("{level}", String.valueOf(newLevel)));
                                                })
                                )
                ))

                // /is
                .executesPlayer((player, args) -> {
                    IsMenu.openIsMenu(player);
                })
                .register();
    }

    private static CommandAPICommand createSubCommand(String usage, String description, CommandAPICommand command) {
        HELP_MAP.put("/is " + usage, description);
        return command;
    }

    public static void teleportToIsland(SkyblockCore plugin, Player player) {
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island != null && island.getSpawnLocation() != null) {
            player.setFallDistance(0);
            player.teleport(island.getSpawnLocation());
            BorderManager.applyIslandBorder(player, island);
            player.sendMessage(msg.getMessage("island.teleported"));
        } else {
            player.sendMessage(msg.getMessage("island.no-island"));
        }
    }

    private static void sendHelpMenu(Player player) {
        player.sendMessage(ChatColor.GOLD + "========== [ SKYBLOCK KOMUTLARI ] ==========");

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
        SkyblockCore.getInstance().getDataManager().saveData();
    }

    private static void handleWarpTeleportCommand(Player player, dev.jorel.commandapi.executors.CommandArguments args, SkyblockCore plugin) {
        String targetName = (String) args.get("target");
        String warpName = (String) args.get("warp");

        if (targetName == null || targetName.isEmpty()) {
            player.sendMessage(msg.getMessage("general.player-not-found"));
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(targetName);
        Island island = plugin.getIslandManager().getIsland(targetOwner.getUniqueId());

        if (island == null) {
            player.sendMessage(msg.getMessage("general.player-not-found"));
            return;
        }
        if (warpName == null || warpName.isEmpty()) {
            WarpMenu.openVisitorWarpMenu(player, targetName);
            return;
        }
        if (island.isLocked() && !player.isOp()) {
            player.sendMessage(msg.getMessage("island.locked-by-owner"));
            return;
        }

        Warp warp = island.getWarp(warpName);
        if (warp == null || (!warp.isVisible() && !player.isOp())) {
            player.sendMessage(msg.getMessage("warp.not-found").replace("{warp}", warpName));
            return;
        }

        player.setFallDistance(0);
        player.teleport(warp.getLocation());
        BorderManager.applyIslandBorder(player, island);
        player.sendMessage(msg.getMessage("warp.teleported")
                .replace("{target}", targetName)
                .replace("{warp}", warp.getName()));
    }
}