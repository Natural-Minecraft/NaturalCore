package id.naturalsmp.naturalcore.afk;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AFKCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public AFKCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigUtils.getString("prefix.general") + ConfigUtils.getString("global.only-player"));
            return true;
        }

        Player player = (Player) sender;
        if (plugin.getAFKManager() != null) {
            boolean isAfk = plugin.getAFKManager().isAFK(player);
            plugin.getAFKManager().setAFK(player, !isAfk);
        }
        return true;
    }
}
