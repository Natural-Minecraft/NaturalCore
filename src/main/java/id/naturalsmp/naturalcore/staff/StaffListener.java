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
        Player player = event.getPlayer();

        // Hide existing vanished players from the new joins
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (plugin.getStaffManager().isVanished(online)) {
                if (!player.hasPermission("naturalsmp.staff")) {
                    player.hidePlayer(plugin, online);
                }
            }
        }

        // Handle if joining player is already in vanish state (e.g. from meta/database
        // persistent if implemented)
        // For now, if they have staff perm, let them see others
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Cleanup if needed
        // If they were vanished, maybe silent quit?
        if (plugin.getStaffManager().isVanished(event.getPlayer())) {
            event.setQuitMessage(null);
        }
    }
}
