package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ChatSnapshotManager {

    private static final Map<UUID, SnapshotData> snapshots = new HashMap<>();

    public static class SnapshotData {
        public final Inventory inventory;
        public final String title;

        public SnapshotData(Inventory inventory, String title) {
            this.inventory = inventory;
            this.title = title;
        }
    }

    public static UUID createItemSnapshot(String ownerName, ItemStack item) {
        UUID id = UUID.randomUUID();
        String title = ChatUtils.colorize("&8Preview Item: &f" + ownerName);
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }
        inv.setItem(13, item.clone());

        snapshots.put(id, new SnapshotData(inv, title));
        return id;
    }

    public static UUID createInventorySnapshot(Player player) {
        UUID id = UUID.randomUUID();
        String title = ChatUtils.colorize("&8[" + player.getName() + "'s Inventory]");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ItemStack filler = createFiller();

        // Row 1: Armor, Offhand, Head, XP
        ItemStack[] armor = player.getInventory().getArmorContents();
        inv.setItem(0, armor[3] != null && armor[3].getType() != Material.AIR ? armor[3]
                : createPlaceholder(Material.IRON_HELMET, "&7Helmet Slot"));
        inv.setItem(1, armor[2] != null && armor[2].getType() != Material.AIR ? armor[2]
                : createPlaceholder(Material.IRON_CHESTPLATE, "&7Chestplate Slot"));
        inv.setItem(2, armor[1] != null && armor[1].getType() != Material.AIR ? armor[1]
                : createPlaceholder(Material.IRON_LEGGINGS, "&7Leggings Slot"));
        inv.setItem(3, armor[0] != null && armor[0].getType() != Material.AIR ? armor[0]
                : createPlaceholder(Material.IRON_BOOTS, "&7Boots Slot"));
        inv.setItem(4,
                player.getInventory().getItemInOffHand().getType() != Material.AIR
                        ? player.getInventory().getItemInOffHand()
                        : createPlaceholder(Material.SHIELD, "&7Offhand Slot"));

        inv.setItem(5, filler);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta != null) {
            headMeta.setOwningPlayer(player);
            headMeta.setDisplayName(ChatUtils.colorize("&b&l" + player.getName()));
            head.setItemMeta(headMeta);
        }
        inv.setItem(6, head);
        inv.setItem(7, filler);

        ItemStack xp = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = xp.getItemMeta();
        if (xpMeta != null) {
            xpMeta.setDisplayName(ChatUtils.colorize("&a&lLevel &f" + player.getLevel()));
            xpMeta.setLore(
                    Collections.singletonList(ChatUtils.colorize("&7Total XP: &e" + player.getTotalExperience())));
            xp.setItemMeta(xpMeta);
        }
        inv.setItem(8, xp);

        for (int i = 9; i < 18; i++) {
            inv.setItem(i, filler);
        }

        ItemStack[] contents = player.getInventory().getStorageContents();
        int slotIdx = 18;
        for (int i = 9; i <= 35; i++) {
            if (slotIdx < 54 && contents[i] != null)
                inv.setItem(slotIdx, contents[i]);
            slotIdx++;
        }
        for (int i = 0; i <= 8; i++) {
            if (slotIdx < 54 && contents[i] != null)
                inv.setItem(slotIdx, contents[i]);
            slotIdx++;
        }

        snapshots.put(id, new SnapshotData(inv, title));
        return id;
    }

    public static UUID createEnderSnapshot(String ownerName, ItemStack[] contents) {
        UUID id = UUID.randomUUID();
        String title = ChatUtils.colorize("&8[" + ownerName + "'s Enderchest]");
        Inventory inv = Bukkit.createInventory(null, 27, title);
        inv.setContents(contents);
        snapshots.put(id, new SnapshotData(inv, title));
        return id;
    }

    public static SnapshotData getSnapshot(UUID id) {
        return snapshots.get(id);
    }

    private static ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createPlaceholder(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
