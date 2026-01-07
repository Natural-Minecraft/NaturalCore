package id.naturalsmp.naturalcore.home;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HomeCommand implements CommandExecutor {

    private final HomeManager homeManager;
    private final HomeGUI homeGUI; // Tambahkan ini

    public HomeCommand(HomeManager homeManager, HomeGUI homeGUI) {
        this.homeManager = homeManager;
        this.homeGUI = homeGUI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only player.");
            return true;
        }

        Player p = (Player) sender;

        // /SETHOME & /DELHOME (Biarkan sama)
        if (label.equalsIgnoreCase("sethome")) {
            String name = (args.length > 0) ? args[0] : "home";
            homeManager.setHome(p, name);
            return true;
        }

        if (label.equalsIgnoreCase("delhome")) {
            if (args.length == 0) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /delhome <name>"));
                return true;
            }
            homeManager.deleteHome(p, args[0]);
            return true;
        }

        // --- /HOME [GUI LOGIC] ---
        if (label.equalsIgnoreCase("home")) {

            // 1. Jika pakai argumen: /home <nama> -> Teleport Langsung
            if (args.length > 0) {
                String target = args[0];
                homeManager.teleportHome(p, target);
                return true;
            }

            // 2. Jika TANPA argumen: /home -> Buka GUI
            // Cek dulu punya home atau gak
            if (homeManager.getHomes(p).isEmpty()) {
                String prefix = ConfigUtils.getString("prefix.home");
                p.sendMessage(prefix + ChatUtils.colorize("&cKamu belum memiliki home. Gunakan &e/sethome <nama>"));
                return true;
            }

            // Buka GUI Halaman 0
            homeGUI.open(p, 0);
            return true;
        }

        // /HOMES -> Buka GUI juga
        if (label.equalsIgnoreCase("homes")) {
            p.performCommand("home");
            return true;
        }

        return true;
    }
}