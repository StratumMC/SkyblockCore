package com.sallamadm.skyblockcore.events;

import com.sallamadm.skyblockcore.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class IslandEvents {

    public static class Create extends Event {
        private static  final HandlerList HANDLERS = new HandlerList();
        private final Player player;
        private final Island island;

        public Create(Player player, Island island) {
            this.player = player;
            this.island = island;
        }

        public Player getPlayer() {
            return player;
        }

        public Island getIsland() {
            return island;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }


    public static class Delete extends Event {
        private static  final HandlerList HANDLERS = new HandlerList();
        private final Player player;
        private final Island island;

        public Delete(Player player, Island island) {
            this.player = player;
            this.island = island;
        }

        public Player getPlayer() {
            return player;
        }

        public Island getIsland() {
            return island;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }
}
