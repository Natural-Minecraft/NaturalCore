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
        // 1. Sembunyikan player vanish yang sudah online dari player baru ini
        plugin.getVanishManager().hideVanishedFrom(e.getPlayer());

        // 2. Jika player yang baru join ini sendiri sedang vanish (persistent),
        // sembunyikan dia dari player lain yang sudah online
        if (plugin.getVanishManager().isVanished(e.getPlayer())) {
            plugin.getVanishManager().setVanished(e.getPlayer(), true);
        }
    }
}