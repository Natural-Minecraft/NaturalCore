package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

    private final NaturalCore plugin;

    public TeleportListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        // Don't save if teleport distance is tiny or same block
        if (e.getFrom().getWorld() == e.getTo().getWorld()) {
            if (e.getFrom().distanceSquared(e.getTo()) < 4)
                return;
        }

        // Save location for /back
        plugin.getTeleportManager().setLastLocation(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Save location on logout so /back works after relog
        plugin.getTeleportManager().setLastLocation(e.getPlayer());
    }
}
