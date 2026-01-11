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

        // Parse Emojis (NEW - ChatEmojis Feature)
        if (EmojiManager.getInstance() != null) {
            message = EmojiManager.getInstance().parseEmojis(p, message);
        }

        // Ambil format dari config
        // Default Config: "%displayname% &8» &f{message}"
        String formatRaw = ConfigUtils.getString("chat.format");

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
            String firstMsg = ConfigUtils.getString("messages.first-join-message");
            if (!firstMsg.equalsIgnoreCase("none")) {
                int count = Bukkit.getOfflinePlayers().length;
                // ChatUtils.formatMessage otomatis mengubah %displayname%
                String result = ChatUtils.formatMessage(p, firstMsg).replace("%count%", String.valueOf(count));
                e.setJoinMessage(result);
            }
        }
        // B. Join Biasa
        else {
            String joinMsg = ConfigUtils.getString("messages.join-message");
            if (joinMsg.equalsIgnoreCase("none")) {
                e.setJoinMessage(null);
            } else {
                // Cukup panggil ini, simpel banget kan?
                e.setJoinMessage(ChatUtils.formatMessage(p, joinMsg));
            }
        }

        // C. MOTD
        for (String line : ConfigUtils.getStringList("messages.motd")) {
            p.sendMessage(ChatUtils.formatMessage(p, line));
        }
    }

    // 3. QUIT MESSAGE
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        String quitMsg = ConfigUtils.getString("messages.quit-message");
        if (quitMsg.equalsIgnoreCase("none")) {
            e.setQuitMessage(null);
        } else {
            e.setQuitMessage(ChatUtils.formatMessage(e.getPlayer(), quitMsg));
        }
    }
}