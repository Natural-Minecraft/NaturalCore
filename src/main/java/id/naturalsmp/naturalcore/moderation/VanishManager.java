package id.naturalsmp.naturalcore.moderation;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager implements Listener {

    private final NaturalCore plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(Player p) {
        return vanishedPlayers.contains(p.getUniqueId());
    }

    public void setVanished(Player p, boolean state) {
        if (state) {
            // AKTIFKAN VANISH
            vanishedPlayers.add(p.getUniqueId());

            // 1. Hide from others
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("naturalsmp.vanish.see")) {
                    online.hidePlayer(plugin, p);
                }
            }

            // 2. Fake Quit Message
            String quitMsg = ConfigUtils.getString("social.quit-message");
            GUIUtils.broadcast(ChatUtils.formatMessage(p, quitMsg));

            // 3. Status Effects
            p.setAllowFlight(true);
            p.setSleepingIgnored(true);

            p.sendMessage(ConfigUtils.getString("prefix.moderation")
                    + ConfigUtils.getString("messages.moderation.vanish-enabled"));
            p.sendTitle("", ChatUtils.colorize("&b&lVANISHED"), 0, 40, 10);

            plugin.getLogger().info(p.getName() + " is now VANISHED 👻");
        } else {
            // MATIKAN VANISH
            vanishedPlayers.remove(p.getUniqueId());

            // 1. Show to others
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, p);
            }

            // 2. Fake Join Message
            String joinMsg = ConfigUtils.getString("social.join-message");
            GUIUtils.broadcast(ChatUtils.formatMessage(p, joinMsg));

            // 3. Reset
            if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                // Keep flight if they have fly permission, else disable
                if (!p.hasPermission("naturalsmp.fly")) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }
            }
            p.setSleepingIgnored(false);

            p.sendMessage(ConfigUtils.getString("prefix.moderation")
                    + ConfigUtils.getString("messages.moderation.vanish-disabled"));

            plugin.getLogger().info(p.getName() + " is now UN-VANISHED ✨");
        }
    }

    // Dipanggil saat ada player baru join
    public void hideVanishedFrom(Player newPlayer) {
        // Jika player baru ini bukan admin, sembunyikan semua player yang sedang vanish
        // dari dia
        if (!newPlayer.hasPermission("naturalsmp.vanish.see")) {
            for (UUID uuid : vanishedPlayers) {
                Player vanishedPlayer = Bukkit.getPlayer(uuid);
                if (vanishedPlayer != null) {
                    newPlayer.hidePlayer(plugin, vanishedPlayer);
                }
            }
        }
    }
}