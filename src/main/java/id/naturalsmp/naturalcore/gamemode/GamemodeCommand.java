package id.naturalsmp.naturalcore.gamemode;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GamemodeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Permission Check
        if (!sender.hasPermission("naturalsmp.gamemode")) {
            sender.sendMessage(ConfigUtils.getString("messages.no-permission"));
            return true;
        }

        GameMode mode = null;
        Player target = (sender instanceof Player) ? (Player) sender : null;

        // 1. Deteksi Command (/gmc, /gms, dll)
        String cmd = label.toLowerCase();

        switch (cmd) {
            case "gmc": mode = GameMode.CREATIVE; break;
            case "gms": mode = GameMode.SURVIVAL; break;
            case "gma": mode = GameMode.ADVENTURE; break;
            case "gmsp": mode = GameMode.SPECTATOR; break;
            case "gamemode":
            case "gm":
                if (args.length == 0) {
                    sender.sendMessage(ChatUtils.colorize("&cUsage: /gm <0/1/2/3> [player]"));
                    return true;
                }
                mode = getGameMode(args[0]);
                if (mode == null) {
                    sender.sendMessage(ChatUtils.colorize("&cMode tidak valid!"));
                    return true;
                }
                // Jika ada argumen kedua (/gm 1 Steve)
                if (args.length > 1) target = Bukkit.getPlayer(args[1]);
                break;
        }

        // Cek target untuk shortcut (/gmc Steve)
        if (target == null && args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
        }

        if (target == null) {
            sender.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", "target"));
            return true;
        }

        // 2. Eksekusi
        target.setGameMode(mode);
        String prefix = ConfigUtils.getString("prefix.admin");

        if (!sender.equals(target)) {
            sender.sendMessage(prefix + ConfigUtils.getString("messages.gamemode-changed-other")
                    .replace("%target%", target.getName())
                    .replace("%mode%", mode.name()));
        }
        target.sendMessage(prefix + ConfigUtils.getString("messages.gamemode-changed").replace("%mode%", mode.name()));

        return true;
    }

    private GameMode getGameMode(String arg) {
        arg = arg.toLowerCase();
        if (arg.equals("0") || arg.startsWith("surv")) return GameMode.SURVIVAL;
        if (arg.equals("1") || arg.startsWith("crea")) return GameMode.CREATIVE;
        if (arg.equals("2") || arg.startsWith("adven")) return GameMode.ADVENTURE;
        if (arg.equals("3") || arg.startsWith("spec")) return GameMode.SPECTATOR;
        return null;
    }
}