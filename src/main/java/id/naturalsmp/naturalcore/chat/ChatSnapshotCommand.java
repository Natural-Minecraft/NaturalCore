package id.naturalsmp.naturalcore.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatSnapshotCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        if (args.length < 2)
            return true;

        Player player = (Player) sender;
        try {
            UUID id = UUID.fromString(args[0]);
            boolean isEnder = args[1].equalsIgnoreCase("ender");
            ChatPreviewGUI.openSnapshot(player, id, isEnder);
        } catch (IllegalArgumentException e) {
            // Invalid UUID
        }

        return true;
    }
}
