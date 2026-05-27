package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.BedrockUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuCommand implements CommandExecutor {

    private final MenuGUI gui;

    public MenuCommand(NaturalCore plugin) {
        this.gui = new MenuGUI(plugin);
        // Register events for the Custom GUIs
        plugin.getServer().getPluginManager().registerEvents(gui, plugin);
        plugin.getServer().getPluginManager().registerEvents(new TutorialGUI(plugin), plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Hanya pemain yang bisa menggunakan menu ini!");
            return true;
        }

        // Bedrock players get native BedrockGUI form
        if (BedrockUtils.isBedrock(p)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bedrockgui open ponsel " + p.getName());
            return true;
        }

        gui.openGUI(p);
        return true;
    }
}
