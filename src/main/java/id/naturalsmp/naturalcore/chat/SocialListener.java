package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class SocialListener implements Listener {

    private final NaturalCore plugin;

    public SocialListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (e.getPlayer().hasPermission("naturalsmp.priority")) {
                e.allow(); // Priority Join for Nature Rank
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Hide if join while vanished (if persistence added) - currently handled by
        // VanishManager hideVanishedFrom
        if (plugin.getVanishManager().isVanished(p)) {
            e.joinMessage(null);
            return;
        }

        // --- JOIN MESSAGE ---
        if (p.hasPermission("naturalsmp.nature")) {
            e.joinMessage(null);
            List<String> lines = ConfigUtils.getStringList("social.nature-join");
            for (String line : lines) {
                GUIUtils.broadcast(ChatUtils.formatMessage(p, line));
            }
            // Sound Effect for Nature Join
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f);
            }
        } else {
            String joinMsg = ConfigUtils.getString("social.join-message");
            e.joinMessage(ChatUtils.toComponent(ChatUtils.formatMessage(p, joinMsg)));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (plugin.getVanishManager().isVanished(p)) {
            e.quitMessage(null);
            return;
        }

        String quitMsg = ConfigUtils.getString("social.quit-message");
        e.quitMessage(ChatUtils.toComponent(ChatUtils.formatMessage(p, quitMsg)));
    }
}
