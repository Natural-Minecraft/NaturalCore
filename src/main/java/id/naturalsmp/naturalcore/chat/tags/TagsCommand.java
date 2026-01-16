package id.naturalsmp.naturalcore.chat.tags;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TagsCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public TagsCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;

        // Open GUI
        new TagsGUI(plugin).openGUI((Player) sender);
        return true;
    }
}
