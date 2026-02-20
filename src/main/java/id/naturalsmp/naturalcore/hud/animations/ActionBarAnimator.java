package id.naturalsmp.naturalcore.hud.animations;

import id.naturalsmp.naturalcore.utils.ChatUtils;

/**
 * Handles smooth animations for ActionBar transitions.
 * Provides true horizontal scrolling effects with visible gaps.
 */
public class ActionBarAnimator {

    // Action Bar constrained to Hotbar width (approx 40-50 chars)
    private static final int ACTIONBAR_WIDTH = 50;

    /**
     * Scroll transition: Slide effect using a fixed window tape.
     * Keeps the visible width constant to prevent Minecraft from jittering the
     * center point.
     * 
     * @param lastText    The current text being displayed
     * @param currentText The new text to transition to
     * @param progress    Animation progress (0.0 to 1.0)
     * @return The interpolated text for this frame
     */
    /**
     * Replaces the old scrolling effect. No longer used, but kept for API
     * compatibility.
     * Defers to reveal effect.
     */
    @Deprecated
    public static String scrollTransition(String lastText, String currentText, float progress) {
        return revealEffect(currentText, progress);
    }

    /**
     * Center-aligned Revealing effect. Smoothly expands text outward from the
     * center
     * avoiding Minecraft's default jitter by using full String but styled with
     * dynamic colors.
     *
     * @param text     The full text to reveal
     * @param progress Animation progress (0.0 to 1.0)
     * @return Center-revealed string
     */
    public static String revealEffect(String text, float progress) {
        if (text == null || text.isEmpty())
            return "";
        if (progress >= 1.0f)
            return text;
        if (progress <= 0.0f)
            return "";

        float eased = easeOutCubic(progress);
        int fullLength = ChatUtils.getVisualLength(text);
        int revealRadius = (int) ((fullLength / 2.0f) * eased);

        // Calculate visible center boundaries
        int center = fullLength / 2;
        int startIndex = center - revealRadius;
        int endIndex = center + revealRadius;

        // Ensure bounds
        startIndex = Math.max(0, startIndex);
        endIndex = Math.min(fullLength, endIndex);

        return ChatUtils.getVisualSlice(text, startIndex, endIndex - startIndex);
    }

    /**
     * Opposite of reveal. Text collapses into the center.
     */
    public static String fadeOutEffect(String text, float progress) {
        if (text == null || text.isEmpty())
            return "";
        if (progress >= 1.0f)
            return "";
        if (progress <= 0.0f)
            return text;

        float eased = easeInCubic(progress);
        int fullLength = ChatUtils.getVisualLength(text);

        // Progress defines how much is cut from the edges
        int cutRadius = (int) ((fullLength / 2.0f) * eased);

        int center = fullLength / 2;
        int startIndex = cutRadius;
        int endIndex = fullLength - cutRadius;

        if (startIndex >= endIndex)
            return "";

        return ChatUtils.getVisualSlice(text, startIndex, endIndex - startIndex);
    }

    /**
     * Cubic easing function for smooth acceleration at end.
     */
    public static float easeOutCubic(float x) {
        return 1 - (float) Math.pow(1 - x, 3);
    }

    /**
     * Cubic easing function for smooth deceleration at start.
     */
    public static float easeInCubic(float x) {
        return x * x * x;
    }

    /**
     * Smooth step easing (S-curve).
     */
    public static float smoothStep(float x) {
        return x * x * (3 - 2 * x);
    }
}
