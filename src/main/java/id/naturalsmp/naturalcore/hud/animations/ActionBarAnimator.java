package id.naturalsmp.naturalcore.hud.animations;

import id.naturalsmp.naturalcore.utils.ChatUtils;

/**
 * Handles smooth animations for ActionBar transitions.
 * Provides methods for scroll, fade, and reveal effects.
 */
public class ActionBarAnimator {

    /**
     * Scroll transition: Push old text left, new text enters from right.
     * 
     * @param oldText  The current text being displayed
     * @param newText  The new text to transition to
     * @param progress Animation progress (0.0 to 1.0)
     * @return The interpolated text for this frame
     */
    public static String scrollTransition(String oldText, String newText, float progress) {
        if (oldText == null || oldText.isEmpty())
            return newText;
        if (newText == null || newText.isEmpty())
            return oldText;
        if (progress >= 1.0f)
            return newText;
        if (progress <= 0.0f)
            return oldText;

        // Apply easing for smooth motion
        float eased = easeOutCubic(progress);

        // Calculate visible portion
        int oldEnd = (int) (ChatUtils.getVisualLength(oldText) * (1 - eased));
        String visibleOld = ChatUtils.colorAwareSubstring(oldText, 0, oldEnd);

        // Spacer between texts
        String spacer = "   ";

        // Build combined view
        int newStart = (int) (ChatUtils.getVisualLength(newText) * (1 - eased));
        String visibleNew = ChatUtils.colorAwareSubstring(newText, newStart, newText.length());

        return visibleOld + spacer + visibleNew;
    }

    /**
     * Reveal effect: Text appears character by character from left.
     * 
     * @param text     The full text to reveal
     * @param progress Animation progress (0.0 to 1.0)
     * @return The revealed portion of the text
     */
    public static String revealEffect(String text, float progress) {
        if (text == null || text.isEmpty())
            return "";
        if (progress >= 1.0f)
            return text;
        if (progress <= 0.0f)
            return "";

        float eased = easeOutCubic(progress);
        int revealLen = (int) (ChatUtils.getVisualLength(text) * eased);
        return ChatUtils.colorAwareSubstring(text, 0, revealLen);
    }

    /**
     * Fade out effect: Text disappears from left.
     * 
     * @param text     The text to fade out
     * @param progress Animation progress (0.0 to 1.0)
     * @return The remaining visible text
     */
    public static String fadeOutEffect(String text, float progress) {
        if (text == null || text.isEmpty())
            return "";
        if (progress >= 1.0f)
            return "";
        if (progress <= 0.0f)
            return text;

        float eased = easeInCubic(progress);
        int cutLen = (int) (ChatUtils.getVisualLength(text) * eased);
        return ChatUtils.colorAwareSubstring(text, cutLen, text.length());
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
