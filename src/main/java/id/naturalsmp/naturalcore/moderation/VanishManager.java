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
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        if (plugin.getCoreDatabase() != null && plugin.getCoreDatabase().isEnabled()) {
            Set<UUID> dbVanished = plugin.getCoreDatabase().getVanishedPlayers();
            vanishedPlayers.addAll(dbVanished);
            plugin.getLogger().info("[Vanish] Loaded " + dbVanished.size() + " vanished players from database.");
        }
    }

    public boolean isVanished(Player p) {
        return vanishedPlayers.contains(p.getUniqueId());
    }

    public void setVanished(Player p, boolean state) {
        if (state) {
            // AKTIFKAN VANISH
            vanishedPlayers.add(p.getUniqueId());
            if (plugin.getCoreDatabase() != null && plugin.getCoreDatabase().isEnabled()) {
                plugin.getCoreDatabase().addVanished(p.getUniqueId());
            }

            // 1. Hide from others
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("naturalsmp.vanish.see") && !online.equals(p)) {
                    online.hidePlayer(plugin, p);
                }
            }

            // 2. Fake Quit Message
            String quitMsg = ConfigUtils.getString("messages.social.quit-message");
            if (quitMsg != null && !quitMsg.isEmpty()) {
                GUIUtils.broadcast(ChatUtils.formatMessage(p, quitMsg));
            }

            // 3. Status Effects
            p.setAllowFlight(true);
            p.setSleepingIgnored(true);

            // Tab List Hiding (SuperVanish Style)
            p.setPlayerListName(""); // Empty name in tab
            // Hide from everyone in tab list
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("naturalsmp.vanish.see") && !online.equals(p)) {
                    online.hidePlayer(plugin, p);
                }
            }

            ConfigUtils.sendMod(p, "messages.moderation.vanish-enabled");
            p.sendTitle("", ChatUtils.colorize("&b&lᴠᴀɴɪsʜᴇᴅ"), 0, 40, 10);

            plugin.getLogger().info(p.getName() + " is now VANISHED 👻");
        } else {
            // MATIKAN VANISH
            vanishedPlayers.remove(p.getUniqueId());
            if (plugin.getCoreDatabase() != null && plugin.getCoreDatabase().isEnabled()) {
                plugin.getCoreDatabase().removeVanished(p.getUniqueId());
            }

            // 1. Show to others
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, p);
            }

            // 2. Fake Join Message
            String joinMsg = ConfigUtils.getString("messages.social.join-message");
            if (joinMsg != null && !joinMsg.isEmpty()) {
                GUIUtils.broadcast(ChatUtils.formatMessage(p, joinMsg));
            }

            // 3. Reset
            p.setPlayerListName(p.getName());
            if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                // Keep flight if they have fly permission, else disable
                if (!p.hasPermission("naturalsmp.fly")) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }
            }
            p.setSleepingIgnored(false);

            ConfigUtils.sendMod(p, "messages.moderation.vanish-disabled");

            plugin.getLogger().info(p.getName() + " is now UN-VANISHED ✨");
        }
    }

    // --- GHOST FEATURES (Event Handlers) ---

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (isVanished(p)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (isVanished(e.getPlayer())) {
            // Prevent treading on pressure plates / physical interaction
            if (e.getAction() == Action.PHYSICAL) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player p) {
            if (isVanished(p)) {
                e.setTarget(null);
                e.setCancelled(true);
            }
        }
    }

    // --- SILENT CHESTS ---
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onInventoryOpen(org.bukkit.event.inventory.InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player p) {
            if (isVanished(p)) {
                org.bukkit.inventory.InventoryHolder holder = e.getInventory().getHolder();
                if (holder instanceof org.bukkit.block.Chest || holder instanceof org.bukkit.block.EnderChest
                        || holder instanceof org.bukkit.block.ShulkerBox || holder instanceof org.bukkit.block.Barrel) {
                    // Silently open for vanished players
                    // We can't easily cancel the sound without NMS/ProtocolLib in all versions,
                    // but we can at least avoid the animation if we use a virtual view
                    // However, standard behavior for "Silent Chests" is often handled via packets.
                    // For now, we can at least ensure they don't trigger events for other plugins.
                }
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onChestInterat(PlayerInteractEvent e) {
        if (isVanished(e.getPlayer()) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            org.bukkit.block.Block block = e.getClickedBlock();
            if (block != null && (block.getType() == org.bukkit.Material.CHEST
                    || block.getType() == org.bukkit.Material.TRAPPED_CHEST
                    || block.getType() == org.bukkit.Material.BARREL
                    || block.getType() == org.bukkit.Material.SHULKER_BOX)) {

                // SuperVanish style silent chest: open a virtual inventory instead of the real
                // one
                // to prevent animation.
                if (e.getPlayer().isSneaking()) {
                    // Allow normal interaction if sneaking? Or maybe always silent?
                    return;
                }

                e.setCancelled(true);
                org.bukkit.block.BlockState state = block.getState();
                if (state instanceof org.bukkit.inventory.InventoryHolder holder) {
                    e.getPlayer().openInventory(holder.getInventory());
                    e.getPlayer().sendMessage(ChatUtils.colorize("&6&lStaff &8» &7Opening container &asilently&7."));
                }
            }
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