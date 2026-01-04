package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class NaturalCoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Cek Permission Admin
        if (!sender.hasPermission("naturalsmp.admin")) {
            sender.sendMessage(ConfigUtils.getString("messages.no-permission"));
            return true;
        }

        // Jika cuma ketik /nacore (tanpa argumen) -> Tampilkan Help Menu Mewah
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        // Sub-commands
        switch (args[0].toLowerCase()) {
            case "reload":
            case "rl":
                ConfigUtils.reload();
                sender.sendMessage(ConfigUtils.getString("prefix.admin") + ConfigUtils.getString("messages.reload-success"));
                break;

            case "version":
            case "ver":
                sender.sendMessage(ChatUtils.colorize("&bNaturalCore &7v" + NaturalCore.getInstance().getDescription().getVersion()));
                break;

            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        String header = "&8&m     &6&l NATURAL CORE &7v" + NaturalCore.getInstance().getDescription().getVersion() + " &8&m     ";

        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize(header));
        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore reload  &7- &fReload config.yml"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore version &7- &fCek versi plugin"));
        sender.sendMessage(ChatUtils.colorize(" &e/kickall        &7- &fKick semua player"));
        sender.sendMessage(ChatUtils.colorize(" &e/restartalert   &7- &fMulai hitungan restart"));
        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize("&8&m                                     "));
    }
}