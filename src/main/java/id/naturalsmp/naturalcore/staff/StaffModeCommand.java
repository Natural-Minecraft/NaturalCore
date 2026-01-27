package id.naturalsmp.naturalcore.staff;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StaffModeCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public StaffModeCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.colorize("&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission("naturalsmp.staff")) {
            player.sendMessage(ChatUtils.colorize("&cMaaf, kamu tidak punya izin untuk menggunakan Staff Mode."));
            return true;
        }

        plugin.getStaffManager().toggleStaffMode(player);
        return true;
    }
}
