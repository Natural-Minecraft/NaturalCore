package id.naturalsmp.naturalcore.media;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MediaCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public MediaCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Hanya player yang dapat menggunakan command ini.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("benefits")) {
            plugin.getMediaGUI().openBenefitsGUI(p);
            return true;
        }

        plugin.getMediaGUI().openGUI(p);
        return true;
    }
}
