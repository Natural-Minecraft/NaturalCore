package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuCommand implements CommandExecutor {

    private final MenuGUI gui;

    public MenuCommand(NaturalCore plugin) {
        this.gui = new MenuGUI(plugin);
        // Register events for the GUI
        plugin.getServer().getPluginManager().registerEvents(gui, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Hanya pemain yang bisa menggunakan menu ini!");
            return true;
        }

        gui.openGUI((Player) sender);
        return true;
    }
}
