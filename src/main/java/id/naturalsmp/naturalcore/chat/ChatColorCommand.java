package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatColorCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public ChatColorCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only Player");
            return true;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("naturalsmp.chat.color") && !p.hasPermission("naturalsmp.color.vip")) {
            // Check specific color perms too if needed, but basic check is good
            p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        new ChatColorGUI(plugin).openGUI(p);
        return true;
    }
}
