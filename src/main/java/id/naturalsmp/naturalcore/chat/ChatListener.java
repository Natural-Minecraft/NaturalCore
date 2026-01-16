package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {

    @SuppressWarnings("unused")
    private final NaturalCore plugin;

    public ChatListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    // 1. CHAT FORMATTING
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!ConfigUtils.getBoolean("chat.enabled"))
            return;

        Player p = e.getPlayer();
        String message = e.getMessage();

        // Izin Warna Chat
        if (ConfigUtils.getBoolean("chat.allow-color") && p.hasPermission("naturalsmp.chat.color")) {
            message = ChatUtils.colorize(message);
        }

        // v1.7: ChatColor System Application
        id.naturalsmp.naturalcore.chat.ChatColorManager colorManager = plugin.getChatColorManager();
        if (colorManager != null) {
            String color = colorManager.getPlayerColor(p);
            boolean bold = colorManager.isBold(p);
            boolean italic = colorManager.isItalic(p);
            String font = colorManager.getPlayerFont(p);

            // Apply Font Transformation
            message = colorManager.applyFont(message, font);

            // Apply Styles Prefix
            StringBuilder style = new StringBuilder(color);
            if (bold)
                style.append("&l");
            if (italic)
                style.append("&o");

            // Prepend style (reset handled by Bukkit usually at end of line, or we can
            // force it)
            // We prepend uncolored codes (e.g. &c) so ChatUtils.colorize or final format
            // picks it up?
            // Actually, message might already be colorized above if user typed &codes.
            // We should colorize our prefix NOW.
            message = ChatUtils.colorize(style.toString()) + message;
        }

        // Parse Emojis (NEW - ChatEmojis Feature)
        if (EmojiManager.getInstance() != null) {
            message = EmojiManager.getInstance().parseEmojis(p, message);
        }

        // Ambil format dari config
        // Default Config: "%displayname% &8» &f{message}"
        String formatRaw = ConfigUtils.getString("chat.format");

        // v1.8: Inject Tags
        id.naturalsmp.naturalcore.chat.tags.TagsManager tagsManager = plugin.getTagsManager();
        String tag = (tagsManager != null) ? tagsManager.getPlayerTag(p) : "";
        // Jika formatRaw tidak punya %tag%, kita paksa taruh di depan nama biar
        // kelihatan
        // Atau buat placeholder manual {tag}
        if (!tag.isEmpty()) {
            // Jika ada tag, kita warnai
            tag = ChatUtils.colorize(tag);
        }

        // Manual Placeholder Replacement for {tag}
        // User disarankan pakai {tag} di config. Jika tidak, kita append di depan.
        if (formatRaw.contains("{tag}")) {
            formatRaw = formatRaw.replace("{tag}", tag);
        } else if (!tag.isEmpty()) {
            // Append force di depan
            formatRaw = tag + formatRaw;
        }

        // v1.8: Inject Tier Suffix
        String suffix = "";
        if (plugin.getTierManager() != null) {
            suffix = plugin.getTierManager().getPlayerSuffix(p);
        }
        // Jika formatRaw tidak punya {tier_suffix}, kita append di belakang nama
        // (biasanya setelah displayname)
        // Tapi formatRaw = "%displayname% message".
        // Kalau kita append ke "formatRaw", misal "%displayname% suffix message".
        // Kita coba replace {tier_suffix} dulu.
        if (formatRaw.contains("{tier_suffix}")) {
            formatRaw = formatRaw.replace("{tier_suffix}", suffix);
        } else if (!suffix.isEmpty()) {
            // Append force setelah displayname (Agak tricky text processing)
            // Simpelnya: Append ke displayname Bukkit? JANGAN, nanti ngerusak plugin lain.
            // Kita replace "%displayname%" jadi "%displayname% suffix" di format string?
            if (formatRaw.contains("%displayname%")) {
                formatRaw = formatRaw.replace("%displayname%", "%displayname%" + suffix);
            } else {
                // Append di depan message? Aneh.
                // Biarkan saja, user disarankan pakai {tier_suffix}
            }
        }

        // Kita replace dulu {message} agar tidak kena filter ChatUtils (biar aman)
        // Sisanya (%displayname% dll) diurus oleh ChatUtils.formatMessage
        String finalFormat = ChatUtils.formatMessage(p, formatRaw)
                .replace("{message}", "%2$s"); // %2$s adalah placeholder pesan asli Bukkit

        // Set Format
        e.setFormat(finalFormat);
        e.setMessage(message);
    }

    // 2. JOIN MESSAGE
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // A. First Join
        if (!p.hasPlayedBefore()) {
            String firstMsg = ConfigUtils.getString("messages.social.first-join-message");
            if (!firstMsg.equalsIgnoreCase("none")) {
                int count = Bukkit.getOfflinePlayers().length;
                // ChatUtils.formatMessage otomatis mengubah %displayname%
                String result = ChatUtils.formatMessage(p, firstMsg).replace("%count%", String.valueOf(count));
                e.setJoinMessage(result);
            }
        }
        // B. Join Biasa
        else {
            String joinMsg = ConfigUtils.getString("messages.social.join-message");
            if (joinMsg.equalsIgnoreCase("none")) {
                e.setJoinMessage(null);
            } else {
                // Cukup panggil ini, simpel banget kan?
                e.setJoinMessage(ChatUtils.formatMessage(p, joinMsg));
            }
        }

        // C. MOTD
        for (String line : ConfigUtils.getStringList("messages.social.motd")) {
            p.sendMessage(ChatUtils.formatMessage(p, line));
        }
    }

    // 3. QUIT MESSAGE
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        // v1.7: Save Location for /otp (Offline TP)
        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().setLastLocation(e.getPlayer());
        }

        String quitMsg = ConfigUtils.getString("messages.social.quit-message");
        if (quitMsg.equalsIgnoreCase("none")) {
            e.setQuitMessage(null);
        } else {
            e.setQuitMessage(ChatUtils.formatMessage(e.getPlayer(), quitMsg));
        }
    }
}