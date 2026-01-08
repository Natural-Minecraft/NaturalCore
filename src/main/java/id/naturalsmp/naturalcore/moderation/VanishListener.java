package id.naturalsmp.naturalcore.moderation;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VanishListener implements Listener {

    private final NaturalCore plugin;

    public VanishListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Minta Manager untuk menyembunyikan player vanish dari player yang baru join ini
        plugin.getVanishManager().hideVanishedFrom(e.getPlayer());
    }
}