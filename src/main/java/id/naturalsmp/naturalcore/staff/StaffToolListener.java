package id.naturalsmp.naturalcore.staff;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StaffToolListener implements Listener {

    private final NaturalCore plugin;
    private final Random random = new Random();

    public StaffToolListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onToolUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffManager().isInStaffMode(player))
            return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR)
            return;

        Action action = event.getAction();
        Material type = item.getType();

        if (type == Material.COMPASS && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
            event.setCancelled(true);
            Block target = player.getTargetBlockExact(100);
            if (target != null) {
                player.teleport(target.getLocation().add(0, 1, 0).setDirection(player.getLocation().getDirection()));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
        } else if (type == Material.CLOCK && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            teleportToRandomPlayer(player);
        } else if (type == Material.BARRIER
                && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            player.performCommand("staff");
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffManager().isInStaffMode(player))
            return;

        if (!(event.getRightClicked() instanceof Player target))
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.BOOK) {
            event.setCancelled(true);
            player.performCommand("invsee " + target.getName());
        }
    }

    private void teleportToRandomPlayer(Player staff) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.remove(staff);

        if (online.isEmpty()) {
            staff.sendMessage(ChatUtils.colorize("&cTidak ada player lain online."));
            return;
        }

        Collections.shuffle(online);
        Player target = online.get(0);
        staff.teleport(target.getLocation());
        staff.sendMessage(ChatUtils.colorize("&6&lStaff &8» &7Random TP ke &e" + target.getName()));
        staff.playSound(staff.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
    }
}
