package id.naturalsmp.naturalcore.moderation;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

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
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("naturalsmp.vanish.see")) {
                    online.hidePlayer(plugin, p);
                }
            }
            p.sendMessage(ConfigUtils.getString("prefix.moderation") + ConfigUtils.getString("messages.vanish-enabled"));
            p.sendTitle("", ChatUtils.colorize("&b&lVANISHED"), 0, 40, 10);
        } else {
            // MATIKAN VANISH
            vanishedPlayers.remove(p.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, p);
            }
            p.sendMessage(ConfigUtils.getString("prefix.moderation") + ConfigUtils.getString("messages.vanish-disabled"));
        }
    }

    // Dipanggil saat ada player baru join
    public void hideVanishedFrom(Player newPlayer) {
        // Jika player baru ini bukan admin, sembunyikan semua player yang sedang vanish dari dia
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