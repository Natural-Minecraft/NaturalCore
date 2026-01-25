package id.naturalsmp.naturalcore.listeners;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandDisablerListener implements Listener {

    private final NaturalCore plugin;

    public CommandDisablerListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission("naturalsmp.admin"))
            return;

        List<String> disabled = ConfigUtils.getDisabledCommandsConfig().getStringList("disabled-commands");
        if (disabled == null || disabled.isEmpty())
            return;

        String message = e.getMessage().toLowerCase();
        String cmd = message.split(" ")[0].replace("/", "");

        if (disabled.contains(cmd)) {
            e.setCancelled(true);
            String prefix = ConfigUtils.getString("prefix.player");
            e.getPlayer().sendMessage(
                    ChatUtils.colorize(prefix + "&cMaaf, perintah ini sedang dinonaktifkan oleh administrator."));
        }
    }
}
