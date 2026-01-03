package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class RestartAlertCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("naturalcore.admin.restartalert")) {
            sender.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatUtils.color("&cUsage: /restartalert <detik>"));
            return true;
        }
        
        int seconds;
        try {
            seconds = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtils.color("&cMasukkan angka yang valid!"));
            return true;
        }
        
        startCountdown(seconds);
        sender.sendMessage(ChatUtils.color("&a&l✔ &aCountdown restart dimulai: &f" + seconds + " &adetik"));
        return true;
    }
    
    private void startCountdown(int seconds) {
        new BukkitRunnable() {
            int remaining = seconds;
            
            @Override
            public void run() {
                if (remaining <= 0) {
                    Bukkit.broadcastMessage(ChatUtils.color("&c&l⚠ SERVER RESTART SEKARANG!"));
                    Bukkit.shutdown();
                    cancel();
                    return;
                }
                
                if (remaining <= 10 || remaining % 30 == 0) {
                    Bukkit.broadcastMessage(ChatUtils.color("&e&l⚠ &eServer akan restart dalam &f" + remaining + " &edetik!"));
                }
                
                remaining--;
            }
        }.runTaskTimer(NaturalCore.getInstance(), 0L, 20L);
    }
}
