package id.naturalsmp.naturalcore.profile;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.bukkit.command.TabCompleter;

public class ProfileCommand implements CommandExecutor, TabCompleter {

    private final NaturalCore plugin;

    public ProfileCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Hanya untuk player");
            return true;
        }

        Player p = (Player) sender;
        Player target = p;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage("Player tidak ditemukan atau offline.");
                return true;
            }
        }

        plugin.getProfileGUI().openGUI(target, p);
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        // Suggest players
        return null;
    }
}
