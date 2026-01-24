package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final NaturalCore plugin;

    public PlayerDeathListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        // 1. Save Location for /back
        plugin.getTeleportManager().setLastLocation(p);
        plugin.getTeleportManager().setLastDeathLocation(p);

        // 2. Interactive Death Message
        // We do not cancel the default death message, but we send our own info.
        // e.setDeathMessage(null); // Uncomment if user wants to hide vanilla message

        p.sendMessage("");
        p.sendMessage(ChatUtils.colorize(ConfigUtils.getString("messages.death.title")));
        p.sendMessage(ChatUtils.colorize(ConfigUtils.getString("messages.death.location")
                .replace("%x%", String.valueOf(p.getLocation().getBlockX()))
                .replace("%y%", String.valueOf(p.getLocation().getBlockY()))
                .replace("%z%", String.valueOf(p.getLocation().getBlockZ()))));
        p.sendMessage(ChatUtils
                .colorize(ConfigUtils.getString("messages.death.world").replace("%world%", p.getWorld().getName())));
        p.sendMessage("");

        TextComponent btn = new TextComponent(ChatUtils.colorize(ConfigUtils.getString("messages.death.back-button")));
        btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back"));
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatUtils.colorize(ConfigUtils.getString("messages.death.back-hover"))).create()));

        p.spigot().sendMessage(btn);
        p.sendMessage("");

        try {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        } catch (Exception ignored) {
        }
    }
}
