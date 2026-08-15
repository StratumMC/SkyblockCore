package com.sallamadm.skyblockcore.island;

import com.sallamadm.skyblockcore.SkyblockCore;
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

    private void autoSave() {
        if (SkyblockCore.getInstance() != null && SkyblockCore.getInstance().getDataManager() != null) {
            SkyblockCore.getInstance().getDataManager().saveData();
        }
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
        autoSave();
    }
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
        autoSave();
    }
    public Material getIcon() {
        return icon;
    }
    public void setIcon(Material icon) {
        this.icon = icon;
        autoSave();
    }
    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
        autoSave();
    }
}