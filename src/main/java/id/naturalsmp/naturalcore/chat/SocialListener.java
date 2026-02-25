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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SocialListener implements Listener {

    private final NaturalCore plugin;
    private final Map<UUID, Long> sessionTimes = new HashMap<>();
    private final Random random = new Random();

    public SocialListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent e) {
        int onlineCount = Bukkit.getOnlinePlayers().size();

        // Custom Priority Logic: Real max players could be 100, but we limit to 69
        // visually.
        if (onlineCount >= 69) {
            Player p = e.getPlayer();
            if (p.hasPermission("naturalsmp.priority")) {
                e.allow(); // Priority Join for Nature Rank

                // Find someone to kick if we are strictly full based on the 69 fake cap
                // Find all online players without priority
                List<Player> nonPriority = new ArrayList<>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.hasPermission("naturalsmp.priority") && !online.hasPermission("naturalsmp.admin")) {
                        nonPriority.add(online);
                    }
                }

                if (!nonPriority.isEmpty()) {
                    // Sort by oldest session (smallest timestamp first)
                    nonPriority.sort(Comparator.comparingLong(
                            player -> sessionTimes.getOrDefault(player.getUniqueId(), System.currentTimeMillis())));

                    // Take the 10 oldest players (or fewer if there aren't 10)
                    int limit = Math.min(10, nonPriority.size());
                    List<Player> oldestPlayers = nonPriority.subList(0, limit);

                    // Pick a random victim from the 10 oldest
                    Player victim = oldestPlayers.get(random.nextInt(oldestPlayers.size()));

                    // Kick them sync
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        victim.kick(ChatUtils.toComponent(
                                "&cServer penuh! Kamu dikeluarkan secara acak untuk memberikan ruang kepada pemain Prioritas!"));
                    });
                }
            } else {
                e.disallow(PlayerLoginEvent.Result.KICK_FULL,
                        ChatUtils.toComponent("&cServer sedang penuh! Beli rank &e&lNATURE &cuntuk masuk kapan saja!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
        }

        // 4. Priority Woahh Join Effect (if permission naturalsmp.priority)
        if (p.hasPermission("naturalsmp.priority")) {
            GUIUtils.broadcast(ChatUtils
                    .colorize("&e&l>&6&l> &b&l" + p.getName() + " &a&lhas swooped into the server! &6&l<&e&l<"));
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
                online.playSound(online.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1f);
            }
        }

        // 5. Default Join
        String joinMsg = ConfigUtils.getString("messages.social.join-message");
        if (joinMsg != null && !joinMsg.isEmpty()) {
            e.joinMessage(ChatUtils.toComponent(ChatUtils.formatMessage(p, joinMsg)));
        }

        // 6. MOTD (New)
        List<String> motd = ConfigUtils.getStringList("messages.social.motd");
        if (motd != null && !motd.isEmpty()) {
            for (String line : motd) {
                p.sendMessage(ChatUtils.toComponent(ChatUtils.formatMessage(p, line)));
            }
        }

        sessionTimes.put(p.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (plugin.getVanishManager().isVanished(p)) {
            e.quitMessage(null);
            return;
        }

        String quitMsg = ConfigUtils.getString("messages.social.quit-message");
        e.quitMessage(ChatUtils.toComponent(ChatUtils.formatMessage(p, quitMsg)));

        sessionTimes.remove(p.getUniqueId());
    }
}
