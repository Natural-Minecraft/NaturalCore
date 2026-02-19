package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final NaturalCore plugin;

    public PlayerDeathListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        // Save Location for /back
        plugin.getTeleportManager().setLastLocation(p, p.getLocation());
        plugin.getTeleportManager().setLastDeathLocation(p);

        // Death text disabled — handled by AxGraves plugin
    }
}
