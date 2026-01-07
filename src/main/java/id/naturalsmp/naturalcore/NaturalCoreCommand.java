package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.NaturalCoreGUI;
import id.naturalsmp.naturalcore.economy.EconomyCommand;
import id.naturalsmp.naturalcore.moderation.GodVanishCommand;
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

    // Simpan instance command lain untuk dipanggil
    private final PlayerUtilCommand playerUtil;
    private final EconomyCommand economyUtil;
    private final GodVanishCommand modUtil;

    public NaturalCoreCommand(NaturalCore plugin) {
        this.plugin = plugin;
        this.playerUtil = new PlayerUtilCommand();
        this.economyUtil = new EconomyCommand();
        this.modUtil = new GodVanishCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // /nacore (Buka GUI)
        if (args.length == 0) {
            if (sender instanceof Player) {
                new NaturalCoreGUI(plugin).openGUI((Player) sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- ADMIN UTILS ---
        if (sub.equals("reload")) {
            if (!sender.hasPermission("naturalsmp.admin")) return true;
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
        // Kita geser argumen. Contoh: "/nacore heal steve" -> "heal" "steve"
        // Args baru untuk executor: "steve"
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        // 1. Player Utils (Heal, Feed, Fly)
        if (sub.equals("heal") || sub.equals("feed") || sub.equals("fly")) {
            // Panggil onCommand milik PlayerUtilCommand
            // Label kita palsukan jadi nama subcommand (misal "heal")
            playerUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        // 2. Economy (Setbal, Takebal, Bal, Pay)
        if (sub.equals("setbal") || sub.equals("takebal") || sub.equals("bal") || sub.equals("pay")) {
            economyUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        // 3. Moderation (God, Vanish, Whois)
        if (sub.equals("god") || sub.equals("vanish") || sub.equals("v") || sub.equals("whois")) {
            modUtil.onCommand(sender, command, sub, newArgs);
            return true;
        }

        sender.sendMessage(ChatUtils.colorize("&cSub-command tidak ditemukan."));
        return true;
    }
}