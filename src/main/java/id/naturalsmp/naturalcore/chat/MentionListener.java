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
import org.bukkit.Particle;
import org.bukkit.Color;

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
            // Match @name or just name (but @name is safer and cooler)
            if (message.toLowerCase().contains("@" + name.toLowerCase())
                    || message.toLowerCase().contains(name.toLowerCase())) {

                // 1. Highlight nama di pesan (Gunakan @ jika belum ada)
                String color = id.naturalsmp.naturalcore.utils.ConfigUtils.getString("chat.mention.color", "&b&l@");
                if (message.toLowerCase().contains("@" + name.toLowerCase())) {
                    message = message.replaceAll("(?i)@" + name, ChatUtils.colorize(color + name + "&f"));
                } else {
                    message = message.replaceAll("(?i)" + name, ChatUtils.colorize(color + name + "&f"));
                }
                mentioned = true;

                // 2. Play Sound & Woah Titles
                Bukkit.getScheduler().runTask(plugin, () -> {
                    boolean isAdmin = sender.hasPermission("natural.admin") || sender.isOp();

                    if (isAdmin) {
                        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.5f, 2.0f);
                        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        target.sendTitle(ChatUtils.colorize("&#FF0000&l⚡ PANGGILAN ADMIN &7(" + sender.getName() + ")"),
                                ChatUtils.colorize("&ePerhatian! Admin menyebut nama Anda."), 5, 60, 15);
                    } else {
                        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 0.5f);
                        target.sendTitle(ChatUtils.colorize("&#00FBFF&l⚠ DI-TAG OLEH &f" + sender.getName()),
                                ChatUtils.colorize("&7Cek chat untuk melihat pesan!"), 5, 40, 10);
                    }

                    // AFK Particle Effect (If target is AFK)
                    if (plugin.getAFKManager() != null && plugin.getAFKManager().isAFK(target)) {
                        target.getWorld().spawnParticle(Particle.DUST, target.getEyeLocation().add(0, 0.5, 0), 20,
                                0.3, 0.3, 0.3, new Particle.DustOptions(Color.AQUA, 1.5f));
                    }
                });
            }
        }

        if (mentioned) {
            e.setMessage(message);
        }
    }
}
