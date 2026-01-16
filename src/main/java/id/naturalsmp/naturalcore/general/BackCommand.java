package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BackCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public BackCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;

        if (!p.hasPermission("naturalsmp.back")) {
            p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        String prefix = ConfigUtils.getString("prefix.player");
        Location lastLoc = plugin.getTeleportManager().getLastLocation(p);
        if (lastLoc == null) {
            p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.back-fail")));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return true;
        }

        p.teleport(lastLoc);
        p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.back-success")));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        return true;
    }
}
