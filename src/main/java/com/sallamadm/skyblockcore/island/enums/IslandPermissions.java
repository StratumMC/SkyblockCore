package com.sallamadm.skyblockcore.island.enums;

public enum IslandPermissions {
    ISLAND_KICK("island.kick"),
    ISLAND_BAN("island.ban"),
    ISLAND_UNBAN("island.unban"),
    ISLAND_MANAGE_WARP("island.managewarp"),
    ISLAND_BLOCK_BREAK("island.blockbreak"),
    ISLAND_BLOCK_PLACE("island.blockplace"),
    ISLAND_INTERACT("island.interact"),
    ISLAND_CHANGE_NAME("island.changename"),
    ISLAND_SET_SPAWN("island.setspawn"),
    ISLAND_LEVEL("island.level"),

    MEMBERS_INVITE("members.invite"),
    MEMBERS_KICK("members.kick"),
    MEMBERS_BAN("members.ban"),
    MEMBERS_ROLE_CHANGE("members.rolechange"),
    MEMBERS_MAKE_LEADER("members.makeleader"),

    COOP_ADD("coop.add"),
    COOP_REMOVE("coop.remove"),

    WARP_CREATE("warp.create"),
    WARP_DELETE("warp.delete"),
    WARP_RENAME("warp.rename"),
    WARP_LOCATION_CHANGE("warp.locationchange"),
    WARP_ICON_CHANGE("warp.iconchange"),
    WARP_VISIBILITY_TOGGLE("warp.visibilitytoggle"),

    BIOME_CHANGE("biome.change"),

    ADMIN_SET_LEVEL("admin.setlevel"),
    ADMIN_ADD_LEVEL("admin.addlevel");


    private final String node;

    IslandPermissions(String node) {
        this.node = node;
    }

    public String getNode(){
        return node;
    }

    @Override
    public String toString() {
        return node;
    }
}
