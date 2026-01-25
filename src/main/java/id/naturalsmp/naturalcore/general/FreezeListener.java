package id.naturalsmp.naturalcore.general;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import id.naturalsmp.naturalcore.utils.ChatUtils;

public class FreezeListener implements Listener {

    private final AdminControlCommand adminControl;

    public FreezeListener(AdminControlCommand adminControl) {
        this.adminControl = adminControl;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (adminControl.isFrozen(e.getPlayer().getUniqueId())) {
            // Allow looking around but not moving head too far? No, standard freeze usually
            // stops all.
            // But let's allow head rotation for better feeling.
            if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()
                    || e.getFrom().getY() != e.getTo().getY()) {
                e.setTo(e.getFrom());
                e.getPlayer().sendMessage(ChatUtils.colorize("&c&lKamu sedang dibekukan!"));
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (adminControl.isFrozen(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (adminControl.isFrozen(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatUtils.colorize("&cKamu tidak bisa chat saat dibekukan!"));
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (adminControl.isFrozen(e.getPlayer().getUniqueId())) {
            // Allow /unfreeze? No, only admin can do it.
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatUtils.colorize("&cKamu tidak bisa menggunakan command saat dibekukan!"));
        }
    }
}
