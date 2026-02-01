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
        String msg = event.getMessage();
        if (msg.startsWith("/"))
            msg = msg.substring(1);
        String[] parts = msg.split(" ");
        String cmd = parts[0].toLowerCase();

        if (cmd.equals("restart") || cmd.equals("spigot:restart")) {
            if (event.getPlayer().hasPermission("naturalcs.restartalert")) {
                event.setCancelled(true);
                String time = parts.length > 1 ? parts[1] : "30";
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nacore admin restart " + time);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        String msg = event.getCommand();
        String[] parts = msg.split(" ");
        String cmd = parts[0].toLowerCase();

        if (cmd.equals("restart") || cmd.equals("spigot:restart")) {
            event.setCancelled(true);
            String time = parts.length > 1 ? parts[1] : "30";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nacore admin restart " + time);
        }
    }

}
