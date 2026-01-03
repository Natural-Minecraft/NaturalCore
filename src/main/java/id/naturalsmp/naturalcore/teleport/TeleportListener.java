package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class TeleportListener implements Listener {

    private final TeleportManager teleportManager;
    
    public TeleportListener(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }
        
        if (!player.hasPermission("naturalcore.back")) {
            return;
        }
        
        Location deathLoc = player.getLocation();
        if (deathLoc == null || deathLoc.getWorld() == null) {
            return;
        }
        
        teleportManager.setBackLocation(player, deathLoc);
        
        // Send death message with clickable /back button
        player.sendMessage("");
        player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &c&lKAMU MATI!"));
        player.sendMessage(ChatUtils.color("  &7Lokasi: &f" + (int)deathLoc.getX() + ", " + (int)deathLoc.getY() + ", " + (int)deathLoc.getZ()));
        player.sendMessage(ChatUtils.color("  &7Dunia: &f" + deathLoc.getWorld().getName()));
        player.sendMessage("");
        
        // Clickable /back button
        TextComponent backButton = new TextComponent(ChatUtils.color("&6&l[ ⚡ KEMBALI KE LOKASI ⚡ ]"));
        backButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back"));
        backButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(ChatUtils.color("&eKlik untuk kembali ke lokasi kematian")).create()));
        
        TextComponent prefix = new TextComponent("     ");
        prefix.addExtra(backButton);
        
        player.spigot().sendMessage(prefix);
        player.sendMessage("");
        
        player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
    }
}
