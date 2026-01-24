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
import org.jetbrains.annotations.NotNull;

public class RestartAlertCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("naturalcs.restartalert")) {
            sender.sendMessage(ConfigUtils.getMessage("global.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ConfigUtils.getMessage("admin.restart.confirm-required"));
            return true;
        }

        // --- VISUAL BROADCAST ---
        Bukkit.broadcastMessage(ConfigUtils.getMessage("admin.restart.warning-header"));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ConfigUtils.getMessage("admin.restart.warning-title"));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ConfigUtils.getMessage("admin.restart.warning-body"));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ConfigUtils.getMessage("admin.restart.warning-footer"));

        // --- SCHEDULER (TIMER) ---
        new BukkitRunnable() {
            int timeLeft = args.length > 0 ? Integer.parseInt(args[0]) : 10;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    String title = ConfigUtils.getMessage("admin.restart.countdown-title");
                    String subTitle = ConfigUtils.getMessage("admin.restart.countdown-subtitle")
                            .replace("%time%", String.valueOf(timeLeft));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(title, subTitle, 0, 25, 5);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    }
                } else if (timeLeft == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(" ", ConfigUtils.getMessage("admin.restart.saving-data"), 0, 40, 10);
                    }
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "discordsrv broadcast **Server sedang melakukan restart!**");
                } else if (timeLeft == -5) {
                    String kickReason = ConfigUtils.getMessage("admin.restart.kick-reason");

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.kickPlayer(kickReason);
                    }

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                    this.cancel();
                }

                timeLeft--;
            }
        }.runTaskTimer(NaturalCore.getInstance(), 0L, 20L);

        return true;
    }
}
