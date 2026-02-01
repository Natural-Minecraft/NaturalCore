package id.naturalsmp.naturalcore.listeners;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandOverrideListener implements Listener {

    public CommandOverrideListener(NaturalCore plugin) {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (isRestartCommand(message)) {
            if (event.getPlayer().hasPermission("naturalcs.restartalert")) {
                event.setCancelled(true);
                // Dispatch NaturalCore restart with 30s default
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nacore admin restart 30");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        String message = event.getCommand().toLowerCase();
        if (isRestartCommand(message)) {
            event.setCancelled(true);
            // Dispatch NaturalCore restart with 30s default
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nacore admin restart 30");
        }
    }

    private boolean isRestartCommand(String command) {
        // Remove leading / if present (PlayerCommandPreprocessEvent has it,
        // ServerCommandEvent doesn't)
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        // Check for exactly 'restart' or 'spigot:restart'
        return command.equals("restart") || command.startsWith("restart ") ||
                command.equals("spigot:restart") || command.startsWith("spigot:restart ");
    }
}
