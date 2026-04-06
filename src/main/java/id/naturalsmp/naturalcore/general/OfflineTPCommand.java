package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OfflineTPCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public OfflineTPCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;

        if (!p.hasPermission("naturalsmp.otp")) {
            p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        String prefix = ConfigUtils.getString("prefix.player");
        if (args.length == 0) {
            p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.otp-usage")));
            return true;
        }

        String targetName = args[0];

        // 1. Check Online
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            p.teleport(target);
            p.sendMessage(ChatUtils.colorize(prefix
                    + ConfigUtils.getString("messages.utils.otp-success").replace("%player%", target.getName())));
            return true;
        }

        // 2. Check Cache (TeleportManager - Quit Location)
        // Note: hasPlayedBefore() might be false if server just restarted and uuid
        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);

        // Note: hasPlayedBefore() might be false if server just restarted and uuid
        // cache is empty,
        // but it's a good check.
        if (!offline.hasPlayedBefore() && !offline.isOnline()) { // isOnline check just in case
            p.sendMessage(ChatUtils.colorize(prefix
                    + ConfigUtils.getString("messages.global.player-not-found").replace("%player%", targetName)));
            return true;
        }

        Location cacheLoc = plugin.getTeleportManager().getLastLocation(offline.getUniqueId());
        if (cacheLoc != null) {
            p.teleport(cacheLoc);
            p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.otp-offline-cache")
                    .replace("%player%", offline.getName())));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            return true;
        }

        // 3. Fallback: Bed Spawn
        Location bed = offline.getBedSpawnLocation();
        if (bed != null) {
            p.teleport(bed);
            p.sendMessage(ChatUtils.colorize(prefix
                    + ConfigUtils.getString("messages.utils.otp-offline-bed").replace("%player%", offline.getName())));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            return true;
        }

        p.sendMessage(ChatUtils.colorize(
                prefix + ConfigUtils.getString("messages.utils.otp-fail").replace("%player%", offline.getName())));
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);

        return true;
    }
}
