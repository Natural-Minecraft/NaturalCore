package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility for managing floating TextDisplay holograms attached to entities.
 * Used by NaturalLaggManager for merged mob stack indicators.
 */
public class HologramUtil {

    private static final String META_KEY = "natural_holo_uuid";
    private static NamespacedKey PDC_OWNER_KEY;
    private static NamespacedKey PDC_HOLO_TAG;
    private static NaturalCore plugin;

    public static void init(NaturalCore p) {
        plugin = p;
        PDC_OWNER_KEY = new NamespacedKey(p, "holo_owner");
        PDC_HOLO_TAG = new NamespacedKey(p, "natural_holo");
    }

    /**
     * Create or update the hologram for a target entity.
     *
     * @param target The entity to attach the hologram to
     * @param text   MiniMessage-formatted text to display
     */
    public static void updateHologram(Entity target, String text) {
        if (target == null || target.isDead() || !target.isValid())
            return;

        TextDisplay existing = getExistingHologram(target);

        if (existing != null && existing.isValid()) {
            // Update text
            existing.customName(ChatUtils.toComponent(text));
            // Teleport to follow the entity
            existing.teleport(getHologramLocation(target));
        } else {
            // Create new
            createHologram(target, text);
        }
    }

    /**
     * Create a new TextDisplay hologram above the target entity.
     */
    private static void createHologram(Entity target, String text) {
        Location loc = getHologramLocation(target);

        target.getWorld().spawn(loc, TextDisplay.class, display -> {
            display.customName(ChatUtils.toComponent(text));
            display.setCustomNameVisible(true);

            // Make it invisible (no block background)
            display.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setShadowed(false);
            display.setDefaultBackground(false);

            // Set text opacity to full
            display.text(ChatUtils.toComponent(text));

            // Store reference: target -> hologram
            display.getPersistentDataContainer().set(PDC_OWNER_KEY, PersistentDataType.STRING,
                    target.getUniqueId().toString());
            display.getPersistentDataContainer().set(PDC_HOLO_TAG, PersistentDataType.BYTE, (byte) 1);

            // Non-persistent: don't save to disk
            display.setPersistent(false);

            // Store hologram UUID on target via metadata
            target.setMetadata(META_KEY, new FixedMetadataValue(plugin, display.getUniqueId().toString()));
        });
    }

    /**
     * Remove the hologram attached to a target entity.
     */
    public static void removeHologram(Entity target) {
        if (target == null)
            return;

        TextDisplay existing = getExistingHologram(target);
        if (existing != null && existing.isValid()) {
            existing.remove();
        }
        target.removeMetadata(META_KEY, plugin);
    }

    /**
     * Teleport holograms to follow their target entities.
     * Called periodically by NaturalLaggManager.
     */
    public static void tickHolograms() {
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof TextDisplay td) {
                    if (!td.getPersistentDataContainer().has(PDC_HOLO_TAG, PersistentDataType.BYTE))
                        continue;

                    String ownerUuid = td.getPersistentDataContainer().get(PDC_OWNER_KEY, PersistentDataType.STRING);
                    if (ownerUuid == null) {
                        td.remove();
                        continue;
                    }

                    Entity owner = org.bukkit.Bukkit.getEntity(UUID.fromString(ownerUuid));
                    if (owner == null || owner.isDead() || !owner.isValid()) {
                        td.remove();
                        continue;
                    }

                    td.teleport(getHologramLocation(owner));
                }
            }
        }
    }

    /**
     * Purge orphaned holograms around a location.
     * If the owner entity is still alive and merged (stack > 1), re-create the
     * hologram.
     *
     * @param center The center location
     * @param radius Radius in blocks (0 = all loaded)
     * @return Number of holograms purged
     */
    public static int purgeHolograms(Location center, double radius) {
        int purged = 0;
        double radiusSq = radius * radius;

        for (Entity entity : center.getWorld().getEntities()) {
            if (!(entity instanceof TextDisplay td))
                continue;
            if (!td.getPersistentDataContainer().has(PDC_HOLO_TAG, PersistentDataType.BYTE))
                continue;

            // Check range
            if (radius > 0 && td.getLocation().distanceSquared(center) > radiusSq)
                continue;

            String ownerUuid = td.getPersistentDataContainer().get(PDC_OWNER_KEY, PersistentDataType.STRING);

            if (ownerUuid == null) {
                // Orphaned, no owner
                td.remove();
                purged++;
                continue;
            }

            Entity owner = org.bukkit.Bukkit.getEntity(UUID.fromString(ownerUuid));

            if (owner == null || owner.isDead() || !owner.isValid()) {
                // Owner gone — remove hologram
                td.remove();
                purged++;
            } else {
                // Owner alive — just refresh (remove & let NaturalLaggManager re-create on next
                // tick)
                td.remove();
                owner.removeMetadata(META_KEY, plugin);
                purged++;
            }
        }

        return purged;
    }

    /**
     * Purge ALL holograms in all worlds.
     *
     * @return Number of holograms purged
     */
    public static int purgeAllHolograms() {
        int purged = 0;
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof TextDisplay td))
                    continue;
                if (!td.getPersistentDataContainer().has(PDC_HOLO_TAG, PersistentDataType.BYTE))
                    continue;

                String ownerUuid = td.getPersistentDataContainer().get(PDC_OWNER_KEY, PersistentDataType.STRING);
                if (ownerUuid != null) {
                    try {
                        Entity owner = org.bukkit.Bukkit.getEntity(UUID.fromString(ownerUuid));
                        if (owner != null) {
                            owner.removeMetadata(META_KEY, plugin);
                        }
                    } catch (Exception ignored) {
                    }
                }

                td.remove();
                purged++;
            }
        }
        return purged;
    }

    /**
     * Get the existing TextDisplay hologram linked to an entity.
     */
    private static TextDisplay getExistingHologram(Entity target) {
        if (!target.hasMetadata(META_KEY))
            return null;

        try {
            String uuidStr = target.getMetadata(META_KEY).get(0).asString();
            UUID holoUuid = UUID.fromString(uuidStr);
            Entity holo = org.bukkit.Bukkit.getEntity(holoUuid);
            if (holo instanceof TextDisplay td && td.isValid()) {
                return td;
            }
        } catch (Exception ignored) {
        }

        // Clean up stale metadata
        target.removeMetadata(META_KEY, plugin);
        return null;
    }

    /**
     * Calculate the hologram position above the entity.
     */
    private static Location getHologramLocation(Entity target) {
        return target.getLocation().add(0, target.getHeight() + 0.3, 0);
    }
}
