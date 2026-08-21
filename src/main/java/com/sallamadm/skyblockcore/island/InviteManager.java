package com.sallamadm.skyblockcore.island;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteManager {
    private static final long INVITE_DURATION = 60L * 20L;

    public static class PendingInvite {
        public final UUID islandOwnerUUID;
        public final UUID inviterUUID;
        public final BukkitTask expiryTask;

        public PendingInvite(UUID islandOwnerUUID, UUID inviterUUID, BukkitTask expiryTask) {
            this.islandOwnerUUID = islandOwnerUUID;
            this.inviterUUID = inviterUUID;
            this.expiryTask = expiryTask;
        }
    }

    private final Map<UUID, PendingInvite> pendingInvites = new HashMap<>();

    public boolean hasPendingInvite(UUID invitedUUID) {
        return pendingInvites.containsKey(invitedUUID);
    }

    public PendingInvite getPendingInvite(UUID invitedUUID) {
        return pendingInvites.get(invitedUUID);
    }


    public void createInvite(Player inviter, Player invited, Island island) {
        MessageManager msg = SkyblockCore.getInstance().getMessageManager();
        UUID invitedUUID = invited.getUniqueId();
        String inviterName = inviter.getName();

        BukkitTask task = Bukkit.getScheduler().runTaskLater(SkyblockCore.getInstance(), () -> {
            pendingInvites.remove(invitedUUID);
            Player invitedNow = Bukkit.getPlayer(invitedUUID);
            if (invitedNow != null && invitedNow.isOnline()) {
                invitedNow.sendMessage(msg.getMessage("invite.expired").replace("{inviter}", inviterName));
            }
        }, INVITE_DURATION);

        pendingInvites.put(invitedUUID, new PendingInvite(island.getOwnerUUID(), inviter.getUniqueId(), task));
    }

    public void removeInvite(UUID invitedUUID) {
        PendingInvite invite = pendingInvites.remove(invitedUUID);
        if (invite != null && invite.expiryTask != null) {
            invite.expiryTask.cancel();
        }
    }
}
