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

import java.util.Arrays;

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

        // --- COMMAND UTAMA: /nacore (Buka GUI) ---
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Hanya player yang bisa membuka GUI.");
                return true;
            }
            Player p = (Player) sender;

            // 1. SECURITY CHECK: Cek Permission Admin
            if (!p.hasPermission("naturalsmp.admin")) {
                return noPerm(p);
            }

            // 2. Open GUI
            new NaturalCoreGUI(plugin).openGUI(p);
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- RELOAD CONFIG ---
        if (sub.equals("reload")) {
            if (!sender.hasPermission("naturalsmp.admin"))
                return noPerm(sender);

            plugin.reloadConfig();
            ConfigUtils.reload();

            // Reload Emoji Registry
            if (plugin.getEmojiManager() != null) {
                plugin.getEmojiManager().loadEmojis();
            }

            sender.sendMessage(ConfigUtils.getString("messages.global.reload-success"));
            return true;
        }

        if (sub.equals("version") || sub.equals("ver")) {
            sender.sendMessage(ChatUtils.colorize("&6NaturalCore v" + plugin.getDescription().getVersion()));
            return true;
        }

        // --- PROXY SUB-COMMANDS ---
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        // Player Utils
        if (sub.equals("heal") || sub.equals("feed") || sub.equals("fly")) {
            playerUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        // Economy
        if (sub.equals("setbal") || sub.equals("takebal") || sub.equals("bal") || sub.equals("pay")) {
            economyUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        // Moderation
        if (sub.equals("god") || sub.equals("vanish") || sub.equals("v") || sub.equals("whois")) {
            modUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        if (sub.equals("resetseason")) {
            if (!sender.hasPermission("naturalsmp.admin")) {
                return noPerm(sender);
            }

            if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
                sender.sendMessage(ChatUtils.colorize(
                        "&c&lWARNING! &7Perintah ini akan me-reset &650% Tier &7dan &650% AuraSkills &7seluruh player."));
                sender.sendMessage(
                        ChatUtils.colorize("&7Gunakan: &f/nacore resetseason confirm &7untuk mengeksekusi."));
                return true;
            }

            plugin.getSeasonResetManager().performFullReset(sender);
            return true;
        }

        sender.sendMessage(ChatUtils.colorize("&cSub-command tidak ditemukan."));
        return true;
    }

    private boolean noPerm(CommandSender s) {
        s.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return java.util.stream.Stream.of("reload", "version", "menu", "admin", "resetseason")
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("resetseason")) {
            if (sender.hasPermission("naturalsmp.admin")) {
                return java.util.Collections.singletonList("confirm");
            }
        }

        return java.util.Collections.emptyList();
    }
}