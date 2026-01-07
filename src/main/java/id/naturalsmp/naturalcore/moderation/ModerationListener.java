package id.naturalsmp.naturalcore.moderation;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ModerationListener implements Listener {

    private final NaturalCore plugin;

    public ModerationListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    // 1. CEK BAN SAAT LOGIN
    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        if (plugin.getPunishmentManager().isBanned(uuid)) {
            String reason = plugin.getPunishmentManager().getBanReason(uuid);
            String msg = ConfigUtils.getString("moderation.ban-message")
                    .replace("%reason%", reason)
                    .replace("%time%", "Permanen/Sisa Waktu");
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatUtils.colorize(msg));
        }
    }

    // 2. CEK MUTE SAAT CHAT
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (plugin.getPunishmentManager().isMuted(uuid)) {
            e.setCancelled(true);
            long expiry = plugin.getPunishmentManager().getMuteExpiry(uuid);
            long left = (expiry - System.currentTimeMillis()) / 1000;
            String msg = ConfigUtils.getString("moderation.mute-message").replace("%time%", left + " detik");
            e.getPlayer().sendMessage(ChatUtils.colorize(msg));
        }
    }

    // 3. GOD MODE & VANISH
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (GodVanishCommand.godPlayers.contains(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Hide vanished players from new joiner
        for (UUID uuid : GodVanishCommand.vanishPlayers) {
            Player vanished = plugin.getServer().getPlayer(uuid);
            if (vanished != null) e.getPlayer().hidePlayer(plugin, vanished);
        }
    }
}