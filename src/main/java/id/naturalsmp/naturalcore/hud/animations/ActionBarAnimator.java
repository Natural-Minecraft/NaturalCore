package id.naturalsmp.naturalcore.hud.animations;

import id.naturalsmp.naturalcore.utils.ChatUtils;

/**
 * Handles smooth animations for ActionBar transitions.
 * Provides true horizontal scrolling effects with visible gaps.
 */
public class ActionBarAnimator {

    // ActionBar approximate width in characters
    private static final int ACTIONBAR_WIDTH = 60;

    /**
     * Scroll transition: Old text slides LEFT, new text enters from RIGHT.
     * Creates a clear horizontal scrolling effect with visible gap.
     * 
     * @param oldText  The current text being displayed
     * @param newText  The new text to transition to
     * @param progress Animation progress (0.0 to 1.0)
     * @return The interpolated text for this frame
     */
    public static String scrollTransition(String lastText, String currentText, float progress) {
        if (progress >= 1.0f)
            return currentText;
        if (progress <= 0.0f)
            return lastText;

        // Colorize both to handle MiniMessage tags before slicing
        String oldText = ChatUtils.colorize(lastText != null ? lastText : "");
        String newText = ChatUtils.colorize(currentText != null ? currentText : "");

        float eased = easeOutCubic(progress);

        int oldLen = ChatUtils.getVisualLength(oldText);
        int newLen = ChatUtils.getVisualLength(newText);
        String gap = "     &8⋄     ";
        int gapLen = 11;

        // Timeline: [OldText] [Gap] [NewText]
        String timeline = oldText + ChatUtils.colorize(gap) + newText;

        // Visual Window Behavior:
        // We want the newText to stop at the center (ACTIONBAR_WIDTH / 2)
        int targetCenter = ACTIONBAR_WIDTH / 2;
        int newTextCenter = oldLen + gapLen + (newLen / 2);

        // The offset needed to bring newTextCenter to targetCenter
        int maxOffset = newTextCenter - targetCenter;
        if (maxOffset < 0)
            maxOffset = 0;

        // Total possible scroll (legacy behavior: scroll everything off)
        int totalScrollDistance = oldLen + gapLen + newLen;

        // current scroll based on progress
        int scrollOffset = (int) (eased * totalScrollDistance);

        // Stop the scroll when newText is centered
        int windowStart = Math.min(scrollOffset, maxOffset);

        String visiblePortion = ChatUtils.colorAwareSubstring(timeline, windowStart, timeline.length());

        // Pad to maintain visual stability
        int visibleLen = ChatUtils.getVisualLength(visiblePortion);
        if (visibleLen < ACTIONBAR_WIDTH && progress < 0.9f) {
            int paddingNeeded = Math.min(ACTIONBAR_WIDTH - visibleLen, 20);
            StringBuilder padded = new StringBuilder(visiblePortion);
            for (int i = 0; i < paddingNeeded; i++) {
                padded.append(" ");
            }
            return padded.toString();
        }

        return visiblePortion;
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
