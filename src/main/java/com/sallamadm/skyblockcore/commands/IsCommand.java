package com.sallamadm.skyblockcore.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.border.BorderManager;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.data.DataManager;
import com.sallamadm.skyblockcore.events.IslandEvents;
import com.sallamadm.skyblockcore.gui.*;
import com.sallamadm.skyblockcore.island.InviteManager;
import com.sallamadm.skyblockcore.island.Island;
import com.sallamadm.skyblockcore.island.Warp;
import com.sallamadm.skyblockcore.island.enums.IslandPermissions;
import com.sallamadm.skyblockcore.island.enums.IslandRole;
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

        ArgumentSuggestions<CommandSender> likePeriodSuggestions =
                ArgumentSuggestions.strings("hafta", "ay", "hepsi");

        new CommandAPICommand("is")
                .withAliases("island")

                // /is level
                .withSubcommand(createSubCommand("level", "Ada levelinizi görün.",
                        new CommandAPICommand("level")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    player.sendMessage(msg.getMessage("island.level-info").replace("{level}", String.valueOf(island.getLevel())));
                                })
                ))

                // /is leveltop
                .withSubcommand(createSubCommand("leveltop", "En yüksek levelli adaları görün.",
                        new CommandAPICommand("leveltop")
                                .executesPlayer((player, args) -> {
                                    Map<String, Double> topLevels = plugin.getDataManager().getTopLeveledIslands();
                                    player.sendMessage(ChatColor.GOLD + "========== [ ADA LEVEL SIRALAMASI ] ==========");
                                    if (topLevels.isEmpty()) {
                                        player.sendMessage(msg.getMessage("level.no-levels"));
                                    } else {
                                        int rank = 1;
                                        for (Map.Entry<String, Double> entry : topLevels.entrySet()) {
                                            Island island = plugin.getIslandManager().getAllIslands().values().stream()
                                                    .filter(currentIsland -> currentIsland.getIslandUuid().equals(entry.getKey()))
                                                    .findFirst()
                                                    .orElse(null);
                                            if (island != null) {
                                                player.sendMessage(ChatColor.YELLOW + "#" + rank + ChatColor.GRAY + " " + island.getIslandName()
                                                        + ChatColor.GREEN + " - " + String.format(Locale.US, "%.2f", entry.getValue()));
                                                rank++;
                                            }
                                        }
                                    }
                                    player.sendMessage(ChatColor.GOLD + "===============================================");
                                })
                ))

                // /is setname <name>
                .withSubcommand(createSubCommand("setname <name>", "Adanızın adını değiştirin.",
                        new CommandAPICommand("setname")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.SET_NAME.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
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
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.SET_BIOME.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }
                                    BiomeMenu.openBiomeMenu(player);
                                })
                ))

                // /is create
                .withSubcommand(createSubCommand("create", "Yeni bir ada oluşturun.",
                        new CommandAPICommand("create")
                                .executesPlayer((player, args) -> {
                                    if (plugin.getIslandManager().getIslandByMember(player.getUniqueId()) != null) {
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

                                    island.seedDefaultPermissionsIfEmpty();

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

                // /is delete
                .withSubcommand(createSubCommand("top", "Ada sıralamasını görün.",
                        new CommandAPICommand("top")
                                .executesPlayer((player, args) -> {
                                    IsTopMenu.openTopMenu(player);
                                })
                ))

                // /is go
                .withSubcommand(createSubCommand("go", "Adanıza ışınlanın.",
                        new CommandAPICommand("go")
                                .executesPlayer((player, args) -> {
                                    teleportToIsland(plugin, player);
                                })
                ))

                // /is help
                .withSubcommand(createSubCommand("help [page]", "Ada komutları listeler.",
                        new CommandAPICommand("help")
                                .withOptionalArguments(new IntegerArgument("page", 1))
                                .executesPlayer((player, args) -> {
                                    int page = (int) args.getOrDefault("page", 1);
                                    sendHelpMenu(player, page);
                                })
                ))

                // /is lock
                .withSubcommand(createSubCommand("lock", "Ziyaretçilere adanızı kapatın.",
                        new CommandAPICommand("lock")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.LOCK_ISLAND.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
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
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.LOCK_ISLAND.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
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

                // /is rate <sayı>
                .withSubcommand(createSubCommand("rate <sayı>", "Bulunduğunuz adayı puanlayın.",
                        new CommandAPICommand("rate")
                                .withArguments(new StringArgument("rating"))
                                .executesPlayer((player, args) -> {
                                    double rating;
                                    try {
                                        rating = Double.parseDouble((String) args.get("rating"));
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(msg.getMessage("rating.invalid-number"));
                                        return;
                                    }

                                    if (Double.isNaN(rating) || Double.isInfinite(rating) || rating < 0D || rating > 5D) {
                                        player.sendMessage(msg.getMessage("rating.invalid-number"));
                                        return;
                                    }

                                    World skyblockWorld = plugin.getWorldManager().getSkyblockWorld();
                                    if (skyblockWorld == null || !player.getWorld().equals(skyblockWorld)) {
                                        player.sendMessage(msg.getMessage("rating.wrong-world"));
                                        return;
                                    }

                                    Island targetIsland = plugin.getIslandManager().getIslandAt(player.getLocation());
                                    if (targetIsland == null) {
                                        player.sendMessage(msg.getMessage("rating.no-island-here"));
                                        return;
                                    }

                                    Island playerIsland = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (targetIsland == playerIsland) {
                                        player.sendMessage(msg.getMessage("rating.cannot-rate-own"));
                                        return;
                                    }

                                    DataManager.RateResult result = plugin.getDataManager().addIslandRating(player.getUniqueId(), targetIsland.getIslandUuid(), rating);
                                    if (result == DataManager.RateResult.ALREADY_RATED) {
                                        player.sendMessage(msg.getMessage("rating.already-rated"));
                                        return;
                                    }
                                    if (result == DataManager.RateResult.DATABASE_ERROR) {
                                        player.sendMessage(msg.getMessage("rating.database-error"));
                                        return;
                                    }

                                    player.sendMessage(msg.getMessage("rating.success")
                                            .replace("{island}", targetIsland.getIslandName())
                                            .replace("{rating}", String.format(Locale.US, "%.2f", rating)));
                                    Player owner = Bukkit.getPlayer(targetIsland.getOwnerUUID());
                                    if (owner != null) {
                                        plugin.getScoreboardManager().updateScoreboard(owner);
                                    }
                                })
                ))

                // /is rating
                .withSubcommand(createSubCommand("rating", "Ada ratinginizi görün.",
                        new CommandAPICommand("rating")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    double rating = plugin.getDataManager().getIslandRating(island.getIslandUuid());
                                    player.sendMessage(msg.getMessage("rating.stats")
                                            .replace("{rating}", String.format(Locale.US, "%.2f", rating)));
                                })
                ))

                // /is ratingtop
                .withSubcommand(createSubCommand("ratingtop", "En yüksek ratingli adaları görün.",
                        new CommandAPICommand("ratingtop")
                                .executesPlayer((player, args) -> {
                                    Map<String, Double> topRatings = plugin.getDataManager().getTopRatedIslands();
                                    player.sendMessage(ChatColor.GOLD + "========== [ ADA RATING SIRALAMASI ] ==========");
                                    if (topRatings.isEmpty()) {
                                        player.sendMessage(msg.getMessage("rating.no-ratings"));
                                    } else {
                                        int rank = 1;
                                        for (Map.Entry<String, Double> entry : topRatings.entrySet()) {
                                            Island island = plugin.getIslandManager().getAllIslands().values().stream()
                                                    .filter(currentIsland -> currentIsland.getIslandUuid().equals(entry.getKey()))
                                                    .findFirst()
                                                    .orElse(null);
                                            if (island != null) {
                                                player.sendMessage(ChatColor.YELLOW + "#" + rank + ChatColor.GRAY + " " + island.getIslandName()
                                                        + ChatColor.AQUA + " - " + String.format(Locale.US, "%.2f", entry.getValue()));
                                                rank++;
                                            }
                                        }
                                    }
                                    player.sendMessage(ChatColor.GOLD + "===============================================");
                                })
                ))

                // /is like
                .withSubcommand(createSubCommand("like", "Bulunduğunuz adayı beğenin.",
                        new CommandAPICommand("like")
                                .executesPlayer((player, args) -> {
                                    World skyblockWorld = plugin.getWorldManager().getSkyblockWorld();
                                    if (skyblockWorld == null || !player.getWorld().equals(skyblockWorld)) {
                                        player.sendMessage(msg.getMessage("likes.wrong-world"));
                                        return;
                                    }

                                    Island targetIsland = plugin.getIslandManager().getIslandAt(player.getLocation());
                                    if (targetIsland == null) {
                                        player.sendMessage(msg.getMessage("likes.no-island-here"));
                                        return;
                                    }

                                    Island playerIsland = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (targetIsland == playerIsland) {
                                        player.sendMessage(msg.getMessage("likes.cannot-like-own"));
                                        return;
                                    }

                                    DataManager.LikeResult result = plugin.getDataManager().addIslandLike(player.getUniqueId(), targetIsland.getIslandUuid());
                                    if (result == DataManager.LikeResult.ALREADY_LIKED_THIS_WEEK) {
                                        player.sendMessage(msg.getMessage("likes.already-liked-this-week"));
                                        return;
                                    }
                                    if (result == DataManager.LikeResult.MONTHLY_LIMIT_REACHED) {
                                        player.sendMessage(msg.getMessage("likes.monthly-limit"));
                                        return;
                                    }
                                    if (result == DataManager.LikeResult.DATABASE_ERROR) {
                                        player.sendMessage(msg.getMessage("likes.database-error"));
                                        return;
                                    }

                                    player.sendMessage(msg.getMessage("likes.success").replace("{island}", targetIsland.getIslandName()));
                                    Player owner = Bukkit.getPlayer(targetIsland.getOwnerUUID());
                                    if (owner != null) {
                                        plugin.getScoreboardManager().updateScoreboard(owner);
                                    }
                                })
                ))

                // /is likes <hafta/ay/hepsi>
                .withSubcommand(createSubCommand("likes <hafta/ay/hepsi>", "Adanızın beğeni istatistiklerinizi görün.",
                        new CommandAPICommand("likes")
                                .withArguments(new StringArgument("period").replaceSuggestions(likePeriodSuggestions))
                                .executesPlayer((player, args) -> {
                                    String period = ((String) args.get("period")).toLowerCase(Locale.ROOT);
                                    if (!period.equals("hafta") && !period.equals("ay") && !period.equals("hepsi")) {
                                        player.sendMessage(msg.getMessage("likes.invalid-period"));
                                        return;
                                    }

                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    int likes = period.equals("hafta") ? plugin.getDataManager().getWeeklyLikeCount(island.getIslandUuid())
                                            : period.equals("ay") ? plugin.getDataManager().getMonthlyLikeCount(island.getIslandUuid())
                                              : plugin.getDataManager().getTotalLikeCount(island.getIslandUuid());
                                    player.sendMessage(msg.getMessage("likes.stats")
                                            .replace("{period}", period)
                                            .replace("{likes}", String.valueOf(likes)));
                                })
                ))

                // /is liketop <hafta/ay/hepsi>
                .withSubcommand(createSubCommand("liketop <hafta/ay/hepsi>", "En çok beğeni alan adaları görün.",
                        new CommandAPICommand("liketop")
                                .withArguments(new StringArgument("period").replaceSuggestions(likePeriodSuggestions))
                                .executesPlayer((player, args) -> {
                                    String period = ((String) args.get("period")).toLowerCase(Locale.ROOT);
                                    if (!period.equals("hafta") && !period.equals("ay") && !period.equals("hepsi")) {
                                        player.sendMessage(msg.getMessage("likes.invalid-top-period"));
                                        return;
                                    }

                                    Map<String, Integer> topLikes = plugin.getDataManager().getTopLikedIslands(period);
                                    player.sendMessage(ChatColor.GOLD + "========== [ " + period.toUpperCase(Locale.ROOT) + "LIK ADA SIRALAMASI ] ==========");
                                    if (topLikes.isEmpty()) {
                                        player.sendMessage(msg.getMessage("likes.no-likes"));
                                    } else {
                                        int rank = 1;
                                        for (Map.Entry<String, Integer> entry : topLikes.entrySet()) {
                                            Island island = plugin.getIslandManager().getAllIslands().values().stream()
                                                    .filter(currentIsland -> currentIsland.getIslandUuid().equals(entry.getKey()))
                                                    .findFirst()
                                                    .orElse(null);
                                            if (island != null) {
                                                player.sendMessage(ChatColor.YELLOW + "#" + rank + ChatColor.GRAY + " " + island.getIslandName()
                                                        + ChatColor.LIGHT_PURPLE + " - " + entry.getValue() + " like");
                                                rank++;
                                            }
                                        }
                                    }
                                    player.sendMessage(ChatColor.GOLD + "================================================");
                                })
                ))

                // /is setwarp
                .withSubcommand(createSubCommand("setwarp <name>", "Adanıza yeni warp oluşturun.",
                        new CommandAPICommand("setwarp")
                                .withArguments(new StringArgument("name"))
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.MANAGE_WARP.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
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
                                    if(player.isFlying()) {
                                        player.sendMessage(msg.getMessage("fly.dont-fly"));
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
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.MANAGE_WARP.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
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
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.MANAGE_WARP.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }
                                    WarpMenu.openOwnerWarpMenu(player);
                                })
                ))

                // /is visit
                .withSubcommand(createSubCommand("visit <target> [warp]", "Başka birinin adasını ziyaret edin.",
                        new CommandAPICommand("visit")
                                .withOptionalArguments(new StringArgument("target")) //replaceSuggestions(onlinePlayerSuggestions)
                                .withOptionalArguments(new StringArgument("warp").replaceSuggestions(warpSuggestions))
                                .executesPlayer((player, args) -> {
                                    handleWarpTeleportCommand(player, args, plugin);
                                })
                ))

                // /is warp
                .withSubcommand(createSubCommand("warp <target> [warp]", "Warp'a ışınlanın.",
                        new CommandAPICommand("warp")
                                .withOptionalArguments(new StringArgument("target")) //.replaceSuggestions(onlinePlayerSuggestions)
                                .withOptionalArguments(new StringArgument("warp").replaceSuggestions(warpSuggestions))
                                .executesPlayer((player, args) -> {
                                    handleWarpTeleportCommand(player, args, plugin);
                                })
                ))


                // /is kick <target>
                .withSubcommand(createSubCommand("kick <target>", "Oyuncuyu adadan atın.",
                        new CommandAPICommand("kick")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((player, args) -> {
                                    Player target = (Player) args.get("target");
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());

                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.KICK.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }
                                    if (player.equals(target)) {
                                        player.sendMessage(msg.getMessage("island.cannot-kick-self"));
                                        return;
                                    }
                                    if (target.isOp()) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }
                                    int actorTier = island.getRoleTier(player.getUniqueId());
                                    int targetTier = island.getRoleTier(target.getUniqueId());
                                    if (targetTier <= actorTier) {
                                        player.sendMessage(msg.getMessage("island.cannot-kick-higher-rank"));
                                        return;
                                    }
                                    if (!isPlayerOnIsland(target, island)) {
                                        player.sendMessage(msg.getMessage("island.target-not-on-island"));
                                        return;
                                    }

                                    teleportOutFromIsland(plugin, target);
                                    target.sendMessage(msg.getMessage("island.kicked"));
                                    player.sendMessage(msg.getMessage("island.kick-success").replace("{target}", target.getName()));
                                })
                ))

                // /is ban <target>
                .withSubcommand(createSubCommand("ban <target>", "Oyunucu adanıza girişini engelleyin.",
                        new CommandAPICommand("ban")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((player, args) -> {
                                    Player target = (Player) args.get("target");
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());

                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.BAN.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }
                                    if (player.equals(target)) {
                                        player.sendMessage(msg.getMessage("island.cannot-ban-self"));
                                        return;
                                    }
                                    if (target.isOp()) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }

                                    int actorTier = island.getRoleTier(player.getUniqueId());
                                    int targetTier = island.getRoleTier(target.getUniqueId());
                                    if (targetTier <= actorTier) {
                                        player.sendMessage(msg.getMessage("island.cannot-ban-higher-rank"));
                                        return;
                                    }

                                    if (island.getMemberRoles().containsKey(target.getUniqueId())) {
                                        island.removeMember(target.getUniqueId());
                                    }

                                    island.banPlayer(target.getUniqueId());

                                    if (isPlayerOnIsland(target, island)) {
                                        teleportOutFromIsland(plugin, target);
                                    }

                                    target.sendMessage(msg.getMessage("island.banned"));
                                    player.sendMessage(msg.getMessage("island.ban-success").replace("{target}", target.getName()));
                                })
                ))

                // /is unban <target>
                .withSubcommand(createSubCommand("unban <target>", "Oyuncunun ada banini kaldirin.",
                        new CommandAPICommand("unban")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((player, args) -> {
                                    Player target = (Player) args.get("target");
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());

                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.UNBAN.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }
                                    if (!island.isBanned(target.getUniqueId())) {
                                        player.sendMessage(msg.getMessage("island.not-banned"));
                                        return;
                                    }

                                    island.unbanPlayer(target.getUniqueId());
                                    player.sendMessage(msg.getMessage("island.unban-success").replace("{target}", target.getName()));
                                })
                ))

                // /is setspawn
                .withSubcommand(createSubCommand("setspawn", "Adanızın spawn noktasını ayarlayın.",
                        new CommandAPICommand("setspawn")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.SET_SPAWN.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }

                                    Location loc = player.getLocation();
                                    Location center = island.getCenterLocation();
                                    if (center == null || !loc.getWorld().equals(center.getWorld())) {
                                        player.sendMessage(msg.getMessage("island.must-be-on-island"));
                                        return;
                                    }
                                    int radius = island.getIslandSize() / 2;
                                    if (Math.abs(loc.getBlockX() - center.getBlockX()) > radius || Math.abs(loc.getBlockZ() - center.getBlockZ()) > radius) {
                                        player.sendMessage(msg.getMessage("island.outside-boundary"));
                                        return;
                                    }

                                    if(player.isFlying()) {
                                        player.sendMessage(msg.getMessage("fly.dont-fly"));
                                        return;
                                    }
                                    island.setSpawnLocation(loc);
                                    plugin.getDataManager().saveData();

                                    player.sendMessage(msg.getMessage("island.spawn-set"));
                                })
                ))


                // /is permissions
                .withSubcommand(createSubCommand("permissions", "[Admin] Rol yetkilerini düzenleyin.",
                        new CommandAPICommand("permissions")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    PermissionsMenu.openRoleSelectMenu(player, island);
                                })
                ))

                // /is gamerules
                .withSubcommand(createSubCommand("gamerules", "Ada gamerule ayarlarınızı düzenleyin.",
                        new CommandAPICommand("gamerules")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.MANAGE_GAMERULES.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }
                                    GameruleMenu.openGameruleMenu(player, island);
                                })
                ))

                // /is weather
                .withSubcommand(createSubCommand("weather", "Ada hava durumu/zaman ayarlarınızı düzenleyin.",
                        new CommandAPICommand("weather")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.MANAGE_WEATHER.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }
                                    WeatherMenu.openWeatherMenu(player, island);
                                })
                ))

                // /is invite <target>
                .withSubcommand(createSubCommand("invite <target>", "Adanıza oyuncu davet edin.",
                        new CommandAPICommand("invite")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((player, args) -> {
                                    Player target = (Player) args.get("target");

                                    if (target.getUniqueId().equals(player.getUniqueId())) {
                                        player.sendMessage(msg.getMessage("invite.cannot-invite-self"));
                                        return;
                                    }

                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }

                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.INVITE.getNode())) {
                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                        return;
                                    }

                                    if (plugin.getIslandManager().getIslandByMember(target.getUniqueId()) != null) {
                                        player.sendMessage(msg.getMessage("invite.target-has-island").replace("{target}", target.getName()));
                                        return;
                                    }

                                    if (plugin.getInviteManager().hasPendingInvite(target.getUniqueId())) {
                                        player.sendMessage(msg.getMessage("invite.already-pending").replace("{target}", target.getName()));
                                        return;
                                    }

                                    plugin.getInviteManager().createInvite(player, target, island);

                                    player.sendMessage(msg.getMessage("invite.sent").replace("{target}", target.getName()));
                                    target.sendMessage(msg.getMessage("invite.received")
                                            .replace("{inviter}", player.getName())
                                            .replace("{island}", island.getIslandName()));
                                })
                ))

                // /is confirm
                .withSubcommand(createSubCommand("confirm", "Bekleyen ada davetini kabul edin.",
                        new CommandAPICommand("confirm")
                                .executesPlayer((player, args) -> {
                                    InviteManager inviteManager = plugin.getInviteManager();
                                    InviteManager.PendingInvite invite = inviteManager.getPendingInvite(player.getUniqueId());

                                    if (invite == null) {
                                        player.sendMessage(msg.getMessage("invite.no-pending"));
                                        return;
                                    }

                                    if (plugin.getIslandManager().getIslandByMember(player.getUniqueId()) != null) {
                                        inviteManager.removeInvite(player.getUniqueId());
                                        player.sendMessage(msg.getMessage("invite.auto-rejected-has-island"));
                                        return;
                                    }

                                    Island island = plugin.getIslandManager().getIsland(invite.islandOwnerUUID);
                                    inviteManager.removeInvite(player.getUniqueId());

                                    if (island == null || island.getSpawnLocation() == null) {
                                        player.sendMessage(msg.getMessage("general.island-not-found"));
                                        return;
                                    }

                                    island.addOrUpdateMember(player.getUniqueId(), IslandRole.MEMBER, null);

                                    player.setFallDistance(0);
                                    player.teleport(island.getSpawnLocation());
                                    BorderManager.applyIslandBorder(player, island);

                                    player.sendMessage(msg.getMessage("invite.accepted").replace("{island}", island.getIslandName()));

                                    Player inviterPlayer = Bukkit.getPlayer(invite.inviterUUID);
                                    if (inviterPlayer != null && inviterPlayer.isOnline()) {
                                        inviterPlayer.sendMessage(msg.getMessage("invite.accepted-notify-inviter")
                                                .replace("{target}", player.getName()));
                                    }
                                })
                ))

                // /is reject
                .withSubcommand(createSubCommand("reject", "Bekleyen ada davetini reddedin.",
                        new CommandAPICommand("reject")
                                .executesPlayer((player, args) -> {
                                    InviteManager inviteManager = plugin.getInviteManager();
                                    InviteManager.PendingInvite invite = inviteManager.getPendingInvite(player.getUniqueId());

                                    if (invite == null) {
                                        player.sendMessage(msg.getMessage("invite.no-pending"));
                                        return;
                                    }

                                    inviteManager.removeInvite(player.getUniqueId());
                                    player.sendMessage(msg.getMessage("invite.rejected"));

                                    Player inviterPlayer = Bukkit.getPlayer(invite.inviterUUID);
                                    if (inviterPlayer != null && inviterPlayer.isOnline()) {
                                        inviterPlayer.sendMessage(msg.getMessage("invite.rejected-notify-inviter")
                                                .replace("{target}", player.getName()));
                                    }
                                })
                ))

                // /is members
                .withSubcommand(createSubCommand("members", "Ada üyelerini görüntüleyin ve rollerini düzenleyin.",
                        new CommandAPICommand("members")
                                .executesPlayer((player, args) -> {
                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                    if (island == null) {
                                        player.sendMessage(msg.getMessage("island.no-island"));
                                        return;
                                    }
                                    MembersMenu.openMembersMenu(player, island);
                                })
                ))

                // /is member kick <member>
                .withSubcommand(createSubCommand("member kick <member>", "Üyeyi adadan çıkarın.",
                        new CommandAPICommand("member")
                                .withSubcommand(
                                        new CommandAPICommand("kick")
                                                .withArguments(new StringArgument("member"))
                                                .executesPlayer((player, args) -> {
                                                    String memberName = (String) args.get("member");

                                                    Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
                                                    if (island == null) {
                                                        player.sendMessage(msg.getMessage("island.no-island"));
                                                        return;
                                                    }

                                                    @SuppressWarnings("deprecation")
                                                    OfflinePlayer target = Bukkit.getOfflinePlayer(memberName);
                                                    UUID targetUuid = target.getUniqueId();

                                                    if (targetUuid.equals(island.getOwnerUUID())) {
                                                        player.sendMessage(msg.getMessage("members.cannot-kick-owner"));
                                                        return;
                                                    }

                                                    if (!island.getMemberRoles().containsKey(targetUuid)) {
                                                        player.sendMessage(msg.getMessage("members.not-a-member"));
                                                        return;
                                                    }

                                                    if (!island.hasPermission(player.getUniqueId(), IslandPermissions.KICK.getNode())) {
                                                        player.sendMessage(msg.getMessage("general.no-permission"));
                                                        return;
                                                    }

                                                    int actorTier = island.getRoleTier(player.getUniqueId());
                                                    int targetTier = island.getRoleTier(targetUuid);
                                                    if (targetTier <= actorTier) {
                                                        player.sendMessage(msg.getMessage("members.cannot-kick-higher-rank"));
                                                        return;
                                                    }

                                                    island.removeMember(targetUuid);

                                                    String targetName = target.getName() != null ? target.getName() : memberName;
                                                    player.sendMessage(msg.getMessage("members.kick-success").replace("{target}", targetName));

                                                    Player targetOnline = target.getPlayer();
                                                    if (targetOnline != null && targetOnline.isOnline()) {
                                                        targetOnline.sendMessage(msg.getMessage("members.kicked-notify"));
                                                        if (island.isWithinBounds(targetOnline.getLocation())) {
                                                            targetOnline.setFallDistance(0);
                                                            BorderManager.removeBorder(targetOnline);
                                                            targetOnline.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                                                        }
                                                    }
                                                })
                                )
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

                                                    Island targetIsland = plugin.getIslandManager().getIslandByMember(target.getUniqueId());
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

                                                    Island targetIsland = plugin.getIslandManager().getIslandByMember(target.getUniqueId());
                                                    if (targetIsland == null) {
                                                        player.sendMessage(msg.getMessage("admin.target-no-island").replace("{target}", target.getName()));
                                                        return;
                                                    }

                                                    targetIsland.addLevel(amount);
                                                    plugin.getScoreboardManager().updateScoreboard(target);

                                                    double newLevel = targetIsland.getLevel();
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
        Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
        if (island != null && island.getSpawnLocation() != null) {
            player.setFallDistance(0);
            player.teleport(island.getSpawnLocation());
            BorderManager.applyIslandBorder(player, island);
            player.sendMessage(msg.getMessage("island.teleported"));
        } else {
            player.sendMessage(msg.getMessage("island.no-island"));
        }
    }

    private static void sendHelpMenu(Player player, int page) {
        List<Map.Entry<String, String>> helpList = new ArrayList<>(HELP_MAP.entrySet());
        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) helpList.size() / pageSize);

        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        player.sendMessage(ChatColor.GOLD + "========== [ SKYBLOCK KOMUTLARI (Sayfa " + page + "/" + Math.max(totalPages, 1) + ") ] ==========");

        if (helpList.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Gösterilecek komut bulunamadı.");
        } else {
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, helpList.size());

            for (int i = startIndex; i < endIndex; i++) {
                Map.Entry<String, String> entry = helpList.get(i);
                player.sendMessage(ChatColor.YELLOW + entry.getKey() + ChatColor.GRAY + " - " + entry.getValue());
            }
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

        Block bedrockBlock = world.getBlockAt(spawnLocation.getBlockX(), spawnLocation.getBlockY() - 4, spawnLocation.getBlockZ());
        bedrockBlock.setType(Material.BEDROCK);
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
        Island island = plugin.getIslandManager().getIslandByMember(player.getUniqueId());

        if (island == null) {
            player.sendMessage(msg.getMessage("general.player-not-found"));
            return;
        }
        if (warpName == null || warpName.isEmpty()) {
            WarpMenu.openVisitorWarpMenu(player, targetName);
            return;
        }
        if (island.isBanned(player.getUniqueId()) && !player.isOp()) {
            player.sendMessage(msg.getMessage("island.banned"));
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

    private static boolean isPlayerOnIsland(Player player, Island island) {
        Location loc = player.getLocation();
        if (island.getCenterLocation() == null || !loc.getWorld().equals(island.getCenterLocation().getWorld())) {
            return false;
        }
        int radius = island.getIslandSize() / 2;
        Location center = island.getCenterLocation();
        return Math.abs(loc.getBlockX() - center.getBlockX()) <= radius &&
                Math.abs(loc.getBlockZ() - center.getBlockZ()) <= radius;
    }

    private static void teleportOutFromIsland(SkyblockCore plugin, Player player) {
        Island playerIsland = plugin.getIslandManager().getIslandByMember(player.getUniqueId());
        player.setFallDistance(0);
        BorderManager.removeBorder(player);

        if (playerIsland != null && playerIsland.getSpawnLocation() != null) {
            player.teleport(playerIsland.getSpawnLocation());
            BorderManager.applyIslandBorder(player, playerIsland);
        } else {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }
}