package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartCommand implements CommandExecutor {

    private final StartGUI gui;

    public StartCommand(NaturalCore plugin) {
        this.gui = new StartGUI(plugin);
        plugin.getServer().getPluginManager().registerEvents(gui, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Hanya pemain yang bisa menggunakan command ini!");
            return true;
        }

        gui.openGUI((Player) sender);
        return true;
    }
}
