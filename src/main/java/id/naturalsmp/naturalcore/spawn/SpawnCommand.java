package id.naturalsmp.naturalcore.spawn;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    private final SpawnManager spawnManager;

    public SpawnCommand(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        // Prefix diambil dari ConfigUtils
        String prefix = ConfigUtils.getString("prefix.admin");

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + ConfigUtils.getString("messages.global.only-player"));
            return true;
        }

        Player p = (Player) sender;

        // --- COMMAND: /setspawn ---
        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!p.hasPermission("naturalsmp.admin")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }

            spawnManager.setSpawn(p.getLocation());

            // Pesan dengan Prefix
            p.sendMessage(prefix + ConfigUtils.getString("messages.spawn.spawn-set"));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            return true;
        }

        // --- COMMAND: /spawn ---
        if (command.getName().equalsIgnoreCase("spawn")) {
            if (!p.hasPermission("naturalsmp.spawn")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }

            spawnManager.teleport(p);
            return true;
        }

        return true;
    }
}