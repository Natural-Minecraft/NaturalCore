package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RestartCancelCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("naturalcs.restartalert")) {
            sender.sendMessage(ConfigUtils.getMessage("global.no-permission"));
            return true;
        }

        RestartAlertCommand.cancelRestart();
        sender.sendMessage(
                ChatUtils.colorize(ConfigUtils.getString("prefix.admin") + "&aBerhasil membatalkan restart server!"));
        return true;
    }
}
