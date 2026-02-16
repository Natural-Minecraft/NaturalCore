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

        if (display == null) {
            // Create new holo as passenger
            display = target.getWorld().spawn(target.getLocation(), TextDisplay.class, d -> {
                d.setPersistent(false); // Don't save to disk
                d.getPersistentDataContainer().set(PDC_HOLO_TAG, PersistentDataType.BYTE, (byte) 1);

                d.setBillboard(Display.Billboard.CENTER);
                d.setSeeThrough(true);
                d.setShadowed(false);
                d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0)); // Transparent background

                // Scale and Translation
                // Position it slightly above the mob's head.
                // Since it's a passenger, (0,0,0) is the seat.
                // We add a translation up.
                Transformation transformation = d.getTransformation();
                transformation.getTranslation().set(0.0f, 0.7f, 0.0f);
                d.setTransformation(transformation);
            });
            target.addPassenger(display);
        }

        display.text(ChatUtils.toComponent(text));
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
     * Legacy Cleanup: Remove all old holograms from the world that might be
     * lingering
     * from the previous system (teleport-based).
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
