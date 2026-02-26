package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationComponent extends AbstractHUDComponent {

    private final Map<UUID, Notification> activeNotifications = new HashMap<>();

    public NotificationComponent(NaturalCore plugin) {
        super(plugin, "notification", HUDPriority.HIGHEST);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        Notification notification = activeNotifications.get(player.getUniqueId());
        if (notification == null)
            return false;

        // Check if expired
        if (System.currentTimeMillis() > notification.expiryTime) {
            activeNotifications.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    @Override
    public String getContent(Player player, int globalTick) {
        Notification notification = activeNotifications.get(player.getUniqueId());
        if (notification == null)
            return null;
        return notification.message;
    }

    public void showNotification(Player player, String message, int ticks) {
        long expiryTime = System.currentTimeMillis() + (ticks * 50L);
        activeNotifications.put(player.getUniqueId(), new Notification(message, expiryTime));
    }

    private static class Notification {
        String message;
        long expiryTime;

        Notification(String message, long expiryTime) {
            this.message = message;
            this.expiryTime = expiryTime;
        }
    }
}
