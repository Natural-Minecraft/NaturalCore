package id.naturalsmp.naturalcore.spawn;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class SpawnListener implements Listener {

    private final NaturalCore plugin;

    public SpawnListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String currentWorld = p.getWorld().getName();

        // Ambil list world yang diizinkan dari config
        // Kita pindah ke section 'spawn.allowed-join-worlds' agar lebih rapi
        List<String> allowed = ConfigUtils.getStringList("spawn.allowed-join-worlds");

        // Jika world saat ini TIDAK ada di daftar allowed
        // (Misal logout di dungeon/minigame world yang sudah unlaoded)
        if (!allowed.contains(currentWorld)) {
            // Teleport ke spawn utama
            plugin.getSpawnManager().teleport(p);

            // Matikan fly agar aman (standar safety)
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }
}
