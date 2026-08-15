package com.sallamadm.skyblockcore.config;

import com.sallamadm.skyblockcore.SkyblockCore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MessageManager {

    private final SkyblockCore plugin;
    private File customConfigFile;
    private FileConfiguration customConfig;

    public MessageManager(SkyblockCore plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        customConfigFile = new File(plugin.getDataFolder(), "messages.yml");

        if(!customConfigFile.exists()){
            customConfigFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }

        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            customConfig.setDefaults(defaultConfig);
        }
    }


    public String getRawMessage(String path) {
        if (!customConfig.contains(path)) {
            return "Missing message: " + path;
        }
        return customConfig.getString(path);
    }

    public String getMessage(String path) {
        String prefix = customConfig.getString("prefix", "");
        String raw = getRawMessage(path);
        return ChatColor.translateAlternateColorCodes('&', prefix + raw);
    }

    public String getMessageWithoutPrefix(String path) {
        String raw = getRawMessage(path);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
