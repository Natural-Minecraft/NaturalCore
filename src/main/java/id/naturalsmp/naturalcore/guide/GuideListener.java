package id.naturalsmp.naturalcore.guide;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class GuideListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        
        // Kirim welcome message dengan panduan singkat
        if (!player.hasPlayedBefore()) {
            player.sendMessage("");
            player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            player.sendMessage(ChatUtils.color("&e&lWelcome to NaturalSMP!"));
            player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            player.sendMessage("");
            player.sendMessage(ChatUtils.color("&7Halo &f" + player.getName() + "&7!"));
            player.sendMessage(ChatUtils.color("&7Selamat datang di server kami!"));
            player.sendMessage("");
            player.sendMessage(ChatUtils.color("&e&lGunakan command:"));
            player.sendMessage(ChatUtils.color("&7• &f/guide &7- Untuk melihat panduan"));
            player.sendMessage(ChatUtils.color("&7• &f/reforge &7- Upgrade item kamu"));
            player.sendMessage(ChatUtils.color("&7• &f/trader &7- Akses travelling trader"));
            player.sendMessage("");
            player.sendMessage(ChatUtils.color("&aSelamat bermain! &e✨"));
            player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            player.sendMessage("");
        }
    }
}
