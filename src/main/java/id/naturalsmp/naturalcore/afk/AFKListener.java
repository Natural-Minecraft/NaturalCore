package id.naturalsmp.naturalcore.afk;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AFKListener implements Listener {

    private final NaturalCore plugin;
    private final AFKManager afkManager;

    public AFKListener(NaturalCore plugin) {
        this.plugin = plugin;
        this.afkManager = plugin.getAFKManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        afkManager.updateActivity(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        afkManager.removeIndicator(e.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        // Optimization: Don't check every tick if just looking around
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockY() == e.getTo().getBlockY() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }
        afkManager.updateActivity(e.getPlayer());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        // Sync because updateActivity modifies HashMaps (not thread safe necessarily if
        // iterating)
        // But lastActivity is map put. ConcurrentModification?
        // afkManager.updateActivity handles logic. It calls setAFK which modifies map.
        // Ideally sync.
        plugin.getServer().getScheduler().runTask(plugin, () -> afkManager.updateActivity(e.getPlayer()));
    }
}
