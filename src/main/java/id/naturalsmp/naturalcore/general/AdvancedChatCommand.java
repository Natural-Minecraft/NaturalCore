package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdvancedChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        String cmd = label.toLowerCase();

        switch (cmd) {
            case "shout" -> {
                if (!sender.hasPermission("naturalsmp.shout")) {
                    sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                    return true;
                }
                if (args.length == 0) {
                    sender.sendMessage(ChatUtils.colorize("&cUsage: /shout <pesan>"));
                    return true;
                }
                String message = String.join(" ", args);
                String format = ConfigUtils.getString("messages.admin.broadcast.shout-format",
                        "&#FFAA00&l[SHOUT] &f%player%: &e%message%");

                String finalMsg = ChatUtils.colorize(format
                        .replace("%player%", sender.getName())
                        .replace("%message%", message));

                Bukkit.broadcastMessage(finalMsg);
                return true;
            }
            case "sudo" -> {
                if (!sender.hasPermission("naturalsmp.sudo")) {
                    sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatUtils.colorize("&cUsage: /sudo <player> <command/text>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(
                            ConfigUtils.getString("messages.global.player-not-found").replace("%player%", args[0]));
                    return true;
                }
                String action = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

                if (action.startsWith("/")) {
                    target.performCommand(action.substring(1));
                    sender.sendMessage(
                            ChatUtils.colorize("&aSudone command: &e" + action + " &apada &f" + target.getName()));
                } else {
                    target.chat(action);
                    sender.sendMessage(
                            ChatUtils.colorize("&aSudone chat: &7\"" + action + "\" &apada &f" + target.getName()));
                }
                return true;
            }
        }

        return false;
    }
}
