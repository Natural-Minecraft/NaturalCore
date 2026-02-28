package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

import java.util.List;

/**
 * Utility for managing floating TextDisplay holograms attached to entities.
 * Optimized to use Passengers instead of Teleport Ticks.
 * Premium minimalist design.
 */
public class HologramUtil {

    private static NamespacedKey PDC_HOLO_TAG;

    public static void init(NaturalCore p) {
        PDC_HOLO_TAG = new NamespacedKey(p, "natural_holo");
    }

    /**
     * Create or update the hologram for a target entity.
     *
     * @param target The entity to attach the hologram to
     * @param text   MiniMessage-formatted text to display
     */
    public static void updateHologram(LivingEntity target, String text) {
        if (target == null || !target.isValid() || target.isDead())
            return;

        TextDisplay display = getPassengerHologram(target);

        // Calculate Y offset based on entity height for better positioning
        float yOffset = getHologramOffset(target);

        if (display == null) {
            // Create new holo as passenger
            display = target.getWorld().spawn(target.getLocation(), TextDisplay.class, d -> {
                d.setPersistent(false); // Don't save to disk
                d.getPersistentDataContainer().set(PDC_HOLO_TAG, PersistentDataType.BYTE, (byte) 1);

                d.setBillboard(Display.Billboard.CENTER);
                d.setSeeThrough(true);
                d.setShadowed(false);
                d.setViewRange(5.0f); // Limit view to 5 blocks radius
                d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0)); // Transparent background

                // Position above mob head
                Transformation transformation = d.getTransformation();
                transformation.getTranslation().set(0.0f, yOffset, 0.0f);
                d.setTransformation(transformation);
            });
            target.addPassenger(display);
        } else {
            // Update existing position if needed
            Transformation transformation = display.getTransformation();
            if (Math.abs(transformation.getTranslation().y() - yOffset) > 0.1f) {
                transformation.getTranslation().set(0.0f, yOffset, 0.0f);
                display.setTransformation(transformation);
            }
        }

        display.text(ChatUtils.toComponent(text));
    }

    /**
     * Get Y offset for hologram based on entity type/height.
     * Ensures the hologram sits nicely above the mob's head.
     */
    private static float getHologramOffset(LivingEntity entity) {
        double height = entity.getHeight();

        // Small mobs (chicken, rabbit, silverfish)
        if (height < 0.6)
            return 0.3f;
        // Medium-small (pig, sheep, wolf)
        if (height < 1.0)
            return 0.5f;
        // Medium (cow, zombie, skeleton)
        if (height < 2.0)
            return 0.7f;
        // Tall (enderman, iron golem)
        if (height < 3.0)
            return 0.9f;
        // Very tall (ghast, wither)
        return 1.2f;
    }

    /**
     * Remove the hologram attached to a target entity.
     */
    public static void removeHologram(LivingEntity target) {
        if (target == null)
            return;

        TextDisplay display = getPassengerHologram(target);
        if (display != null) {
            display.remove();
        }
    }

    /**
     * Helper to find the specific TextDisplay passenger that belongs to us.
     */
    private static TextDisplay getPassengerHologram(LivingEntity target) {
        List<Entity> passengers = target.getPassengers();
        for (Entity passenger : passengers) {
            if (passenger instanceof TextDisplay td) {
                if (td.getPersistentDataContainer().has(PDC_HOLO_TAG, PersistentDataType.BYTE)) {
                    return td;
                }
            }
        }
        return null; // Not found
    }

    /**
     * Purge orphaned holograms around a location.
     *
     * @param center The center location
     * @param radius Radius in blocks (0 = all loaded)
     * @return Number of holograms purged
     */
    public static int purgeHolograms(org.bukkit.Location center, double radius) {
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

            td.remove();
            purged++;
        }
        return purged;
    }

    /**
     * Purge ALL holograms from all worlds.
     * Used on server startup to clean orphans from crashes.
     */
    public static int purgeAllHolograms() {
        int purged = 0;
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof TextDisplay td) {
                    if (td.getPersistentDataContainer().has(PDC_HOLO_TAG, PersistentDataType.BYTE)) {
                        td.remove();
                        purged++;
                    }
                }
            }
        }
        return purged;
    }
}
