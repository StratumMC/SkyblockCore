package com.sallamadm.skyblockcore.scoreboard;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

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

        int level = (island != null) ? island.getLevel() : 0;



        Score blank1 = obj.getScore(" ");
        Score playerLine = obj.getScore(ChatColor.WHITE + "Oyuncu: " + ChatColor.YELLOW + player.getName());
        Score levelLine = obj.getScore(ChatColor.WHITE + "Ada leveli: " + ChatColor.GREEN + level);
        Score blank2 = obj.getScore(" ");
        Score footer = obj.getScore(ChatColor.GRAY + "localhost");

        blank1.setScore(5);
        playerLine.setScore(4);
        levelLine.setScore(3);
        blank2.setScore(2);
        footer.setScore(1);

        player.setScoreboard(board);



    }
}
