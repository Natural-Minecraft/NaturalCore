package id.naturalsmp.naturalcore.combat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.List;

public class CombatListener implements Listener {

    private final NaturalCore plugin;
    private final List<String> blockedCommands = Arrays.asList(
            "tp", "tpa", "spawn", "home", "back", "ec", "enderchest", "feed", "heal", "fly", "trade");

    public CombatListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player victim))
            return;
        if (damager.equals(victim))
            return;

        plugin.getCombatManager().tagPlayer(damager);
        plugin.getCombatManager().tagPlayer(victim);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getCombatManager().isInCombat(player))
            return;
        if (player.hasPermission("naturalsmp.admin"))
            return;

        String cmd = event.getMessage().split(" ")[0].replace("/", "").toLowerCase();
        if (blockedCommands.contains(cmd)) {
            event.setCancelled(true);
            player.sendMessage(ChatUtils.colorize("&6&lNaturalPVP &8» &cPerintah ini dilarang saat bertempur!"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCombatManager().isInCombat(player)) {
            player.setHealth(0); // Kill the player
            plugin.getServer().broadcastMessage(ChatUtils.colorize(
                    "&6&lNaturalPVP &8» &e" + player.getName() + " &cmencoba kabur saat bertempur dan tewas!"));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Combat manager cleanup will handle this naturally via timer or we can force
        // it
    }
}
