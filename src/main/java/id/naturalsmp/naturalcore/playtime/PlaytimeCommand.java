package id.naturalsmp.naturalcore.playtime;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaytimeCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public PlaytimeCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.colorize("&cOnly players can use this command."));
            return true;
        }

        String playtime = plugin.getPlaytimeManager().getPlaytime(player);
        player.sendMessage("");
        player.sendMessage(ChatUtils.colorize("   &#FFAA00&lＮＡＴＵＲＡＬ &f&lＰＬＡＹＴＩＭＥ"));
        player.sendMessage(ChatUtils.colorize("   &7Total waktu bermainmu di server ini."));
        player.sendMessage("");
        player.sendMessage(ChatUtils.colorize("   &8» &fTotal Playtime: &#FFD400" + playtime));
        player.sendMessage(ChatUtils.colorize("   &8» &fStatus: &a&lActive Player"));
        player.sendMessage("");
        player.sendMessage(ChatUtils.colorize("   &7Terus bermain untuk mendapatkan &eRewards&7!"));
        player.sendMessage("");

        return true;
    }
}
