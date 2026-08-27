package com.sallamadm.skyblockcore.island.enums;

import org.bukkit.Material;
import org.bukkit.WeatherType;

public enum IslandWeather {

    NORMAL("weather.normal", "Varsayılan (Sunucu)", Material.WHITE_TERRACOTTA, null, null),

    ALWAYS_SUNRISE("weather.sunrise", "Sürekli Gün Doğumu", Material.PINK_TERRACOTTA, 23000L, WeatherType.CLEAR),
    ALWAYS_DAY("weather.day", "Sürekli Gündüz", Material.YELLOW_TERRACOTTA, 1000L, WeatherType.CLEAR),
    ALWAYS_NOON("weather.noon", "Sürekli Öğlen", Material.ORANGE_TERRACOTTA, 6000L, WeatherType.CLEAR),
    ALWAYS_SUNSET("weather.sunset", "Sürekli Gün Batımı", Material.RED_TERRACOTTA, 12000L, WeatherType.CLEAR),
    ALWAYS_NIGHT("weather.night", "Sürekli Gece", Material.PURPLE_TERRACOTTA, 13000L, WeatherType.CLEAR),
    ALWAYS_MIDNIGHT("weather.midnight", "Sürekli Gece Yarısı", Material.BLACK_TERRACOTTA, 18000L, WeatherType.CLEAR),

    ALWAYS_CLEAR("weather.clear", "Sürekli Açık Hava", Material.LIGHT_BLUE_TERRACOTTA, null, WeatherType.CLEAR),
    ALWAYS_RAIN("weather.rain", "Sürekli Yağmurlu", Material.BLUE_TERRACOTTA, null, WeatherType.DOWNFALL),
    ALWAYS_THUNDER("weather.thunder", "Sürekli Fırtınalı", Material.GRAY_TERRACOTTA, null, WeatherType.DOWNFALL);

    private final String node;
    private final String displayName;
    private final Material icon;
    private final Long fixedTime;
    private final WeatherType weatherType;

    IslandWeather(String node, String displayName, Material icon, Long fixedTime, WeatherType weatherType) {
        this.node = node;
        this.displayName = displayName;
        this.icon = icon;
        this.fixedTime = fixedTime;
        this.weatherType = weatherType;
    }

    public String getNode() {
        return node;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public Long getFixedTime() {
        return fixedTime;
    }

    public WeatherType getWeatherType() {
        return weatherType;
    }

    public boolean isThunder() {
        return this == ALWAYS_THUNDER;
    }

    public static IslandWeather fromNode(String node) {
        if (node == null) return NORMAL;
        for (IslandWeather w : values()) {
            if (w.node.equalsIgnoreCase(node)) return w;
        }
        return NORMAL;
    }
}
