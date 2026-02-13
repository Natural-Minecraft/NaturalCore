package id.naturalsmp.naturalcore.hud;

/**
 * Priority levels for HUD components.
 * Higher values = higher priority = shown first.
 */
public enum HUDPriority {

    CRITICAL(100), // Temperature warning, HP critical
    HIGH(75), // Lagg notifications
    MEDIUM(50), // Normal combat info, Tips
    LOW(25), //
    DEFAULT(10); // Season info, always-on HUD

    private final int value;

    HUDPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
