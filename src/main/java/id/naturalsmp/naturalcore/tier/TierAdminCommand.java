package id.naturalsmp.naturalcore.tier;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TierAdminCommand implements CommandExecutor, TabCompleter {

    private final NaturalCore plugin;

    public TierAdminCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("naturalsmp.admin")) {
            sender.sendMessage(ChatUtils.colorize("&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("upgrade") || args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(ChatUtils.colorize("&cUsage: /tieradmin upgrade <player> <level>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatUtils.colorize("&cPlayer not found."));
                return true;
            }

            try {
                int level = Integer.parseInt(args[2]);
                if (plugin.getTierManager().getTier(level) == null) {
                    sender.sendMessage(ChatUtils.colorize("&cLevel " + level + " is not a valid tier level."));
                    return true;
                }

                plugin.getTierManager().setPlayerLevel(target.getUniqueId(), level);
                plugin.getTierManager().savePlayerDataPublic();

                sender.sendMessage(ChatUtils
                        .colorize("&aSuccessfully updated &e" + target.getName() + "&a to tier level &e" + level));
                target.sendMessage(ChatUtils.colorize("&aYour tier has been set to "
                        + plugin.getTierManager().getTier(level).display + " &aby an administrator."));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatUtils.colorize("&cInvalid level number."));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("edit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can open the GUI.");
                return true;
            }
            new TierAdminGUI(plugin).openGUI((Player) sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatUtils.colorize("&6&lTier Admin Help:"));
        sender.sendMessage(ChatUtils.colorize("&e/tieradmin upgrade <player> <level> &7- Set player tier level"));
        sender.sendMessage(ChatUtils.colorize("&e/tieradmin edit &7- Open requirement editor GUI"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("naturalsmp.admin"))
            return null;

        if (args.length == 1) {
            return Arrays.asList("upgrade", "edit").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("upgrade")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("upgrade")) {
            List<String> levels = new ArrayList<>();
            for (int i = 1; i <= 28; i++) {
                levels.add(String.valueOf(i));
            }
            return levels.stream()
                    .filter(s -> s.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
