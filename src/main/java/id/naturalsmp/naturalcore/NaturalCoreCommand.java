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

public class NaturalCoreCommand implements CommandExecutor {

    private final NaturalCore plugin;

    // Instance command lain untuk dipanggil via /nacore
    private final PlayerUtilCommand playerUtil;
    private final EconomyCommand economyUtil;
    private final ModerationCommand modUtil; // <--- Variable tipe baru

    public NaturalCoreCommand(NaturalCore plugin) {
        this.plugin = plugin;
        this.playerUtil = new PlayerUtilCommand();
        this.economyUtil = new EconomyCommand();
        // Init ModerationCommand (butuh plugin instance)
        this.modUtil = new ModerationCommand(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // /nacore (Buka GUI)
        if (args.length == 0) {
            if (sender instanceof Player) {
                new NaturalCoreGUI(plugin).openGUI((Player) sender);
            } else {
                sender.sendMessage("Hanya player yang bisa membuka GUI.");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- ADMIN UTILS ---
        if (sub.equals("reload")) {
            if (!sender.hasPermission("naturalsmp.admin")) return noPerm(sender);
            plugin.reloadConfig();
            ConfigUtils.reload();
            sender.sendMessage(ConfigUtils.getString("prefix.admin") + ConfigUtils.getString("messages.reload-success"));
            return true;
        }

        if (sub.equals("version") || sub.equals("ver")) {
            sender.sendMessage(ChatUtils.colorize("&6NaturalCore v" + plugin.getDescription().getVersion()));
            return true;
        }

        // --- PROXY KE COMMAND LAIN ---
        // Geser argumen: "/nacore heal steve" -> args untuk heal adalah "steve"
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        // 1. Player Utils (Heal, Feed, Fly)
        if (sub.equals("heal") || sub.equals("feed") || sub.equals("fly")) {
            playerUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        // 2. Economy (Setbal, Takebal, Bal, Pay)
        if (sub.equals("setbal") || sub.equals("takebal") || sub.equals("bal") || sub.equals("pay")) {
            economyUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        // 3. Moderation (God, Vanish, Whois)
        // Delegate ke ModerationCommand
        if (sub.equals("god") || sub.equals("vanish") || sub.equals("v") || sub.equals("whois")) {
            modUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        sender.sendMessage(ChatUtils.colorize("&cSub-command tidak ditemukan."));
        return true;
    }

    private boolean noPerm(CommandSender s) {
        s.sendMessage(ConfigUtils.getString("messages.no-permission"));
        return true;
    }
}