package id.naturalsmp.naturalcore.hud.animations;

import id.naturalsmp.naturalcore.utils.ChatUtils;

/**
 * Handles smooth animations for ActionBar transitions.
 * Provides true horizontal scrolling effects with visible gaps.
 */
public class ActionBarAnimator {

    // Action Bar constrained to Hotbar width (approx 40-50 chars)
    private static final int ACTIONBAR_WIDTH = 45;

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
    public static String scrollTransition(String lastText, String currentText, float progress) {
        if (progress >= 1.0f)
            return currentText;
        if (progress <= 0.0f)
            return lastText;

        // 1. Prepare Content
        String oldT = lastText != null ? lastText : "";
        String newT = currentText != null ? currentText : "";
        String gap = "   &#4facfe»   "; // Visual separator with Hex color

        // 2. Build the Tape (Keep it in raw/mixed format, getVisualSlice will handle
        // it)
        String padding = " ".repeat(ACTIONBAR_WIDTH);
        String tape = padding + oldT + gap + newT + padding;

        // 3. Calculate Visual Measurements
        int oldLen = ChatUtils.getVisualLength(oldT);
        int newLen = ChatUtils.getVisualLength(newT);
        int gapLen = ChatUtils.getVisualLength(gap);
        int paddingLen = ACTIONBAR_WIDTH;

        // 4. Determine Start and End Offsets in Visual Space
        // We want to slice a window of exactly ACTIONBAR_WIDTH.

        // Start: Center OldText in the window
        int startOffset = paddingLen - (ACTIONBAR_WIDTH - oldLen) / 2;

        // End: Center NewText in the window
        int endOffset = (paddingLen + oldLen + gapLen) - (ACTIONBAR_WIDTH - newLen) / 2;

        // 5. Interpolate using smoothStep
        float eased = smoothStep(progress);
        int currentOffset = (int) (startOffset + (endOffset - startOffset) * eased);

        // 6. Slice the Tape using our new robust method
        return ChatUtils.getVisualSlice(tape, currentOffset, ACTIONBAR_WIDTH);
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
        return ChatUtils.getVisualSlice(text, 0, revealLen);
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
        return ChatUtils.getVisualSlice(text, cutLen, ChatUtils.getVisualLength(text) - cutLen);
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
