package com.sallamadm.skyblockcore.fly;

import com.sallamadm.skyblockcore.SkyblockCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class FlyItem {

    public static final NamespacedKey REWARD_KEY = new NamespacedKey(SkyblockCore.getInstance(), "fly_reward_seconds");

    public static ItemStack create(int minutes) {
        long seconds = minutes * 60L;

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "" + minutes + "dk Fly Süresi");
            meta.setLore(List.of(
                    ChatColor.GRAY + "Sağ tıklayarak kullanın.",
                    ChatColor.GRAY + "Fly sürenize " + minutes + " dakika ekler."
            ));
            meta.getPersistentDataContainer().set(REWARD_KEY, PersistentDataType.LONG, seconds);
            item.setItemMeta(meta);
        }
        return item;
    }
}