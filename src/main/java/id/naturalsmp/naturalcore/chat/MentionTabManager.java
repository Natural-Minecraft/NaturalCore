package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages modern chat suggestions for player mentions.
 * Uses Paper's addCustomChatCompletions API to sync with the client's chat bar.
 */
public class MentionTabManager implements Listener {

    private final NaturalCore plugin;

    public MentionTabManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Initial sync for all online players.
     */
    public void initialSync() {
        List<String> playerMentions = getPlayerMentions();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.addCustomChatCompletions(playerMentions);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        String mention = "@" + joined.getName();

        // Add the new player to everyone else's completions
        List<String> newMention = Collections.singletonList(mention);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.addCustomChatCompletions(newMention);
        }

        // Give the joined player all current mentions
        joined.addCustomChatCompletions(getPlayerMentions());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        String mention = "@" + event.getPlayer().getName();
        List<String> removedMention = Collections.singletonList(mention);

        // Remove the leaving player from everyone else's completions
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(event.getPlayer())) {
                online.removeCustomChatCompletions(removedMention);
            }
        }
    }

    private List<String> getPlayerMentions() {
        return Bukkit.getOnlinePlayers().stream()
                .map(p -> "@" + p.getName())
                .collect(Collectors.toList());
    }
}
