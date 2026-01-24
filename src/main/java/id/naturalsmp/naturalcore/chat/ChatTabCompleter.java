package id.naturalsmp.naturalcore.chat;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.event.server.TabCompleteEvent;

public class ChatTabCompleter implements Listener {

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent event) {
        // Only trigger for chat tab completion (not in commands usually, but chat is
        // empty buffer)
        // In Paper, AsyncTabCompleteEvent is fired for chat if nothing else handles it.
        String buffer = event.getBuffer();

        // If buffer is empty or doesn't start with /, it's chat
        if (!buffer.startsWith("/") && buffer.contains("@")) {
            int lastAtIndex = buffer.lastIndexOf("@");
            String search = buffer.substring(lastAtIndex + 1).toLowerCase();

            // Only trigger if @ is at the start or preceded by a space
            if (lastAtIndex > 0 && buffer.charAt(lastAtIndex - 1) != ' ')
                return;

            List<String> matches = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .map(name -> "@" + name)
                    .collect(Collectors.toList());

            if (!matches.isEmpty()) {
                event.completions(matches.stream()
                        .map(AsyncTabCompleteEvent.Completion::completion)
                        .collect(Collectors.toList()));
                event.setHandled(true);
            }
        }
    }

    @EventHandler
    public void onSyncTabComplete(TabCompleteEvent event) {
        String buffer = event.getBuffer();
        if (!buffer.startsWith("/") && buffer.contains("@")) {
            int lastAtIndex = buffer.lastIndexOf("@");
            String search = buffer.substring(lastAtIndex + 1).toLowerCase();

            List<String> suggestions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .map(name -> "@" + name)
                    .collect(Collectors.toList());

            if (!suggestions.isEmpty()) {
                List<String> current = new ArrayList<>(event.getCompletions());
                current.addAll(suggestions);
                event.setCompletions(current);
            }
        }
    }
}
