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
        String buffer = event.getBuffer();

        // Skip if it's a command (starts with /)
        if (buffer.startsWith("/"))
            return;

        // Find the last token to see if it starts with @
        String[] tokens = buffer.split(" ");
        if (tokens.length == 0)
            return;

        String lastToken = tokens[tokens.length - 1];

        if (lastToken.startsWith("@")) {
            String search = lastToken.substring(1).toLowerCase();
            List<String> matches = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .map(name -> {
                        // Reconstruct buffer with the new name
                        // We need to replace the LAST token
                        int lastIndex = buffer.lastIndexOf(lastToken);
                        return buffer.substring(0, lastIndex) + "@" + name;
                    })
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
        if (buffer.startsWith("/"))
            return;

        int lastAtIndex = buffer.lastIndexOf("@");
        if (lastAtIndex == -1)
            return;
        if (lastAtIndex > 0 && buffer.charAt(lastAtIndex - 1) != ' ')
            return;

        String search = buffer.substring(lastAtIndex + 1).toLowerCase();
        if (search.contains(" "))
            return;

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
