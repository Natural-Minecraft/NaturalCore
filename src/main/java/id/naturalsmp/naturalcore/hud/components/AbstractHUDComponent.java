package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;

/**
 * Base class for HUD components with common functionality.
 */
public abstract class AbstractHUDComponent implements HUDComponent {

    protected final NaturalCore plugin;
    protected final String id;
    protected final HUDPriority priority;

    protected AbstractHUDComponent(NaturalCore plugin, String id, HUDPriority priority) {
        this.plugin = plugin;
        this.id = id;
        this.priority = priority;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public HUDPriority getPriority() {
        return priority;
    }
}
