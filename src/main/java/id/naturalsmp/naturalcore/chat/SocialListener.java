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

        // 1. Vanish Check
        if (plugin.getVanishManager().isVanished(p)) {
            e.joinMessage(null);
            return;
        }

        // 2. First Join Check
        if (!p.hasPlayedBefore()) {
            e.joinMessage(null);
            List<String> lines = ConfigUtils.getStringList("messages.social.first-join-message");
            int playerCount = Bukkit.getOfflinePlayers().length;
            for (String line : lines) {
                GUIUtils.broadcast(ChatUtils.colorize(
                        line.replace("%displayname%", p.getName()).replace("%count%", String.valueOf(playerCount))));
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                online.playSound(online.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1.2f);
            }
            return;
        }

        // 3. Nature Rank Broadcast (Priority)
        if (p.hasPermission("naturalsmp.nature")) {
            e.joinMessage(null);
            List<String> lines = ConfigUtils.getStringList("messages.social.nature-join");
            for (String line : lines) {
                GUIUtils.broadcast(ChatUtils.formatMessage(p, line));
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f);
            }
            // 4. Default Join
            String joinMsg = ConfigUtils.getString("messages.social.join-message");
            e.joinMessage(ChatUtils.toComponent(ChatUtils.formatMessage(p, joinMsg)));
        }

        // 5. MOTD (New)
        List<String> motd = ConfigUtils.getStringList("messages.social.motd");
        if (motd != null && !motd.isEmpty()) {
            for (String line : motd) {
                p.sendMessage(ChatUtils.toComponent(ChatUtils.formatMessage(p, line)));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (plugin.getVanishManager().isVanished(p)) {
            e.quitMessage(null);
            return;
        }

        String quitMsg = ConfigUtils.getString("messages.social.quit-message");
        e.quitMessage(ChatUtils.toComponent(ChatUtils.formatMessage(p, quitMsg)));
    }
}
