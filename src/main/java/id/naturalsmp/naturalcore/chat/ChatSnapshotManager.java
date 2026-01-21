package id.naturalsmp.naturalcore.chat;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatSnapshotManager {

    private static final Map<UUID, Inventory> inventorySnapshots = new HashMap<>();
    private static final Map<UUID, Inventory> enderSnapshots = new HashMap<>();

    public static UUID createInventorySnapshot(String ownerName, ItemStack[] contents) {
        UUID id = UUID.randomUUID();
        Inventory inv = Bukkit.createInventory(null, 45, ownerName + "'s Inventory");

        // Fill contents (assuming standard 36 slots + armor/offhand if needed, but we
        // use a simple view)
        // For a full snapshot like the image provided, we might need a custom GUI
        // renderer.
        inv.setContents(contents);

        inventorySnapshots.put(id, inv);
        return id;
    }

    public static UUID createEnderSnapshot(String ownerName, ItemStack[] contents) {
        UUID id = UUID.randomUUID();
        Inventory inv = Bukkit.createInventory(null, 27, ownerName + "'s Enderchest");
        inv.setContents(contents);

        enderSnapshots.put(id, inv);
        return id;
    }

    public static Inventory getInventorySnapshot(UUID id) {
        return inventorySnapshots.get(id);
    }

    public static Inventory getEnderSnapshot(UUID id) {
        return enderSnapshots.get(id);
    }

    // Optional: Cleanup snapshots after a while to prevent memory leak
    public static void cleanup() {
        inventorySnapshots.clear();
        enderSnapshots.clear();
    }
}
