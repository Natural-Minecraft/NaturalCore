package id.naturalsmp.naturalcore.chat.tags;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagsTabCompleter implements TabCompleter {

    private final NaturalCore plugin;

    public TagsTabCompleter(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest tag IDs
            suggestions.addAll(plugin.getTagsManager().getAvailableTags().keySet());

            // Suggest admin commands if applicable
            if (sender.hasPermission("naturalsmp.admin")) {
                suggestions.add("reload");
            }

            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return suggestions;
    }
}
