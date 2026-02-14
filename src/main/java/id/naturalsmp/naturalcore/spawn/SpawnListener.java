package id.naturalsmp.naturalcore.spawn;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;

public class SpawnListener implements Listener {

    private final NaturalCore plugin;

    public SpawnListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String currentWorld = p.getWorld().getName();

        // 1. FIRST JOIN HANDLING (Priority)
        if (!e.getPlayer().hasPlayedBefore()) {
            // First Join Kit
            List<String> kitItems = plugin.getConfig().getStringList("first-join-kit.items");
            if (kitItems.isEmpty()) {
                // Fallback Default
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.WOODEN_SWORD));
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.WOODEN_PICKAXE));
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.WOODEN_AXE));
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.WOODEN_SHOVEL));
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.COOKED_BEEF, 16));
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_SHOVEL)); // Claim
            } else {
                for (String itemStr : kitItems) {
                    try {
                        String[] parts = itemStr.split(":");
                        org.bukkit.Material mat = org.bukkit.Material.valueOf(parts[0]);
                        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                        p.getInventory().addItem(new org.bukkit.inventory.ItemStack(mat, amount));
                    } catch (Exception ignored) {
                    }
                }
            }

            // Armor
            String helmet = plugin.getConfig().getString("first-join-kit.armor.helmet", "LEATHER_HELMET");
            String chest = plugin.getConfig().getString("first-join-kit.armor.chestplate", "LEATHER_CHESTPLATE");
            String legs = plugin.getConfig().getString("first-join-kit.armor.leggings", "LEATHER_LEGGINGS");
            String boots = plugin.getConfig().getString("first-join-kit.armor.boots", "LEATHER_BOOTS");

            try {
                if (!helmet.equals("AIR"))
                    p.getInventory()
                            .setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(helmet)));
                if (!chest.equals("AIR"))
                    p.getInventory()
                            .setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(chest)));
                if (!legs.equals("AIR"))
                    p.getInventory()
                            .setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(legs)));
                if (!boots.equals("AIR"))
                    p.getInventory()
                            .setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(boots)));
            } catch (Exception ignored) {
            }

            p.sendMessage(ChatUtils.colorize("&aWelcome to NaturalSMP! You received a starter kit."));

            // Teleport to spawn for first join
            plugin.getSpawnManager().teleport(p);

            p.setAllowFlight(false);
            p.setFlying(false);
            return; // Done with first join
        }

        // 2. EXISTING PLAYER JOIN HANDLING
        List<String> allowed = ConfigUtils.getStringList("spawn.allowed-join-worlds");

        // Allow vanilla worlds implicitly for existing players
        if (currentWorld.equals("world") || currentWorld.equals("world_nether")
                || currentWorld.equals("world_the_end")) {
            return;
        }

        // If in an non-allowed world, force to spawn
        if (!allowed.contains(currentWorld)) {
            plugin.getSpawnManager().teleport(p);
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        // Jika player tidak punya bed spawn atau anchor spawn
        if (!e.isBedSpawn() && !e.isAnchorSpawn()) {
            org.bukkit.Location spawn = plugin.getSpawnManager().getSpawn();
            if (spawn != null) {
                e.setRespawnLocation(spawn);
            }
        }
    }
}
