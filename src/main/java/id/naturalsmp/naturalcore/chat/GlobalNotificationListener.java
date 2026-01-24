package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.Random;

public class GlobalNotificationListener implements Listener {

    private final NaturalCore plugin;
    private final Random random = new Random();

    public GlobalNotificationListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        // Disable default message
        e.setDeathMessage(null);

        String message;
        if (killer != null && killer != victim) {
            // Kill Message
            List<String> variants = ConfigUtils.getStringList("notifications.kill");
            if (variants.isEmpty())
                return;
            message = variants.get(random.nextInt(variants.size()))
                    .replace("%killer%", killer.getName())
                    .replace("%victim%", victim.getName());

            killer.playSound(killer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
        } else {
            // Death Message
            List<String> variants = ConfigUtils.getStringList("notifications.death");
            if (variants.isEmpty())
                return;
            message = variants.get(random.nextInt(variants.size()))
                    .replace("%player%", victim.getName());
        }

        Bukkit.broadcastMessage(ChatUtils.colorize(message));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        // Only broadcast if it's a visible advancement (has display info)
        if (e.getAdvancement().getDisplay() == null)
            return;

        String title = LegacyComponentSerializer.legacyAmpersand().serialize(e.getAdvancement().getDisplay().title());
        // Hide default advancement message if possible (usually via gamerule, but we
        // can't easily cancel the system broadcast here without a custom advancement
        // system or packet manipulation)
        // However, we can send our aesthetic one.

        Player p = e.getPlayer();
        List<String> variants = ConfigUtils.getStringList("notifications.achievement");
        if (variants.isEmpty())
            return;

        String message = variants.get(random.nextInt(variants.size()))
                .replace("%player%", p.getName())
                .replace("%achievement%", title);

        Bukkit.broadcastMessage(ChatUtils.colorize(message));

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(online.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.5f);
        }
    }
}
