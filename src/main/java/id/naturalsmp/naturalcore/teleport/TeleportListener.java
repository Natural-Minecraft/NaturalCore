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
        plugin.getTeleportManager().setLastLocation(e.getPlayer(), e.getFrom());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Save location on logout so /back works after relog
        plugin.getTeleportManager().setLastLocation(e.getPlayer(), e.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(org.bukkit.event.player.PlayerChangedWorldEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        String world = p.getWorld().getName();
        
        // Target worlds for animation
        if (world.equals("world") || world.equals("world_nether") 
            || world.equals("world_the_end") || world.equals("dungeon") || world.equals("dungeon_world")) {
            
            // Generate elegant names
            String displayName = world.toUpperCase().replace("_", " ");
            if (world.equals("world")) displayName = "SURVIVAL WORLD";
            else if (world.equals("world_nether")) displayName = "THE NETHER";
            else if (world.equals("world_the_end")) displayName = "THE END";
            else if (world.startsWith("dungeon")) displayName = "DUNGEON";

            // 1.5 seconds total = 30 ticks. (fade in 10, stay 10, fade out 10)
            // Send elegant title
            p.sendTitle(id.naturalsmp.naturalcore.utils.ChatUtils.colorize("&#FFAA00&l" + displayName), 
                        id.naturalsmp.naturalcore.utils.ChatUtils.colorize("&fHati-hati di perjalanan..."), 
                        10, 10, 10);
            
            // Send ItemsAdder screen effect
            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), 
                "screeneffect fullscreen BLACK 10 10 10 freeze " + p.getName());
        }
    }
}
