package id.naturalsmp.naturalcore.combat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {

    private final NaturalCore plugin;
    private final Map<UUID, Long> combatTimers = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final int COMBAT_SECONDS = 15;
    private BukkitTask cleanupTask;

    public CombatManager(NaturalCore plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    public void stop() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
        combatTimers.clear();
    }

    public void tagPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        if (!combatTimers.containsKey(uuid)) {
            player.sendMessage(
                    ChatUtils.colorize("&6&lNaturalPVP &8» &cKamu sedang dalam pertempuran! Jangan logout."));
        }

        combatTimers.put(uuid, System.currentTimeMillis() + (COMBAT_SECONDS * 1000));
        updateBossBar(player);
    }

    public boolean isInCombat(Player player) {
        Long endTime = combatTimers.get(player.getUniqueId());
        return endTime != null && System.currentTimeMillis() < endTime;
    }

    private void updateBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bar = bossBars.computeIfAbsent(uuid, k -> {
            BossBar b = Bukkit.createBossBar(ChatUtils.colorize("&c&lCOMBAT MODE &8┃ &fJangan Keluar!"),
                    BarColor.RED, BarStyle.SOLID);
            b.addPlayer(player);
            return b;
        });
        bar.setProgress(1.0);
        bar.setVisible(true);
    }

    private void startCleanupTask() {
        this.cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (UUID uuid : new java.util.HashSet<>(combatTimers.keySet())) {
                    long endTime = combatTimers.get(uuid);
                    Player player = Bukkit.getPlayer(uuid);

                    if (now >= endTime) {
                        combatTimers.remove(uuid);
                        if (bossBars.containsKey(uuid)) {
                            bossBars.get(uuid).setVisible(false);
                            bossBars.get(uuid).removeAll();
                            bossBars.remove(uuid);
                        }
                        if (player != null) {
                            player.sendMessage(ChatUtils.colorize("&6&lNaturalPVP &8» &aKamu sudah aman."));
                        }
                    } else if (player != null && bossBars.containsKey(uuid)) {
                        double remaining = (endTime - now) / 1000.0;
                        double progress = Math.max(0, Math.min(1, remaining / COMBAT_SECONDS));
                        bossBars.get(uuid).setProgress(progress);
                        bossBars.get(uuid).setTitle(
                                ChatUtils.colorize("&c&lIN COMBAT &8┃ &f" + String.format("%.1fs", remaining)));
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 2L); // Every 0.1s for smooth bar
    }
}
