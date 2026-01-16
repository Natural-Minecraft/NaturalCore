package id.naturalsmp.naturalcore.afk;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager {

    private final NaturalCore plugin;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Map<UUID, Boolean> afkStatus = new HashMap<>();
    private final Map<UUID, TextDisplay> afkIndicators = new HashMap<>();

    private long timeoutMillis;

    public AFKManager(NaturalCore plugin) {
        this.plugin = plugin;
        reloadConfig();
        startTask();
    }

    public void reloadConfig() {
        // Default 300 detik
        this.timeoutMillis = ConfigUtils.getInt("afk.timeout", 300) * 1000L;
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isAFK(p))
                        continue; // Sudah AFK

                    if (!lastActivity.containsKey(p.getUniqueId())) {
                        lastActivity.put(p.getUniqueId(), now);
                    }

                    long last = lastActivity.get(p.getUniqueId());
                    if (now - last > timeoutMillis) {
                        setAFK(p, true);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void updateActivity(Player p) {
        // Jika sedang AFK, dan bergerak -> Matikan AFK
        if (isAFK(p)) {
            setAFK(p, false);
        }
        lastActivity.put(p.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isAFK(Player p) {
        return afkStatus.getOrDefault(p.getUniqueId(), false);
    }

    public void setAFK(Player p, boolean state) {
        if (state) {
            afkStatus.put(p.getUniqueId(), true);

            // Broadcast (Optional)
            // Bukkit.broadcastMessage(ChatUtils.colorize("&7* &e" + p.getName() + "
            // &7sekarang AFK."));

            p.setPlayerListName(ChatUtils.colorize("&7&o" + p.getName() + " [AFK]"));
            spawnIndicator(p);

            // Send Title
            if (ConfigUtils.getBoolean("afk.title.enabled")) {
                String title = ConfigUtils.getString("afk.title.title", "&b&lYou are now AFK");
                String sub = ConfigUtils.getString("afk.title.subtitle", "&7Move to resume...");
                p.sendTitle(ChatUtils.colorize(title), ChatUtils.colorize(sub), 10, 70, 20);
            }

        } else {
            afkStatus.put(p.getUniqueId(), false);

            // Broadcast (Optional)
            // Bukkit.broadcastMessage(ChatUtils.colorize("&7* &e" + p.getName() + "
            // &7kembali online."));

            p.setPlayerListName(null); // Reset Tablist
            removeIndicator(p);

            p.sendMessage(ChatUtils.colorize("&aWelcome back!"));
        }
    }

    private void spawnIndicator(Player p) {
        removeIndicator(p); // Safety check

        Location loc = p.getLocation().add(0, 2.5, 0); // Di atas kepala
        TextDisplay display = (TextDisplay) p.getWorld().spawnEntity(loc, EntityType.TEXT_DISPLAY);

        String text = ConfigUtils.getString("afk.display-text", "&b💤 &7Sedang Bermimpi... &b💤");
        display.setText(ChatUtils.colorize(text));
        display.setBillboard(Display.Billboard.CENTER); // Selalu menghadap player
        display.setSeeThrough(true); // Bisa dilihat tembus tembok? Optional.

        // Scale animation? Nanti dulu, statis saja biar performa aman.
        // display.setTransformation(new Transformation(new Vector3f(), new
        // AxisAngle4f(), new Vector3f(1.5f), new AxisAngle4f()));

        afkIndicators.put(p.getUniqueId(), display);

        // Safety: Mount ke player agar ikut bergerak?
        // Jika player AFK biasanya diam, tapi kalau didorong air?
        // Passenger bisa, tapi TextDisplay di 1.21 support riding? Bisa.
        p.addPassenger(display);
        // Note: Passenger position might imply offset logic tweaks or just relying on
        // `addPassenger`.
        // TextDisplay as passenger sits specific way. Let's try basic spawn at location
        // first.
        // Actually, if we mount, Y offset is handled by entity height.
        // Let's stick to simple spawn at location. If player moves, AFK is cancelled
        // anyway.
        // But water flow?
        // Keep it simple: Static. If pushed, AFK breaks manually or visual stays behind
        // (bug?).
        // If passenger: p.addPassenger(display); -> Offset adjustment needed usually.
        // Let's cancel logic: If player moves -> updateActivity -> setAFK(false) ->
        // removeIndicator.
        // So static is fine. Exception: Water flow pushing player without input.
        // `PlayerMoveEvent` trigger can filter "physical movement" vs "camera".
        // If water pushes, PlayerMoveEvent Fired. AFK Cancelled. Correct.
    }

    public void removeIndicator(Player p) {
        if (afkIndicators.containsKey(p.getUniqueId())) {
            TextDisplay display = afkIndicators.remove(p.getUniqueId());
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        // Cek juga passenger in case
        for (org.bukkit.entity.Entity passenger : p.getPassengers()) {
            if (passenger instanceof TextDisplay && ((TextDisplay) passenger).getText().contains("Bermimpi")) {
                passenger.remove();
            }
        }
    }

    public void cleanup() {
        for (TextDisplay d : afkIndicators.values()) {
            if (d != null)
                d.remove();
        }
        afkIndicators.clear();
    }
}
