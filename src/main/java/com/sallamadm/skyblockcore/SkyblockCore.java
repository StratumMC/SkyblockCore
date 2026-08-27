package com.sallamadm.skyblockcore;

import com.sallamadm.skyblockcore.commands.*;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockcore.data.DataManager;
import com.sallamadm.skyblockcore.fly.FlightManager;
import com.sallamadm.skyblockcore.gui.*;
import com.sallamadm.skyblockcore.island.InviteManager;
import com.sallamadm.skyblockcore.island.IslandManager;
import com.sallamadm.skyblockcore.listeners.*;
import com.sallamadm.skyblockcore.scoreboard.ScoreboardManager;
import com.sallamadm.skyblockcore.island.IslandWeatherManager;
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
    private InviteManager inviteManager;
    private FlightManager flightManager;


    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
    }

    @Override
    public void onEnable() {
        instance = this;

        getConfig().addDefault("mysql.host", "localhost");
        getConfig().addDefault("mysql.port", 3306);
        getConfig().addDefault("mysql.database", "skyblock");
        getConfig().addDefault("mysql.username", "root");
        getConfig().addDefault("mysql.password", "");
        getConfig().options().copyDefaults(true);
        saveConfig();



        this.messageManager = new MessageManager(this);
        this.islandManager = new IslandManager();
        this.scoreboardManager = new ScoreboardManager(this);
        this.worldManager = new WorldManager(this);
        this.inviteManager = new InviteManager();
        this.flightManager = new FlightManager(this);

        CommandAPI.onEnable();

        this.dataManager = new DataManager(this);
        this.dataManager.loadData();

        IsCommand.registerCommand(this);
        RegisterCommand.registerCommand(this);
        LoginCommand.registerCommand(this);
        FlyCommand.registerCommand(this);
        FlyItemCommand.registerCommand(this);


        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new VoidFallListener(this), this);
        getServer().getPluginManager().registerEvents(new IslandTeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new IslandProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new AuthListener(this), this);
        getServer().getPluginManager().registerEvents(new FlyItemListener(this), this);
        getServer().getPluginManager().registerEvents(flightManager, this);

        //guis
        getServer().getPluginManager().registerEvents(new WarpMenu(), this);
        getServer().getPluginManager().registerEvents(new BiomeMenu(), this);
        getServer().getPluginManager().registerEvents(new IsMenu(), this);
        getServer().getPluginManager().registerEvents(new IslandDeleteMenu(), this);
        getServer().getPluginManager().registerEvents(new PermissionsMenu(), this);
        getServer().getPluginManager().registerEvents(new MembersMenu(), this);
        getServer().getPluginManager().registerEvents(new GameruleMenu(), this);
        getServer().getPluginManager().registerEvents(new WeatherMenu(), this);

        IslandWeatherManager.startThunderEffectTask(this);

        getLogger().info("Skyblock Core is now activated.");
    }

    @Override
    public void onDisable() {
        if(dataManager != null) {
            dataManager.saveDataSync();
            dataManager.closeConnection();
            getLogger().info("SkyblockCore datas saved in islandData.yml.");
        }

        if (flightManager != null) {
            flightManager.saveAllSync();
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
    public InviteManager getInviteManager() {
        return inviteManager;
    }
    public FlightManager getFlightManager() {
        return flightManager;
    }

}
