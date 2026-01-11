package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * EmojiCommand - Command untuk menampilkan daftar emoji yang tersedia
 * 
 * Usage:
 * /emoji - Tampilkan daftar emoji
 * /emoji reload - Reload emoji (admin only)
 */
public class EmojiCommand implements CommandExecutor {

    private final NaturalCore plugin;
    private static final int EMOJIS_PER_PAGE = 15;

    public EmojiCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }

        Player p = (Player) sender;

        // /emoji reload (Admin)
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!p.hasPermission("naturalsmp.admin")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }

            if (plugin.getEmojiManager() != null) {
                plugin.getEmojiManager().loadEmojis();
                p.sendMessage(ChatUtils.colorize("&aEmoji registry berhasil di-reload!"));
            }
            return true;
        }

        // /emoji [page]
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                // Bukan angka, gunakan halaman 1
            }
        }

        showEmojiList(p, page);
        return true;
    }

    private void showEmojiList(Player p, int page) {
        EmojiManager manager = plugin.getEmojiManager();
        if (manager == null) {
            p.sendMessage(ChatUtils.colorize("&cEmoji system is not enabled."));
            return;
        }

        // Collect unique emojis (remove duplicate triggers)
        Map<String, EmojiManager.EmojiData> registry = manager.getEmojiRegistry();
        Map<String, EmojiManager.EmojiData> uniqueEmojis = new LinkedHashMap<>();

        for (Map.Entry<String, EmojiManager.EmojiData> entry : registry.entrySet()) {
            String name = entry.getValue().getName();
            if (!uniqueEmojis.containsKey(name)) {
                uniqueEmojis.put(name, entry.getValue());
            }
        }

        List<EmojiManager.EmojiData> emojiList = new ArrayList<>(uniqueEmojis.values());
        int totalPages = (int) Math.ceil((double) emojiList.size() / EMOJIS_PER_PAGE);

        if (page < 1)
            page = 1;
        if (page > totalPages)
            page = totalPages;
        if (totalPages == 0) {
            p.sendMessage(ChatUtils.colorize("&cTidak ada emoji yang terdaftar."));
            return;
        }

        // Header
        p.sendMessage(ChatUtils.colorize(
                "&8&m----------&r &6&lEmoji List &8(&f" + page + "&8/&f" + totalPages + "&8) &8&m----------"));

        // Get page items
        int startIndex = (page - 1) * EMOJIS_PER_PAGE;
        int endIndex = Math.min(startIndex + EMOJIS_PER_PAGE, emojiList.size());

        for (int i = startIndex; i < endIndex; i++) {
            EmojiManager.EmojiData emoji = emojiList.get(i);

            // Check permission
            boolean hasAccess = !emoji.hasPermission() || p.hasPermission(emoji.getPermission())
                    || p.hasPermission("naturalsmp.emoji.*");

            String statusColor = hasAccess ? "&a" : "&c";
            String statusIcon = hasAccess ? "✔" : "✖";

            // Get first trigger for this emoji
            String firstTrigger = getFirstTrigger(registry, emoji.getName());

            // Create clickable component
            TextComponent line = new TextComponent(ChatUtils.colorize(
                    statusColor + statusIcon + " &f" + emoji.getCharacter() + " &7- &e" + emoji.getName() + " &8("
                            + firstTrigger + ")"));

            // Hover text
            line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            ChatUtils.colorize("&7Click to copy: &f" + firstTrigger + "\n&7Permission: &f" +
                                    (emoji.hasPermission() ? emoji.getPermission() : "None")))
                            .create()));

            // Click to suggest
            line.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, firstTrigger));

            p.spigot().sendMessage(line);
        }

        // Footer with navigation
        StringBuilder footer = new StringBuilder("&8&m----------&r ");
        if (page > 1) {
            footer.append("&a« Prev ");
        }
        footer.append("&7Use &f/emoji <page> ");
        if (page < totalPages) {
            footer.append("&aNext »");
        }
        footer.append(" &8&m----------");

        p.sendMessage(ChatUtils.colorize(footer.toString()));
    }

    private String getFirstTrigger(Map<String, EmojiManager.EmojiData> registry, String emojiName) {
        for (Map.Entry<String, EmojiManager.EmojiData> entry : registry.entrySet()) {
            if (entry.getValue().getName().equals(emojiName)) {
                return entry.getKey();
            }
        }
        return "?";
    }
}
