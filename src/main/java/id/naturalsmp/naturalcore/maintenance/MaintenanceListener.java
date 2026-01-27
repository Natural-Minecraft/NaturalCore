package id.naturalsmp.naturalcore.maintenance;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class MaintenanceListener implements Listener {

    private final MaintenanceManager manager;

    public MaintenanceListener(MaintenanceManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncLogin(AsyncPlayerPreLoginEvent e) {
        if (!manager.isActive())
            return;

        // Note: Perm check is not possible here as player data isn't loaded yet
        // However, we can check by name/UUID for the whitelist
        if (manager.isWhitelisted(e.getName()))
            return;

        // We will do a permission check on PlayerJoinEvent as well for secondary safety
        String kickReason = ConfigUtils.getString("messages.admin.maintenance.kick-reason");
        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatUtils.toComponent(kickReason));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!manager.isActive())
            return;

        if (e.getPlayer().hasPermission("naturalsmp.maintenance.bypass")
                || manager.isWhitelisted(e.getPlayer().getName())) {
            e.getPlayer().sendMessage(ChatUtils.colorize("&6&lMaintenance &8» &aAnda masuk menggunakan akses Bypass."));
            return;
        }

        String kickReason = ConfigUtils.getString("messages.admin.maintenance.kick-reason");
        e.getPlayer().kick(ChatUtils.toComponent(kickReason));
    }
}
