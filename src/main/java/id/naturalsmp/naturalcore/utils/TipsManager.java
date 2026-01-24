package id.naturalsmp.naturalcore.utils;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class TipsManager {

    private final NaturalCore plugin;
    private List<String> tips;
    private long intervalHooks;
    private int stayDurationTicks;
    private String soundName;

    private long lastRun = System.currentTimeMillis();
    private TipState state = TipState.IDLE;

    private String currentTip = "";

    private int animationFrame = 0;
    private final int MAX_FRAMES = 20; // Length of transition

    public enum TipState {
        IDLE,
        SLIDING_IN, // Transition Main -> Tip
        STATIC, // Showing Tip
        SLIDING_OUT // Transition Tip -> Main
    }

    public TipsManager(NaturalCore plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.tips = ConfigUtils.getStringList("tips.list");
        this.intervalHooks = ConfigUtils.getInt("tips.interval", 300) * 1000L;
        this.stayDurationTicks = ConfigUtils.getInt("tips.duration", 4) * 20;
        this.soundName = ConfigUtils.getString("tips.sound", "BLOCK_NOTE_BLOCK_HAT");
    }

    public void tick() {
        long now = System.currentTimeMillis();

        if (state == TipState.IDLE) {
            if (now - lastRun > intervalHooks) {
                if (!tips.isEmpty()) {
                    currentTip = tips.get(new Random().nextInt(tips.size()));
                    state = TipState.SLIDING_IN;
                    animationFrame = 0;
                    lastRun = now;
                }
            }
        } else if (state == TipState.SLIDING_IN) {
            animationFrame++;
            if (animationFrame % 2 == 0)
                playSound();
            if (animationFrame >= MAX_FRAMES) {
                state = TipState.STATIC;
                animationFrame = 0;
            }
        } else if (state == TipState.STATIC) {
            animationFrame++;
            if (animationFrame >= stayDurationTicks) {
                state = TipState.SLIDING_OUT;
                animationFrame = 0;
            }
        } else if (state == TipState.SLIDING_OUT) {
            animationFrame++;
            if (animationFrame % 2 == 0)
                playSound();
            if (animationFrame >= MAX_FRAMES) {
                state = TipState.IDLE;
                animationFrame = 0;
            }
        }
    }

    private void playSound() {
        try {
            Sound s = Sound.valueOf(soundName);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), s, 0.5f, 2.0f);
            }
        } catch (Exception ignored) {
        }
    }

    public String getDisplay(String mainBarText) {
        if (state == TipState.IDLE)
            return null;

        String cleanMain = ChatUtils.colorize(mainBarText);
        String cleanTip = ChatUtils.colorize(currentTip);

        if (state == TipState.SLIDING_IN) {
            float progress = (float) animationFrame / MAX_FRAMES;
            int cutLen = (int) (cleanMain.length() * progress);
            int revealLen = (int) (cleanTip.length() * progress);

            String partMain = ChatUtils.colorAwareSubstring(cleanMain, cutLen, cleanMain.length());
            String partTip = ChatUtils.colorAwareSubstring(cleanTip, 0, revealLen);

            return partMain + "     " + partTip;

        } else if (state == TipState.STATIC) {
            return cleanTip;
        } else if (state == TipState.SLIDING_OUT) {
            float progress = (float) animationFrame / MAX_FRAMES;
            int cutLen = (int) (cleanTip.length() * progress);
            int revealLen = (int) (cleanMain.length() * progress);

            String partTip = ChatUtils.colorAwareSubstring(cleanTip, cutLen, cleanTip.length());
            String partMain = ChatUtils.colorAwareSubstring(cleanMain, 0, revealLen);

            return partTip + "     " + partMain;
        }

        return null;
    }

    public TipState getState() {
        return state;
    }
}
