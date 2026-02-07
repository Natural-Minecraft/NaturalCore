package id.naturalsmp.naturalcore.gamemode;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.bukkit.command.TabCompleter;

public class GamemodeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            // Allow console to use /gm <mode> <player>
            if (args.length < 2) {
                sender.sendMessage("Usage: /gm <mode> <player>");
                return true;
            }
        }

        GameMode mode = null;
        Player target;

        // 1. Detect Command & Mode
        String cmd = label.toLowerCase();

        // Direct Shortcuts
        if (cmd.equals("gmc") || cmd.startsWith("creative"))
            mode = GameMode.CREATIVE;
        else if (cmd.equals("gms"))
            mode = GameMode.SURVIVAL;
        else if (cmd.equals("gma") || cmd.startsWith("adventure"))
            mode = GameMode.ADVENTURE;
        else if (cmd.equals("gmsp") || cmd.startsWith("spec"))
            mode = GameMode.SPECTATOR;

        // Argument parsing
        if (mode != null) {
            // Case: /gmc [player]
            if (args.length > 0) {
                target = Bukkit.getPlayer(args[0]);
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("Console must specify a player.");
                return true;
            }
        } else {
            // Case: /gm <mode> [player]
            if (args.length < 1) {
                sender.sendMessage(ChatUtils.colorize("&cUsage: /gamemode <mode> [player]"));
                return true;
            }
            mode = getGameMode(args[0]);
            if (mode == null) {
                sender.sendMessage(ChatUtils.colorize("&cInvalid gamemode. Use 0, 1, 2, 3 or name."));
                return true;
            }

            if (args.length > 1) {
                target = Bukkit.getPlayer(args[1]);
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("Console must specify a player.");
                return true;
            }
        }

        if (target == null) {
            ConfigUtils.sendError(sender, ConfigUtils.getString("messages.global.player-not-found", "Player not found")
                    .replace("%player%", args.length > 0 ? args[args.length - 1] : "target"));
            return true;
        }

        // 2. Permission Checks
        // Self
        if (sender.equals(target)) {
            if (!sender.hasPermission("naturalsmp.gamemode")
                    && !sender.hasPermission("naturalsmp.gamemode." + mode.name().toLowerCase())) {
                sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
        } else {
            // Other
            if (!sender.hasPermission("naturalsmp.gamemode.others")) {
                sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
        }

        // 3. Execution
        target.setGameMode(mode);

        ConfigUtils.sendAdmin(target, "messages.essentials.gamemode-changed", "%mode%",
                ChatUtils.colorize("&e" + mode.name()));

        if (!sender.equals(target)) {
            ConfigUtils.sendAdmin(sender, "messages.essentials.gamemode-changed-other",
                    "%target%", target.getName(),
                    "%mode%", ChatUtils.colorize("&e" + mode.name()));
        }

        return true;
    }

    private GameMode getGameMode(String arg) {
        arg = arg.toLowerCase();
        if (arg.equals("0") || arg.startsWith("surv") || arg.equalsIgnoreCase("s"))
            return GameMode.SURVIVAL;
        if (arg.equals("1") || arg.startsWith("crea") || arg.equalsIgnoreCase("c"))
            return GameMode.CREATIVE;
        if (arg.equals("2") || arg.startsWith("adven") || arg.equalsIgnoreCase("a"))
            return GameMode.ADVENTURE;
        if (arg.equals("3") || arg.startsWith("spec") || arg.equalsIgnoreCase("sp"))
            return GameMode.SPECTATOR;
        return null;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        String cmd = alias.toLowerCase();

        // Shortcut commands: /gmc <player>
        if (cmd.equals("gmc") || cmd.equals("gms") || cmd.equals("gma") || cmd.equals("gmsp")) {
            if (args.length == 1) {
                return null; // Return null to let Bukkit suggest player names
            }
            return java.util.Collections.emptyList();
        }

        // Standard command: /gm <mode> <player>
        if (args.length == 1) {
            return java.util.stream.Stream.of("survival", "creative", "adventure", "spectator", "0", "1", "2", "3")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (args.length == 2) {
            return null; // Suggest players
        }

        return java.util.Collections.emptyList();
    }
}