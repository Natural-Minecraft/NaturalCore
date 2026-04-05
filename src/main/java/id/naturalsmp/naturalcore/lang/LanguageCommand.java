package id.naturalsmp.naturalcore.lang;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LanguageCommand implements CommandExecutor {

    private final LanguageGUI gui;

    public LanguageCommand(LanguageGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Command ini hanya dapat digunakan oleh player.");
            return true;
        }

        gui.openGUI(p);
        return true;
    }
}
