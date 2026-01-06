package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NaturalCoreCommand implements CommandExecutor {

    private final NaturalCoreGUI gui;

    public NaturalCoreCommand(NaturalCore plugin) {
        this.gui = new NaturalCoreGUI(plugin);

        plugin.getServer().getPluginManager().registerEvents(gui, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("naturalsmp.admin")) {
            sender.sendMessage(ConfigUtils.getString("messages.no-permission"));
            return true;
        }

        // Jika player ketik /nacore tanpa argumen -> Buka GUI
        if (args.length == 0) {
            if (sender instanceof Player) {
                gui.openGUI((Player) sender);
            } else {
                sender.sendMessage("Console gunakan: /nacore help");
            }
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

            // Fallback command help versi Text (buat Console)
            case "help":
                sender.sendMessage(ChatUtils.colorize("&e--- NaturalCore Commands ---"));
                sender.sendMessage(ChatUtils.colorize("&f/kickall, /restartalert, /bc"));
                sender.sendMessage(ChatUtils.colorize("&f/warps, /setwarp, /delwarp"));
                sender.sendMessage(ChatUtils.colorize("&f/wt, /settrader"));
                break;

            default:
                if (sender instanceof Player) {
                    gui.openGUI((Player) sender);
                }
                break;
        }

        return true;
    }
}