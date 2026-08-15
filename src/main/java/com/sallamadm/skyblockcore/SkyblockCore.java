package com.sallamadm.skyblockcore;

import com.sallamadm.skyblockcore.commands.IsCommand;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.data.DataManager;
import com.sallamadm.skyblockcore.gui.BiomeMenu;
import com.sallamadm.skyblockcore.gui.IsMenu;
import com.sallamadm.skyblockcore.gui.IslandDeleteMenu;
import com.sallamadm.skyblockcore.gui.WarpMenu;
import com.sallamadm.skyblockcore.island.IslandManager;
import com.sallamadm.skyblockcore.listeners.*;
import com.sallamadm.skyblockcore.scoreboard.ScoreboardManager;
import com.sallamadm.skyblockcore.world.WorldManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkyblockCore extends JavaPlugin {

    private static SkyblockCore instance;
    private IslandManager islandManager;
    private ScoreboardManager scoreboardManager;
    private DataManager dataManager;
    private WorldManager worldManager;
    private MessageManager messageManager;


    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
    }

    @Override
    public void onEnable() {
        instance = this;
        this.messageManager = new MessageManager(this);
        this.islandManager = new IslandManager();
        this.scoreboardManager = new ScoreboardManager(this);
        this.worldManager = new WorldManager(this);
        CommandAPI.onEnable();

        this.dataManager = new DataManager(this);
        this.dataManager.loadData();

        IsCommand.registerCommand(this);


        getServer().getPluginManager().registerEvents(new BiomeMenu(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new VoidFallListener(this), this);
        getServer().getPluginManager().registerEvents(new IslandTeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new IslandProtectionListener(this), this);

        //guis
        getServer().getPluginManager().registerEvents(new WarpMenu(), this);
        getServer().getPluginManager().registerEvents(new IsMenu(), this);
        getServer().getPluginManager().registerEvents(new IslandDeleteMenu(), this);

        getLogger().info("Skyblock Core is now activated.");
    }

    @Override
    public void onDisable() {
        if(dataManager != null) {
            dataManager.saveData();
            getLogger().info("SkyblockCore datas saved in islandData.yml.");
        }


        CommandAPI.onDisable();
        getLogger().info("Skyblock Core is now deactivated.");
    }

    public static SkyblockCore getInstance() {
        return instance;
    }
    public WorldManager getWorldManager() {
        return worldManager;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
