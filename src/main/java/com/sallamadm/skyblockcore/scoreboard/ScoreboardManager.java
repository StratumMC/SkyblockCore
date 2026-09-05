package com.sallamadm.skyblockcore.scoreboard;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Locale;

public class ScoreboardManager {
    private final SkyblockCore plugin;

    public ScoreboardManager(SkyblockCore plugin) {
        this.plugin = plugin;
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("skyblock", Criteria.DUMMY, ChatColor.GOLD + "" + ChatColor.BOLD + "SKYBLOCK");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        double level = (island != null) ? island.getLevel() : 0D;
        int likes = (island != null) ? plugin.getDataManager().getTotalLikeCount(island.getIslandUuid()) : 0;
        double rating = (island != null) ? plugin.getDataManager().getIslandRating(island.getIslandUuid()) : 0D;

        String flyDisplay = player.isOp()
                ? "0"
                : formatFlyDuration(plugin.getFlightManager().getRemainingSeconds(player.getUniqueId()));

        Score blank1 = obj.getScore(" ");
        Score likeLine = obj.getScore(ChatColor.WHITE + "Ada Like: " + ChatColor.LIGHT_PURPLE + likes);
        Score ratingLine = obj.getScore(ChatColor.WHITE + "Ada Rating: " + ChatColor.AQUA + String.format(Locale.US, "%.2f", rating));
        Score playerLine = obj.getScore(ChatColor.WHITE + "Oyuncu: " + ChatColor.YELLOW + player.getName());
        Score levelLine = obj.getScore(ChatColor.WHITE + "Ada leveli: " + ChatColor.GREEN + level);
        Score flyLine = obj.getScore(ChatColor.WHITE + "Fly süresi: " + ChatColor.AQUA + flyDisplay);
        Score blank2 = obj.getScore("  ");
        Score footer = obj.getScore(ChatColor.GRAY + "localhost");

        blank1.setScore(9);
        playerLine.setScore(8);
        levelLine.setScore(7);
        likeLine.setScore(6);
        ratingLine.setScore(5);
        flyLine.setScore(3);
        blank2.setScore(2);
        footer.setScore(1);

        player.setScoreboard(board);
    }

    private String formatFlyDuration(long totalSeconds) {
        if (totalSeconds <= 0) return "0sn";

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) return days + "g " + hours + "s";
        if (hours > 0) return hours + "s " + minutes + "dk";
        if (minutes > 0) return minutes + "dk " + seconds + "sn";
        return seconds + "sn";
    }
}