package id.naturalsmp.naturalcore.chat.suffix;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SuffixCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public SuffixCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("naturalsmp.admin")) {
                    sender.sendMessage(ChatUtils.toComponent("&cNo permission."));
                    return true;
                }
                plugin.getSuffixManager().loadConfigs();
                sender.sendMessage(ChatUtils.toComponent("&aSuffix configuration reloaded!"));
                return true;
            }
        }

        if (!(sender instanceof Player p)) {
            sender.sendMessage("Hanya player yang bisa menggunakan perintah ini.");
            return true;
        }

        // Open GUI
        new SuffixGUI(plugin).openGUI(p);
        return true;
    }
}
