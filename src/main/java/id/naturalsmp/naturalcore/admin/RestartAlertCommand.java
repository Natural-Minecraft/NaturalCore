package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.NotNull;

public class RestartAlertCommand implements CommandExecutor {

    private static BukkitRunnable currentTask;
    private static BossBar bossBar;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("naturalcs.restartalert")) {
            sender.sendMessage(ConfigUtils.getMessage("global.no-permission"));
            return true;
        }

        int seconds = 30; // Default to 30 seconds
        if (args.length >= 1) {
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatUtils.colorize("&cError: Waktu harus berupa angka (detik)."));
                return true;
            }
        } else if (label.equalsIgnoreCase("restartalert")) {
            // Only require confirmation if using the long command without args
            sender.sendMessage(ConfigUtils.getMessage("admin.restart.confirm-required"));
            return true;
        }
        // If label is 'restart' and no args, it defaults to 30s.

        if (currentTask != null) {
            sender.sendMessage(
                    ChatUtils.colorize("&cError: Ada restart yang sedang berjalan! Gunakan /restartcancel dulu."));
            return true;
        }

        // --- INITIAL BROADCAST ---
        String header = ConfigUtils.getMessage("admin.restart.warning-header");
        String titleMsg = ConfigUtils.getMessage("admin.restart.warning-title");
        String body = ConfigUtils.getMessage("admin.restart.warning-body").replace("%time%", String.valueOf(seconds));
        String footer = ConfigUtils.getMessage("admin.restart.warning-footer");

        Bukkit.broadcastMessage(ChatUtils.colorize(header));
        Bukkit.broadcastMessage(ChatUtils.colorize(titleMsg));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatUtils.colorize(body));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatUtils.colorize(footer));

        // --- BOSS BAR SETUP ---
        bossBar = Bukkit.createBossBar(
                ChatUtils.colorize(ConfigUtils.getMessage("admin.restart.bossbar-title").replace("%time%",
                        String.valueOf(seconds))),
                BarColor.GREEN,
                BarStyle.SOLID);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        // --- SCHEDULER ---
        final int finalSeconds = seconds;
        currentTask = new BukkitRunnable() {
            int timeLeft = finalSeconds;
            final double initialTime = finalSeconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    processRestart();
                    this.cancel();
                    cleanup();
                    return;
                }

                // Update BossBar
                double progress = (double) timeLeft / initialTime;
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                bossBar.setTitle(ChatUtils.colorize(ConfigUtils.getMessage("admin.restart.bossbar-title")
                        .replace("%time%", String.valueOf(timeLeft))));

                // Change BossBar Color based on time
                if (progress < 0.2)
                    bossBar.setColor(BarColor.RED);
                else if (progress < 0.5)
                    bossBar.setColor(BarColor.YELLOW);

                // Notifications based on interval
                if (shouldNotify(timeLeft)) {
                    broadcastCountdown(timeLeft);
                }

                timeLeft--;
            }
        };
        currentTask.runTaskTimer(NaturalCore.getInstance(), 20L, 20L);

        return true;
    }

    private boolean shouldNotify(int time) {
        if (time <= 10)
            return true; // Every second
        if (time <= 60 && time % 10 == 0)
            return true; // Every 10s
        if (time % 60 == 0)
            return true; // Every minute
        return false;
    }

    private void broadcastCountdown(int time) {
        String title = ConfigUtils.getMessage("admin.restart.countdown-title");
        String subTitle = ConfigUtils.getMessage("admin.restart.countdown-subtitle").replace("%time%",
                String.valueOf(time));

        // Dynamic Sound Pitch
        float pitch = 1.0f;
        if (time <= 10)
            pitch = 1.0f + ((10 - time) * 0.1f);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(ChatUtils.colorize(title), ChatUtils.colorize(subTitle), 0, 25, 5);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, pitch);
            if (!bossBar.getPlayers().contains(p))
                bossBar.addPlayer(p);
        }
    }

    private void processRestart() {
        Bukkit.broadcastMessage(ChatUtils.colorize(ConfigUtils.getMessage("admin.restart.saving-data")));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");

        String kickReason = ConfigUtils.getMessage("admin.restart.kick-reason");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer(ChatUtils.colorize(kickReason));
        }

        Bukkit.getScheduler().runTaskLater(NaturalCore.getInstance(), () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        }, 40L);
    }

    public static void cancelRestart() {
        if (currentTask != null) {
            currentTask.cancel();
            cleanup();
        }
    }

    private static void cleanup() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        currentTask = null;
    }
}
