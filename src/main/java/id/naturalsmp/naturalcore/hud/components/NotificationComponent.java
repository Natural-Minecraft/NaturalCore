package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Displays intercepted action bar notifications from other plugins (MMOItems,
 * MythicLib)
 * with premium NaturalCore aesthetics.
 *
 * Types of notifications:
 * - COOLDOWN: Progress bar + countdown (like ClearLag timer)
 * - MANA_WARNING: Blinking red mana text
 * - STAMINA_WARNING: Blinking red stamina text
 * - GENERIC: Generic notification with fade
 */
public class NotificationComponent extends AbstractHUDComponent {

    private final Map<UUID, Notification> activeNotifications = new HashMap<>();

    // Tracks when a cooldown for a specific skill was first sent to a player
    // playerUUID -> (skillName -> firstSeenTimeMs)
    private final Map<UUID, Map<String, Long>> cooldownDisplayStartTimes = new HashMap<>();

    // Tracks the last time a player interacted (clicked), to show cooldown again
    private final Map<UUID, Long> lastInteractTimes = new ConcurrentHashMap<>();

    public NotificationComponent(NaturalCore plugin) {
        super(plugin, "notification", HUDPriority.HIGHEST);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        Notification notification = activeNotifications.get(player.getUniqueId());
        if (notification == null)
            return false;

        if (System.currentTimeMillis() > notification.expiryTime) {
            activeNotifications.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    @Override
    public String getContent(Player player, int globalTick) {
        Notification notification = activeNotifications.get(player.getUniqueId());
        if (notification == null)
            return null;

        return switch (notification.type) {
            case COOLDOWN -> renderCooldown(notification, globalTick);
            case MANA_WARNING -> renderManaWarning(notification, globalTick);
            case STAMINA_WARNING -> renderStaminaWarning(notification, globalTick);
            case TWO_HANDED -> renderTwoHanded(notification, globalTick);
            case CANT_USE -> renderCantUse(notification, globalTick);
            default -> renderGeneric(notification, globalTick);
        };
    }

    // =========================================
    // COOLDOWN — ClearLag-style progress bar
    // =========================================
    private String renderCooldown(Notification n, int tick) {
        // Calculate time remaining
        long remaining = n.expiryTime - System.currentTimeMillis();
        double secondsLeft = Math.max(0, remaining / 1000.0);

        // Progress: 0.0 (just started) -> 1.0 (done)
        double totalDuration = n.totalDurationMs;
        double progress = 1.0 - (remaining / totalDuration);
        progress = Math.max(0, Math.min(1, progress));

        // Build progress bar (15 blocks)
        String bar = buildProgressBar(progress, 15, tick);

        // Urgency icon
        String icon;
        String timeColor;
        if (secondsLeft <= 1.0) {
            icon = (tick % 4 < 2) ? "&cⓘ" : "&fⓘ"; // Pulse
            timeColor = "&a&l";
        } else if (secondsLeft <= 3.0) {
            icon = "&e⏳";
            timeColor = "&e";
        } else {
            icon = "&7⏳";
            timeColor = "&7";
        }

        String skillName = n.extraData != null ? n.extraData : "Skill";

        return icon + " &8┃ &7" + skillName + " " + bar + " &8┃ "
                + timeColor + String.format("%.1fs", secondsLeft);
    }

    private String buildProgressBar(double progress, int length, int tick) {
        int filled = (int) (progress * length);
        int empty = length - filled;

        StringBuilder bar = new StringBuilder();

        // Color based on progress
        String fillColor;
        if (progress > 0.8)
            fillColor = "&#55FF55"; // Green (almost ready)
        else if (progress > 0.5)
            fillColor = "&#FFFF55"; // Yellow
        else if (progress > 0.25)
            fillColor = "&#FFAA00"; // Orange
        else
            fillColor = "&#FF5555"; // Red (just started)

        // Animated leading edge
        for (int i = 0; i < filled; i++) {
            if (i == filled - 1 && tick % 4 < 2) {
                bar.append("&f■"); // Pulse the leading edge
            } else {
                bar.append(fillColor).append("■");
            }
        }
        for (int i = 0; i < empty; i++) {
            bar.append("&8□");
        }

        return bar.toString();
    }

    // =========================================
    // MANA WARNING — Blinking red mana text
    // =========================================
    private String renderManaWarning(Notification n, int tick) {
        // Fast blink cycle: 6 ticks per phase (3 ticks ON, 3 ticks OFF)
        boolean blinkOn = (tick % 6) < 3;

        String manaIcon = blinkOn ? "&#5555FF✦" : "&#FF5555✦";
        String manaText = blinkOn ? "&#FF5555&lMana" : "&#AA0000&lMana";
        String msgColor = blinkOn ? "&c" : "&4";

        return manaIcon + " &8┃ " + msgColor + "Insufficient " + manaText
                + msgColor + "! " + "&8┃ " + manaIcon;
    }

    // =========================================
    // STAMINA WARNING — Blinking orange text
    // =========================================
    private String renderStaminaWarning(Notification n, int tick) {
        boolean blinkOn = (tick % 6) < 3;

        String icon = blinkOn ? "&#FFAA00⚡" : "&#FF5555⚡";
        String text = blinkOn ? "&#FFAA00&lStamina" : "&#AA5500&lStamina";
        String msgColor = blinkOn ? "&6" : "&c";

        return icon + " &8┃ " + msgColor + "Not Enough " + text
                + msgColor + "! " + "&8┃ " + icon;
    }

    // =========================================
    // TWO HANDED — Warning
    // =========================================
    private String renderTwoHanded(Notification n, int tick) {
        boolean pulse = (tick % 8) < 4;
        String icon = pulse ? "&#FFAA00⚠" : "&#FFFFFF⚠";

        return icon + " &8┃ &6Two-Handed &7weapon — &cdrop offhand! &8┃ " + icon;
    }

    // =========================================
    // CANT USE — Error warning
    // =========================================
    private String renderCantUse(Notification n, int tick) {
        boolean pulse = (tick % 6) < 3;
        String icon = pulse ? "&c✖" : "&4✖";
        String text = n.extraData != null ? n.extraData : "Cannot use this item";

        return icon + " &8┃ &c" + text + " &8┃ " + icon;
    }

    // =========================================
    // GENERIC — Simple styled message
    // =========================================
    private String renderGeneric(Notification n, int tick) {
        return "&8┃ &7" + n.rawMessage + " &8┃";
    }

    // =================================================================
    // PUBLIC API — Called by HUDPacketListener after intercepting
    // =================================================================

    /**
     * Show a notification with auto-detected type based on the intercepted text.
     */
    public void showNotification(Player player, String rawMessage, int ticks) {
        NotificationType type = detectType(rawMessage);
        long durationMs = ticks * 50L;
        long expiryTime = System.currentTimeMillis() + durationMs;

        Notification notification = new Notification(rawMessage, type, expiryTime, durationMs);

        // Extract extra data based on type
        switch (type) {
            case COOLDOWN -> {
                // Extract skill name
                notification.extraData = extractCooldownInfo(rawMessage);
                // Extract total cooldown duration
                double seconds = extractSeconds(rawMessage);
                if (seconds > 0) {
                    notification.totalDurationMs = seconds * 1000;
                }
            }
            case CANT_USE -> notification.extraData = rawMessage;
            default -> {
            }
        }

        // --- Custom Cooldown Display Limiter Logic ---
        if (type == NotificationType.COOLDOWN) {
            String skillName = notification.extraData != null ? notification.extraData : "Unknown";
            Map<String, Long> playerCooldowns = cooldownDisplayStartTimes.computeIfAbsent(player.getUniqueId(),
                    k -> new HashMap<>());

            long now = System.currentTimeMillis();
            long firstSeen = playerCooldowns.getOrDefault(skillName, 0L);

            // If we haven't seen this skill's cooldown recently (e.g. within the last 5
            // seconds)
            // It means this is a fresh cast or a fresh "attempt to cast"
            // Wait, since MMOItems sends the packet every tick, the packet gap is never >
            // 5s unless the cooldown finished.
            // BUT, if the player interacts (clicks), we update `lastInteractTime`.
            if (now - firstSeen > 10000L) {
                playerCooldowns.put(skillName, now);
                firstSeen = now;
            }

            long timeSinceFirstSeen = now - firstSeen;
            long lastInteract = lastInteractTimes.getOrDefault(player.getUniqueId(), 0L);

            // Logic:
            // 1. Show for the first 3 seconds of the initial cast
            // 2. OR, if they clicked/interacted recently (within the last 4 seconds), show
            // it for 4 seconds
            boolean showBecauseInitial = timeSinceFirstSeen <= 3000L;
            boolean showBecauseInteract = (now - lastInteract) <= 4000L;

            if (!showBecauseInitial && !showBecauseInteract) {
                // Time's up, don't show the cooldown anymore (it will return to default HUD)
                return;
            }
        }

        activeNotifications.put(player.getUniqueId(), notification);
    }

    /**
     * Directly show a cooldown notification with known duration.
     */
    public void showCooldown(Player player, String skillName, double totalSeconds) {
        long durationMs = (long) (totalSeconds * 1000);
        long expiryTime = System.currentTimeMillis() + durationMs;

        Notification notification = new Notification("", NotificationType.COOLDOWN, expiryTime, durationMs);
        notification.extraData = skillName;
        notification.totalDurationMs = durationMs;

        activeNotifications.put(player.getUniqueId(), notification);
    }

    // =================================================================
    // INTERACTION TRACKING
    // =================================================================

    /**
     * Called by HUDManager when the player clicks/interacts.
     * This registers an intent to see the cooldown again for 4 seconds if it's
     * active.
     */
    public void registerInteraction(Player player) {
        lastInteractTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // =================================================================
    // DETECTION & PARSING
    // =================================================================

    private NotificationType detectType(String text) {
        String lower = text.toLowerCase();

        // Cooldown patterns
        if (lower.contains("cooldown") || lower.contains("wait") && lower.contains("second")) {
            return NotificationType.COOLDOWN;
        }

        // Mana
        if (lower.contains("mana")) {
            return NotificationType.MANA_WARNING;
        }

        // Stamina
        if (lower.contains("stamina")) {
            return NotificationType.STAMINA_WARNING;
        }

        // Two-handed
        if (lower.contains("two-handed") || lower.contains("two handed")) {
            return NotificationType.TWO_HANDED;
        }

        // Can't use / permission / level
        if (lower.contains("can't use") || lower.contains("cannot use")
                || lower.contains("don't have enough level")
                || lower.contains("don't have enough perm")
                || lower.contains("wrong class")) {
            return NotificationType.CANT_USE;
        }

        return NotificationType.GENERIC;
    }

    private String extractCooldownInfo(String text) {
        // Try to extract skill name — MMOItems format: "... before casting this spell"
        // or "This item is on cooldown"
        if (text.toLowerCase().contains("spell") || text.toLowerCase().contains("casting")) {
            return "Spell";
        }
        if (text.toLowerCase().contains("item")) {
            return "Item";
        }
        return "Skill";
    }

    private double extractSeconds(String text) {
        // Extract number before "second" — e.g., "wait 3.5 seconds"
        try {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("(\\d+\\.?\\d*)\\s*second").matcher(text.toLowerCase());
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    // =================================================================
    // DATA CLASSES
    // =================================================================

    public enum NotificationType {
        COOLDOWN,
        MANA_WARNING,
        STAMINA_WARNING,
        TWO_HANDED,
        CANT_USE,
        GENERIC
    }

    private static class Notification {
        String rawMessage;
        NotificationType type;
        long expiryTime;
        double totalDurationMs;
        String extraData; // Skill name, item name, etc.

        Notification(String rawMessage, NotificationType type, long expiryTime, double totalDurationMs) {
            this.rawMessage = rawMessage;
            this.type = type;
            this.expiryTime = expiryTime;
            this.totalDurationMs = totalDurationMs;
        }
    }
}
