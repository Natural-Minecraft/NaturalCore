package id.naturalsmp.naturalcore.home;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class HomeCommand implements CommandExecutor {

    private final HomeManager homeManager;
    private final HomeGUI homeGUI;

    public HomeCommand(HomeManager homeManager, HomeGUI homeGUI) {
        this.homeManager = homeManager;
        this.homeGUI = homeGUI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        String cmd = label.toLowerCase();

        // --- SETHOME ---
        if (cmd.equals("sethome")) {
            if (!p.hasPermission("naturalsmp.home.use"))
                return noPerm(p);

            // Nama Home (Default "home" jika tidak diketik)
            String name = (args.length > 0) ? args[0] : "home";

            // Cek Limit
            int current = homeManager.getHomes(p).size();
            int max = homeManager.getMaxHomes(p);

            // Jika belum punya home ini (home baru), tapi sudah limit -> Block
            if (!homeManager.hasHome(p, name) && current >= max) {
                ConfigUtils.sendMessage(p, "prefix.home", "messages.home.home-limit",
                        "%current%", String.valueOf(current),
                        "%max%", String.valueOf(max));
                return true;
            }

            // Simpan Home
            homeManager.setHome(p, name, p.getLocation());

            // FIX: Support %home% DAN %name% agar config fleksibel
            ConfigUtils.sendMessage(p, "prefix.home", "messages.home.home-set", "%home%", name, "%name%", name);
            return true;
        }

        // --- DELHOME ---
        if (cmd.equals("delhome")) {
            if (!p.hasPermission("naturalsmp.home.use"))
                return noPerm(p);

            String name = (args.length > 0) ? args[0] : "home";
            if (!homeManager.hasHome(p, name)) {
                ConfigUtils.sendMessage(p, "prefix.home", "messages.home.home-not-found");
                return true;
            }

            homeManager.deleteHome(p, name);

            ConfigUtils.sendMessage(p, "prefix.home", "messages.home.home-deleted", "%name%", name);
            return true;
        }

        // --- HOMES (LIST) ---
        if (cmd.equals("homes")) {
            if (!p.hasPermission("naturalsmp.home.use"))
                return noPerm(p);

            Set<String> list = homeManager.getHomes(p);
            if (list.isEmpty()) {
                ConfigUtils.sendMessage(p, "prefix.home", "messages.home.list-empty");
            } else {
                ConfigUtils.sendMessage(p, "prefix.home", "messages.home.home-list", "%homes%",
                        String.join(", ", list));
            }
            return true;
        }

        // --- HOME (TP / GUI) ---
        if (cmd.equals("home")) {
            if (!p.hasPermission("naturalsmp.home.use"))
                return noPerm(p);

            // Jika ada argumen (/home nama), langsung TP
            if (args.length > 0) {
                String name = args[0];
                if (homeManager.hasHome(p, name)) {
                    homeManager.teleportHome(p, name);
                } else {
                    ConfigUtils.sendMessage(p, "prefix.home", "messages.home.home-not-found", "%name%", name);
                }
                return true;
            }

            // Jika kosong (/home), buka GUI
            homeGUI.openGUI(p);
            return true;
        }

        return true;
    }

    private boolean noPerm(Player p) {
        ConfigUtils.sendGeneral(p, "messages.global.no-permission");
        return true;
    }
}