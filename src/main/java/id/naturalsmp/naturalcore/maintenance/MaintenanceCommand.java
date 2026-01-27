package id.naturalsmp.naturalcore.maintenance;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MaintenanceCommand implements CommandExecutor {

    private final MaintenanceManager manager;

    public MaintenanceCommand(MaintenanceManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("naturalsmp.maintenance.admin")) {
            sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on", "enable" -> {
                int seconds = 30;
                if (args.length > 1) {
                    try {
                        seconds = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                manager.scheduleMaintenance(seconds);
                sender.sendMessage(
                        ChatUtils.colorize("&6&lMaintenance &8» &7Memulai hitung mundur &e" + seconds + "s&7."));
            }
            case "off", "disable" -> {
                manager.setMaintenance(false);
                sender.sendMessage(
                        ChatUtils.colorize("&6&lMaintenance &8» &7Mode Maintenance telah &cNonaiktifkan&7."));
            }
                manager.addWhitelist(args[1]);
                sender.sendMessage(ChatUtils
                        .colorize("&6&lMaintenance &8» &7Player &e" + args[1] + " &7telah ditambahkan ke whitelist."));
            }
            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatUtils.colorize("&cUsage: /maintenance remove <player>"));
                    return true;
                }
                manager.removeWhitelist(args[1]);
                sender.sendMessage(ChatUtils
                        .colorize("&6&lMaintenance &8» &7Player &e" + args[1] + " &7telah dihapus dari whitelist."));
            }
            case "status" -> {
                String status = manager.isActive() ? "&aAKTIF" : "&cNONAKTIF";
                sender.sendMessage(ChatUtils.colorize("&6&lMaintenance &8» &7Status saat ini: " + status));
            }
            default -> sendUsage(sender);
        }

    return true;

    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatUtils.colorize("&8&l&m---------------------------------"));
        sender.sendMessage(ChatUtils.colorize("   &6&lMAINTENANCE &f&lCONTROL"));
        sender.sendMessage("");
        sender.sendMessage(ChatUtils.colorize(" &8» &e/maintenance on [detik] &7- Mulai maintenance"));
        sender.sendMessage(ChatUtils.colorize(" &8» &e/maintenance off &7- Matikan maintenance"));
        sender.sendMessage(ChatUtils.colorize(" &8» &e/maintenance add <player> &7- Bypass maintenance"));
        sender.sendMessage(ChatUtils.colorize(" &8» &e/maintenance remove <player> &7- Hapus bypass"));
        sender.sendMessage(ChatUtils.colorize(" &8» &e/maintenance status &7- Cek status"));
        sender.sendMessage(ChatUtils.colorize("&8&l&m---------------------------------"));
    }
}
