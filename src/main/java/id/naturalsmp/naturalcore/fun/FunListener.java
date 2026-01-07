package id.naturalsmp.naturalcore.fun;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class FunListener implements Listener {

    // 1. WARDEN KILL LOGIC
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.WARDEN) {
            Player killer = e.getEntity().getKiller();
            if (killer != null) {
                // Tambah XP
                int xp = ConfigUtils.getInt("fun.warden-kill-xp");
                killer.giveExp(xp);

                // Log Console
                Location loc = killer.getLocation();
                String coords = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
                Bukkit.getLogger().info(ChatUtils.stripColor("&6&lLOG &c> &f" + killer.getName() + " membunuh Warden di " + coords));
            }
        }
    }

    // 2. AUTO LOBBY (Jika login di world terlarang)
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String currentWorld = p.getWorld().getName();
        List<String> allowed = ConfigUtils.getStringList("fun.allowed-join-worlds");

        // Jika world saat ini TIDAK ada di daftar allowed
        if (!allowed.contains(currentWorld)) {
            // Jalankan command spawn console (atau pakai SpawnManager kita)
            // Kita pakai SpawnManager biar lebih native Java
            NaturalCore.getInstance().getSpawnManager().teleport(p);

            // Matikan fly (sesuai skript)
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }
}