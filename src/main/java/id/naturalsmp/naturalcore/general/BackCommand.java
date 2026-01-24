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

        String prefix = ConfigUtils.getString("prefix.player");
        Location backLoc;

        if (p.hasPermission("naturalsmp.back")) {
            // VIP Permission: Can go back to any last location (Death or Teleport)
            backLoc = plugin.getTeleportManager().getLastLocation(p);
        } else {
            // Default: Can ONLY go back to last DEATH location
            backLoc = plugin.getTeleportManager().getLastDeathLocation(p);
            if (backLoc == null) {
                // Check if they have a generic last location but not death
                // and explain they need permission
                if (plugin.getTeleportManager().getLastLocation(p) != null) {
                    p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                    return true;
                }
            }
        }

        if (backLoc == null) {
            p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.back-fail")));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return true;
        }

        p.teleport(backLoc);
        p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.back-success")));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        return true;
    }
}
