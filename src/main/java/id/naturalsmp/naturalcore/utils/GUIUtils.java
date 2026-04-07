package id.naturalsmp.naturalcore.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUIUtils - Helper untuk membuat GUI dan ItemStack dengan API modern.
 * Mengganti deprecated methods dengan Component-based API.
 */
public class GUIUtils {

    /**
     * Membuat inventory dengan Component title (non-deprecated).
     */
    public static Inventory createGUI(InventoryHolder holder, int size, String title) {
        Component titleComponent = ChatUtils.toComponent(title);
        return Bukkit.createInventory(holder, size, titleComponent);
    }

    /**
     * Membuat inventory dengan InventoryType dan Component title (non-deprecated).
     */
    public static Inventory createGUI(InventoryHolder holder, org.bukkit.event.inventory.InventoryType type,
            String title) {
        Component titleComponent = ChatUtils.toComponent(title);
        return Bukkit.createInventory(holder, type, titleComponent);
    }

    /**
     * Membuat ItemStack dengan nama dan lore.
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(ChatUtils.toComponent(line));
                }
                meta.lore(loreComponents);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Membuat ItemStack dengan nama saja (tanpa lore).
     */
    public static ItemStack createItem(Material material, String name) {
        return createItem(material, name, null);
    }

    /**
     * Membuat tombol Close/Tutup standar dengan icon X (CustomModelData 10063)
     */
    public static ItemStack createClosePaper() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent("§c§lTUTUP"));
            meta.setCustomModelData(10063);
            List<Component> loreList = new ArrayList<>();
            loreList.add(ChatUtils.toComponent("§7Klik untuk menutup menu."));
            meta.lore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Membuat filler/pane item (biasanya untuk dekorasi GUI).
     */
    public static ItemStack createFiller(Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            filler.setItemMeta(meta);
        }
        return filler;
    }

    /**
     * Mengisi slot tertentu dengan filler item.
     */
    public static void fillSlots(Inventory inv, Material material, int... slots) {
        ItemStack filler = createFiller(material);
        for (int slot : slots) {
            if (slot >= 0 && slot < inv.getSize()) {
                inv.setItem(slot, filler);
            }
        }
    }

    /**
     * Mengisi border GUI dengan filler.
     */
    public static void fillBorder(Inventory inv, Material material) {
        ItemStack filler = createFiller(material);
        int size = inv.getSize();
        int rows = size / 9;

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, filler); // Top row
            inv.setItem(size - 9 + i, filler); // Bottom row
        }
        for (int i = 1; i < rows - 1; i++) {
            inv.setItem(i * 9, filler); // Left column
            inv.setItem(i * 9 + 8, filler); // Right column
        }
    }

    /**
     * Broadcast message ke semua player online (non-deprecated).
     */
    public static void broadcast(String message) {
        Component component = ChatUtils.toComponent(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }
        // Also log to console
        Bukkit.getConsoleSender().sendMessage(ChatUtils.colorize(message));
    }

    /**
     * Broadcast message dengan prefix kosong (untuk spacing).
     */
    public static void broadcastEmpty() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.empty());
        }
    }
}
