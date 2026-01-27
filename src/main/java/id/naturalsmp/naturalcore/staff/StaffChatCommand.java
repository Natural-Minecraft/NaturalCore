package id.naturalsmp.naturalcore.staff;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StaffChatCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public StaffChatCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("naturalsmp.staff")) {
            sender.sendMessage(ChatUtils.colorize("&cMaaf, kamu tidak punya izin untuk akses Staff Chat."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /sc <message>"));
            return true;
        }

        String message = String.join(" ", args);
        String senderName = sender.getName();
        String format = ChatUtils.colorize("&6&lStaffChat &8┃ &e" + senderName + "&8: &f" + message);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("naturalsmp.staff")) {
                online.sendMessage(format);
            }
        }

        // Also log to console
        Bukkit.getConsoleSender().sendMessage(format);

        return true;
    }
}
