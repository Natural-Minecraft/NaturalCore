package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ChatPreviewGUI implements Listener {

    public static void openSnapshot(Player viewer, UUID snapshotId, boolean isEnder) {
        Inventory snapshot = isEnder ? ChatSnapshotManager.getEnderSnapshot(snapshotId)
                : ChatSnapshotManager.getInventorySnapshot(snapshotId);

        if (snapshot == null) {
            viewer.sendMessage(ChatUtils.colorize("&cSnapshot ini sudah kadaluarsa (Expired)."));
            return;
        }

        // Create a copy to prevent modification of the snapshot
        Inventory gui = Bukkit.createInventory(null, snapshot.getSize(),
                snapshot.getHolder() != null ? snapshot.getHolder().toString() : "Preview");
        gui.setContents(snapshot.getContents());

        viewer.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("'s Inventory") || title.contains("'s Enderchest")) {
            // Cancel all interaction in preview
            event.setCancelled(true);
        }
    }
}
