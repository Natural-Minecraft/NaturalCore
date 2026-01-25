package id.naturalsmp.naturalcore.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankCommand implements CommandExecutor {

    private final RankGUI gui;

    public RankCommand(RankGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage("Hanya player yang bisa menggunakan command ini!");
            return true;
        }

        gui.openGUI(p);
        return true;
    }
}
