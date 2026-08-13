package com.sallamadm.skyblockcore.island;

import org.bukkit.Location;
import org.bukkit.Material;

public class Warp {
    private String name;
    private Location location;
    private Material icon;
    private boolean visible;

    public Warp(String name, Location location) {
        this.name = name;
        this.location = location;
        this.icon = Material.OAK_SIGN;
        this.visible = false;
    }

    public Warp(String name, Location location, Material icon, boolean visible) {
        this.name = name;
        this.location = location;
        this.icon = icon;
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}