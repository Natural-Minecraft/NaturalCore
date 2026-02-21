package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatPreviewGUI implements Listener, InventoryHolder {

    public static void openSnapshot(Player viewer, UUID snapshotId) {
        ChatSnapshotManager.SnapshotData data = ChatSnapshotManager.getSnapshot(snapshotId);

        if (data == null) {
            viewer.sendMessage(ChatUtils.toComponent("&cSnapshot ini sudah kadaluarsa (Expired)."));
            return;
        }

        Inventory snapshot = data.inventory;
        String title = data.title;

        // Create a copy with our holder to prevent interaction
        // Use standard createGUI
        Inventory gui = GUIUtils.createGUI(new ChatPreviewGUI(), snapshot.getSize(), title);
        gui.setContents(snapshot.getContents());

        viewer.openInventory(gui);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null; // Not needed
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof ChatPreviewGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory())
                return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ChatPreviewGUI) {
            event.setCancelled(true);
        }
    }
}
