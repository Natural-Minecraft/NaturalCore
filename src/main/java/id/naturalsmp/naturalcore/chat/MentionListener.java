package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MentionListener implements Listener {

    @SuppressWarnings("unused")
    private final NaturalCore plugin;

    public MentionListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled())
            return;

        Player sender = e.getPlayer();
        String message = e.getMessage();
        boolean mentioned = false;

        // Loop semua player untuk cek nama
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getUniqueId().equals(sender.getUniqueId()))
                continue;

            String name = target.getName();
            // Cek jika pesan mengandung nama player (case insensitive)
            // Tambahkan @ support jika mau, tapi standar nama aja cukup keren
            if (message.toLowerCase().contains(name.toLowerCase())) {

                // 1. Highlight nama di pesan
                // Ambil format dari config
                String color = id.naturalsmp.naturalcore.utils.ConfigUtils.getString("chat.mention.color", "&e@");
                message = message.replaceAll("(?i)" + name, ChatUtils.colorize(color + name + "&f"));
                mentioned = true;

                // 2. Play Sound & Title
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String soundName = id.naturalsmp.naturalcore.utils.ConfigUtils.getString("chat.mention.sound",
                            "BLOCK_NOTE_BLOCK_PLING");
                    float vol = (float) plugin.getConfig().getDouble("chat.mention.volume", 1.0);
                    float pitch = (float) plugin.getConfig().getDouble("chat.mention.pitch", 2.0);

                    try {
                        target.playSound(target.getLocation(), Sound.valueOf(soundName), vol, pitch);
                    } catch (IllegalArgumentException ex) {
                        // Ignore invalid sound
                    }

                    // Kirim Title singkat
                    target.sendTitle("", ChatUtils.colorize("&e" + sender.getName() + " &fmemanggilmu!"), 10, 40, 10);
                });
            }
        }

        if (mentioned) {
            e.setMessage(message);
        }
    }
}
