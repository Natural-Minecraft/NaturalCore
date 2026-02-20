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

        String prefix = ConfigUtils.getString("messages.prefix.general");
        Location backLoc;

        if (plugin.getPermissionManager().isAtLeast(p, "midi")) {
            // MIDI Rank or higher: Can go back to any last location (Death or Teleport)
            backLoc = plugin.getTeleportManager().getLastLocation(p);
        } else {
            // Default/Member: Can ONLY go back to last DEATH location
            backLoc = plugin.getTeleportManager().getLastDeathLocation(p);
            if (backLoc == null) {
                // Check if they have a generic last location but not death
                // and explain they need permission
                if (plugin.getTeleportManager().getLastLocation(p) != null) {
                    p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.teleport.back-no-rank")));
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    return true;
                }
                // No death location either — show specific message
                p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.teleport.back-no-death")));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return true;
            }
        }

        if (backLoc == null) {
            p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.teleport.back-fail")));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return true;
        }

        // Prevent returning *to* or *from* a dungeon world
        if (p.getWorld().getName().toLowerCase().startsWith("dungeon") ||
                (backLoc.getWorld() != null && backLoc.getWorld().getName().toLowerCase().startsWith("dungeon"))) {
            p.sendMessage(ChatUtils.colorize(prefix + "&cPerintah /back diblokir untuk wilayah Dungeon!"));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return true;
        }

        p.teleport(backLoc);
        p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.teleport.back-success")));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        return true;
    }
}
