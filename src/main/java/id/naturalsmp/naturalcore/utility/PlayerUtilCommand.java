package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerUtilCommand implements CommandExecutor {

    // Cooldown maps: UUID -> expiry timestamp (ms)
    private static final Map<UUID, Long> healCooldowns = new HashMap<>();
    private static final Map<UUID, Long> feedCooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        String cmdName = label.toLowerCase();

        Player target = (sender instanceof Player) ? (Player) sender : null;

        // Cek argumen jika admin ingin heal orang lain
        if (args.length > 0) {
            // Cek permission dulu sebelum memproses argumen
            String permNode = "naturalsmp."
                    + (cmdName.equals("fly") ? "fly" : cmdName.equals("heal") ? "heal" : "feed");
            if (sender.hasPermission(permNode + ".others")) {
                Player t = Bukkit.getPlayer(args[0]);
                if (t != null)
                    target = t;
            }
        }

        if (target == null) {
            if (args.length > 0) {
                ConfigUtils.sendError(sender,
                        ConfigUtils.getString("messages.global.player-not-found", "Player not found")
                                .replace("%player%", args[0]));
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("Console must specify player");
                return true;
            }

            // Jika tidak ada argumen, baru fallback ke sender
            target = (Player) sender;
        }

        String prefix = ConfigUtils.getString("prefix.admin");

        // --- HEAL ---
        if (cmdName.equals("heal")) {
            if (!sender.hasPermission("naturalsmp.heal"))
                return noPerm(sender);

            // Cooldown check (only for self-use, not admin-targeting-others)
            if (sender instanceof Player p && target.equals(p)) {
                long remaining = checkCooldown(p, healCooldowns);
                if (remaining > 0) {
                    String timeStr = formatCooldown(remaining);
                    p.sendMessage(ConfigUtils.getString("messages.global.no-permission")
                            .isEmpty() ? "" : "");
                    ConfigUtils.sendGeneral(p, "messages.utils.essentials.cooldown",
                            "%time%", timeStr);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    return true;
                }
                applyCooldown(p, healCooldowns);
            }

            // FIX HEAL LOGIC
            double maxHealth = 20.0;
            if (target.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            }
            target.setHealth(maxHealth);
            target.setFoodLevel(20);
            target.setSaturation(20);
            target.setFireTicks(0);

            ConfigUtils.sendGeneral(target, "messages.utils.essentials.heal-success");
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            if (!target.equals(sender)) {
                ConfigUtils.sendGeneral(sender, "messages.utils.essentials.heal-other", "%player%", target.getName());
            }
            return true;
        }

        // --- FEED ---
        if (cmdName.equals("feed")) {
            if (!sender.hasPermission("naturalsmp.feed"))
                return noPerm(sender);

            // Cooldown check (only for self-use)
            if (sender instanceof Player p && target.equals(p)) {
                long remaining = checkCooldown(p, feedCooldowns);
                if (remaining > 0) {
                    String timeStr = formatCooldown(remaining);
                    ConfigUtils.sendGeneral(p, "messages.utils.essentials.cooldown",
                            "%time%", timeStr);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    return true;
                }
                applyCooldown(p, feedCooldowns);
            }

            target.setFoodLevel(20);
            target.setSaturation(20);

            ConfigUtils.sendGeneral(target, "messages.utils.essentials.feed-success");
            target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 1f);

            if (!target.equals(sender)) {
                ConfigUtils.sendGeneral(sender, "messages.utils.essentials.feed-other", "%player%", target.getName());
            }
            return true;
        }

        // --- FLY ---
        if (cmdName.equals("fly")) {
            if (!sender.hasPermission("naturalsmp.fly"))
                return noPerm(sender);

            boolean newStatus = !target.getAllowFlight();
            target.setAllowFlight(newStatus);

            String msgPath = newStatus ? "messages.utils.essentials.fly-enabled"
                    : "messages.utils.essentials.fly-disabled";
            ConfigUtils.sendGeneral(target, msgPath);

            if (!target.equals(sender)) {
                ConfigUtils.sendGeneral(sender, "messages.utils.essentials.fly-other", "%player%", target.getName(),
                        "%status%", newStatus ? "enabled" : "disabled");
            }
            return true;
        }

        return true;
    }

    /**
     * Get the cooldown duration in milliseconds for a player based on their rank.
     * Hierarchy (highest permission wins):
     * - naturalsmp.cooldown.bypass / naturalsmp.admin → 0 (no cooldown)
     * - naturalsmp.heal.cd.nature → 0 (no cooldown)
     * - naturalsmp.heal.cd.gold → 30 seconds
     * - naturalsmp.heal.cd.mvp → 1 minute
     * - naturalsmp.heal.cd.vip → 3 minutes
     * - Default (member with perm): 5 minutes
     */
    private long getCooldownMs(Player p) {
        if (p.hasPermission("naturalsmp.cooldown.bypass") || p.hasPermission("naturalsmp.admin")) {
            return 0; // Admin / Nature+ bypass
        }
        if (p.hasPermission("naturalsmp.heal.cd.nature")) {
            return 0; // Nature rank: no cooldown
        }
        if (p.hasPermission("naturalsmp.heal.cd.gold")) {
            return 30_000L; // Gold: 30 seconds
        }
        if (p.hasPermission("naturalsmp.heal.cd.mvp")) {
            return 60_000L; // MVP: 1 minute
        }
        if (p.hasPermission("naturalsmp.heal.cd.vip")) {
            return 180_000L; // VIP: 3 minutes
        }
        return 300_000L; // Default: 5 minutes
    }

    /**
     * Check remaining cooldown in milliseconds. Returns 0 if no cooldown active.
     */
    private long checkCooldown(Player p, Map<UUID, Long> cooldownMap) {
        long cooldownMs = getCooldownMs(p);
        if (cooldownMs == 0) return 0; // No cooldown for this rank

        Long expiry = cooldownMap.get(p.getUniqueId());
        if (expiry == null) return 0;

        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Apply cooldown to a player.
     */
    private void applyCooldown(Player p, Map<UUID, Long> cooldownMap) {
        long cooldownMs = getCooldownMs(p);
        if (cooldownMs <= 0) return;

        cooldownMap.put(p.getUniqueId(), System.currentTimeMillis() + cooldownMs);
    }

    /**
     * Format milliseconds into a human-readable string like "2m 30s" or "45s".
     */
    private String formatCooldown(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    private boolean noPerm(CommandSender s) {
        s.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
        return true;
    }
}