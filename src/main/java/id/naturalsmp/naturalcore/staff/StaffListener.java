package id.naturalsmp.naturalcore.staff;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffListener implements Listener {

    private final NaturalCore plugin;

    public StaffListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        // Vanish hiding is handled by VanishListener/Manager
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Cleanup if needed
        // If they were vanished, maybe silent quit?
        if (plugin.getVanishManager().isVanished(event.getPlayer())) {
            event.setQuitMessage(null);
        }
    }
}
