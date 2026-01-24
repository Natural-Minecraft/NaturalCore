package id.naturalsmp.naturalcore.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiSoundListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        InventoryHolder holder = event.getInventory().getHolder();

        // If it's a custom inventory (has a holder that isn't null and isn't a standard
        // block/entity)
        // OR if the title looks like one of our custom menus (fallback)
        if (holder != null && !(holder instanceof org.bukkit.block.BlockState)
                && !(holder instanceof org.bukkit.entity.Entity)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.2f);
        } else {
            // Fallback for some GUIs that might use null holder but custom titles
            String title = event.getView().getTitle();
            if (title.contains("❂") || title.contains("ɴᴀᴛᴜʀᴀʟ") || title.contains("ʙᴀᴛᴛʟᴇᴘᴀꜱꜱ")) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.2f);
            }
        }
    }
}
