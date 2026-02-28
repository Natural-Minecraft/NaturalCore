package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellConfirmCommand implements CommandExecutor {

    private final Map<UUID, Long> confirmationPlayers = new HashMap<>();
    private final Map<UUID, String> lastCommandType = new HashMap<>();
    private static final long CONFIRM_TIMEOUT = 10000; // 10 seconds

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.colorize("&cOnly players can use this command."));
            return true;
        }

        UUID uuid = player.getUniqueId();
        String cmdName = label.toLowerCase();
        long now = System.currentTimeMillis();

        if (confirmationPlayers.containsKey(uuid) &&
                now - confirmationPlayers.get(uuid) < CONFIRM_TIMEOUT &&
                cmdName.equals(lastCommandType.get(uuid))) {

            // Confirmed - execute internal ExcellentShop command
            confirmationPlayers.remove(uuid);
            lastCommandType.remove(uuid);

            String internalCmd = cmdName + "_internal";
            player.performCommand(internalCmd);
        } else {
            // First time - request confirmation
            confirmationPlayers.put(uuid, now);
            lastCommandType.put(uuid, cmdName);

            player.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
            player.sendMessage(ChatUtils.colorize("   &c&lSELL ALL CONFIRMATION"));
            player.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
            player.sendMessage(ChatUtils.colorize(" &8» &fKamu yakin ingin menjual &cSEMUA &fitem?"));
            player.sendMessage(ChatUtils.colorize(" &8» &fKetik &e/" + cmdName + " &fsekali lagi untuk konfirmasi."));
            player.sendMessage(ChatUtils.colorize(" &8» &7Konfirmasi berakhir dalam 10 detik."));
            player.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        }

        return true;
    }
}
