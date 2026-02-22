package id.naturalsmp.naturalcore.listeners;

import id.naturalsmp.naturalcore.utility.NaturalLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class LogListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        NaturalLogger.getInstance().logChat(event.getPlayer().getName(), event.getFormat(), event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage(); // contains the initial slash, e.g. "/shop"
        String[] args = message.substring(1).split(" ");
        String cmd = args[0].toLowerCase();

        // 1. Private Chat Check
        if (cmd.equals("msg") || cmd.equals("tell") || cmd.equals("w") || cmd.equals("whisper") || cmd.equals("pm")
                || cmd.equals("reply") || cmd.equals("r") || cmd.equals("tc") || cmd.equals("teamchat")) {
            // For reply or team chat, target might be inferred, we log what they typed
            NaturalLogger.getInstance().logPrivateChat(player.getName(), "Somebody/Team", message);
            return;
        }

        // 2. Admin Check
        boolean isAdminCommand = false;
        if (player.hasPermission("naturalsmp.admin") || player.isOp()) {
            isAdminCommand = true;
        } else if (cmd.equals("give") || cmd.equals("effect") || cmd.equals("vanish") || cmd.equals("v")
                || cmd.equals("god") || cmd.equals("staff") || cmd.equals("sm") || cmd.equals("staffmode")
                || cmd.equals("sc") || cmd.equals("staffchat")) {
            isAdminCommand = true;
        }

        if (isAdminCommand) {
            NaturalLogger.getInstance().logAdmin(player.getName(), message);
        } else {
            // 3. Normal Command Check
            NaturalLogger.getInstance().logCommand(player.getName(), message);
        }
    }
}
