package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;

public class CleanManager {

    private final NaturalCore plugin;
    private boolean cleaningInProgress = false;

    public CleanManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void startGlobalClean(int countdownSeconds) {
        if (cleaningInProgress)
            return;
        cleaningInProgress = true;

        new BukkitRunnable() {
            int timeLeft = countdownSeconds;
            int tick = 0;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    performClean();
                    this.cancel();
                    cleaningInProgress = false;
                    return;
                }

                // Scrolling Effect logic
                String message = getScrollingMessage(timeLeft, tick);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(ChatUtils.toComponent(message));
                    if (timeLeft <= 5 && tick % 20 == 0) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    }
                }

                if (tick % 20 == 0) {
                    timeLeft--;
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private String getScrollingMessage(int seconds, int tick) {
        String core = " !!! CLEARING GROUND ITEMS IN " + seconds + "s !!! ";
        String decor = "»»»»»»»»»»";

        // Simple offset scrolling
        int offset = (tick / 2) % decor.length();
        String scrolledDecor = decor.substring(offset) + decor.substring(0, offset);
        String reverseDecor = new StringBuilder(scrolledDecor).reverse().toString();

        return "&#FF5555&l" + scrolledDecor + core + "&#FF5555&l" + reverseDecor;
    }

    private void performClean() {
        AtomicInteger count = new AtomicInteger(0);
        Bukkit.getWorlds().forEach(world -> {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                    count.incrementAndGet();
                }
            }
        });

        String msg = "&#55FF55&l✧ &7Berhasil membersihkan &#55FF55&l" + count.get() + " &7sampah di tanah!";
        Bukkit.broadcast(ChatUtils.toComponent(msg));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            p.sendActionBar(ChatUtils.toComponent("&#55FF55&l✔ CLEANUP COMPLETE"));
        }
    }
}
