package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.NaturalCoreGUI;
import id.naturalsmp.naturalcore.economy.EconomyCommand;
import id.naturalsmp.naturalcore.moderation.ModerationCommand;
import id.naturalsmp.naturalcore.utility.PlayerUtilCommand;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.TabCompleter;

public class NaturalCoreCommand implements CommandExecutor, TabCompleter {

    private final NaturalCore plugin;

    // Instance command lain
    private final PlayerUtilCommand playerUtil;
    private final EconomyCommand economyUtil;
    private final ModerationCommand modUtil;

    public NaturalCoreCommand(NaturalCore plugin) {
        this.plugin = plugin;
        this.playerUtil = new PlayerUtilCommand();
        this.economyUtil = new EconomyCommand();
        this.modUtil = new ModerationCommand(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Hanya player yang bisa menggunakan perintah ini.");
                return true;
            }
            Player p = (Player) sender;
            if (p.hasPermission("naturalsmp.admin")) {
                new NaturalCoreGUI(plugin).openGUI(p);
            } else {
                ConfigUtils.sendGeneral(p, "messages.global.version", "%version%",
                        plugin.getDescription().getVersion());
                ConfigUtils.sendGeneral(p, "messages.global.help-hint");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- SUB: ADMIN (Cascading Admin Branch) ---
        if (sub.equals("admin")) {
            if (!sender.hasPermission("naturalsmp.admin"))
                return noPerm(sender);
            if (args.length < 2) {
                sender.sendMessage(ChatUtils.colorize("&cUsage: /nacore admin <reload|resetseason|gui>"));
                return true;
            }
            String adminSub = args[1].toLowerCase();
            String[] adminArgs = Arrays.copyOfRange(args, 2, args.length);

            switch (adminSub) {
                case "reload" -> {
                    sender.sendMessage(ChatUtils.colorize("&6&lNaturalCore &8» &fRefreshing system..."));
                    performDeepReload(sender);
                }
                case "ranksync" -> {
                    sender.sendMessage(ChatUtils.colorize("&6&lPermissions &8» &fSyncing to LuckPerms..."));
                    plugin.getPermissionManager().syncToLuckPerms();
                    sender.sendMessage(ChatUtils.colorize("&6&lPermissions &8» &aSync complete! 🔑"));
                }
                case "resetseason" -> {
                    if (adminArgs.length < 1 || !adminArgs[0].equalsIgnoreCase("confirm")) {
                        sender.sendMessage(ChatUtils.colorize("&c&lWARNING! &7Reset 50% Tier & AuraSkills."));
                        sender.sendMessage(ChatUtils.colorize("&7Gunakan: &f/nacore admin resetseason confirm"));
                    } else {
                        plugin.getSeasonResetManager().performFullReset(sender);
                    }
                }
                case "status" -> {
                    if (sender instanceof Player p)
                        plugin.getStatusGUI().openGUI(p);
                }
                case "backup" -> {
                    if (plugin.getBackupManager() != null) {
                        sender.sendMessage(ChatUtils.colorize("&6&lBackup &8» &fCreating manual backup..."));
                        plugin.getBackupManager().createBackup("ManualBackup");
                        sender.sendMessage(ChatUtils.colorize("&6&lBackup &8» &aDone!"));
                    }
                }
                case "gui" -> {
                    if (sender instanceof Player p)
                        new NaturalCoreGUI(plugin).openGUI(p);
                }
                default -> sender.sendMessage(ChatUtils.colorize("&cAdmin sub-command tidak ditemukan."));
            }
            return true;
        }

        // --- GLOBAL RELOAD (Legacy Support) ---
        if (sub.equals("reload")) {
            if (!sender.hasPermission("naturalsmp.admin"))
                return noPerm(sender);
            performDeepReload(sender);
            return true;
        }

        if (sub.equals("ver") || sub.equals("version")) {
            if (sender instanceof Player p) {
                ConfigUtils.sendGeneral(p, "messages.global.version", "%version%",
                        plugin.getDescription().getVersion());
            } else {
                sender.sendMessage("NaturalCore v" + plugin.getDescription().getVersion());
            }
            return true;
        }

        // --- DYNAMIC PROXY DISPATCH ---
        String[] proxyArgs = Arrays.copyOfRange(args, 1, args.length);

        // List of all mapped commands to proxy
        switch (sub) {
            case "heal", "feed", "fly" -> {
                playerUtil.onCommand(sender, command, sub, proxyArgs);
                return true;
            }
            case "bal", "pay", "setbal", "takebal" -> {
                economyUtil.onCommand(sender, command, sub, proxyArgs);
                return true;
            }
            case "god", "vanish", "v", "whois" -> {
                modUtil.onCommand(sender, command, sub, proxyArgs);
                return true;
            }
            case "spawn" -> {
                new id.naturalsmp.naturalcore.spawn.SpawnCommand(plugin.getSpawnManager()).onCommand(sender, command,
                        sub, proxyArgs);
                return true;
            }
            case "home", "homes", "sethome", "delhome" -> {
                new id.naturalsmp.naturalcore.home.HomeCommand(plugin.getHomeManager(),
                        plugin.getHomeGUI()).onCommand(sender, command, sub, proxyArgs);
                return true;
            }
        }

        sender.sendMessage(ChatUtils.colorize("&cSub-command tidak ditemukan. Cek /nacore help."));
        return true;
    }

    private void performDeepReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatUtils.colorize("&aAll system configurations have been deep-refreshed!"));
    }

    private boolean noPerm(CommandSender s) {
        if (s instanceof Player p) {
            ConfigUtils.sendGeneral(p, "messages.global.no-permission");
        } else {
            s.sendMessage("You don't have permission.");
        }
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {

        if (args.length == 1) {
            List<String> list = new ArrayList<>(
                    Arrays.asList("admin", "version", "help", "spawn", "home", "bal", "vanish", "heal", "feed", "fly"));
            if (sender.hasPermission("naturalsmp.admin"))
                list.add("reload");
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return Arrays.asList("reload", "resetseason", "ranksync", "gui", "status", "backup").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase())).collect(java.util.stream.Collectors.toList());
        }

        return java.util.Collections.emptyList();
    }
}
