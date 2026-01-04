package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("naturalcs.restartalert")) {
            sender.sendMessage(ChatUtils.colorize("&cNo Permission."));
            return true;
        }

        // Cek argumen (harus ada konfirmasi minimal 1 kata, misal /restartalert confirm)
        if (args.length < 1) {
            sender.sendMessage(ChatUtils.colorize("&8&l| &cSilahkan ketik &aConfirmation &cterlebih dahulu"));
            return true;
        }

        // --- VISUAL BROADCAST ---
        Bukkit.broadcastMessage(ChatUtils.colorize("&4&m&l---------=============---------"));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatUtils.colorize("                 &c&lWarning"));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatUtils.colorize("       &cServer akan melakukan restart"));
        Bukkit.broadcastMessage(ChatUtils.colorize("             &cdalam waktu &a10 &cdetik"));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatUtils.colorize("&4&m&l---------=============---------"));

        // --- SCHEDULER (TIMER) ---
        new BukkitRunnable() {
            int timeLeft = 10; // Mulai dari 10 detik

            @Override
            public void run() {
                // FASE 1: Hitung Mundur (10 - 1)
                if (timeLeft > 0) {
                    String subTitle = "&c" + timeLeft + " Seconds";

                    // Kirim Title ke semua player
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(
                                ChatUtils.colorize("&4&l! &c&lRestart &4&l!"),
                                ChatUtils.colorize(subTitle),
                                0, 25, 5 // FadeIn, Stay, FadeOut (ticks)
                        );
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    }
                }

                // FASE 2: Saving Data (Saat waktu habis / 0)
                else if (timeLeft == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(ChatUtils.colorize("&7"), ChatUtils.colorize("&4Saving-data..."), 0, 40, 10);
                    }
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discordsrv broadcast **Server sedang melakukan restart!**");
                }

                // FASE 3: Eksekusi Restart (Delay 5 detik setelah save, jadi di -5)
                else if (timeLeft == -5) {
                    String kickReason = ChatUtils.colorize(
                            "&a&lNaturalSMP &cRestart\n\n" +
                                    "&bServer sedang melakukan restart! silahkan tunggu &e3 menit"
                    );

                    // Kick semua player
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.kickPlayer(kickReason);
                    }

                    // Matikan Server
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                    this.cancel(); // Hentikan timer
                }

                timeLeft--; // Kurangi waktu setiap detik
            }
        }.runTaskTimer(NaturalCore.getInstance(), 0L, 20L); // Jalan setiap 20 ticks (1 detik)

        return true;
    }
}