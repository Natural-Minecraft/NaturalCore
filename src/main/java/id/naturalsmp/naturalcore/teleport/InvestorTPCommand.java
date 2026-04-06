package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * /itp <player> — Investor Teleport
 * Force-teleport to any player without TPA accept.
 * Features a Sonic Boom particle animation before teleporting.
 * Permission: naturalsmp.itp
 */
public class InvestorTPCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public InvestorTPCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage(ConfigUtils.getString("messages.global.only-player"));
            return true;
        }

        if (!p.hasPermission("naturalsmp.itp")) {
            p.sendMessage(ChatUtils.toComponent(ConfigUtils.getString("messages.global.no-permission")));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ChatUtils.toComponent("&c&l⚠ &cGunakan: &f/itp <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(ChatUtils.toComponent(
                    ConfigUtils.getString("messages.global.player-not-found", "Player not found")
                            .replace("%player%", args[0])));
            return true;
        }

        if (target.equals(p)) {
            p.sendMessage(ChatUtils.toComponent("&c&l⚠ &cTidak bisa teleport ke diri sendiri!"));
            return true;
        }

        // Block in dungeon worlds
        if (p.getWorld().getName().toLowerCase().startsWith("dungeon")) {
            p.sendMessage(ChatUtils.toComponent("&c&l⚠ &cKamu tidak bisa menggunakan ITP di dungeon!"));
            return true;
        }
        if (target.getWorld().getName().toLowerCase().startsWith("dungeon")) {
            p.sendMessage(ChatUtils.toComponent("&c&l⚠ &cPemain tersebut sedang berada di dungeon!"));
            return true;
        }

        // Execute the sonic boom teleport sequence
        executeSonicBoomTP(p, target);
        return true;
    }

    private void executeSonicBoomTP(Player p, Player target) {
        Location origin = p.getLocation().clone();

        // Phase 1: Charge-up effect at origin (0.5 seconds)
        p.sendMessage(ChatUtils.toComponent(
                "&4&l⚡ INVESTOR TELEPORT &8» &7Mengunci lokasi &e" + target.getName() + "&7..."));

        // Play warden sonic boom charge sound
        p.getWorld().playSound(origin, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.5f, 0.8f);

        // Spiral particle charge-up
        new BukkitRunnable() {
            int tick = 0;
            final int chargeTime = 15; // 0.75 seconds charge

            @Override
            public void run() {
                if (tick >= chargeTime || !p.isOnline()) {
                    this.cancel();

                    // Phase 2: Sonic Boom at origin
                    if (p.isOnline()) {
                        spawnSonicBoom(origin);

                        // Phase 3: Teleport after boom
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (!p.isOnline() || !target.isOnline()) return;

                            // Save back location
                            plugin.getTeleportManager().setLastLocation(p);

                            // Teleport
                            Location targetLoc = target.getLocation();
                            p.teleport(targetLoc);

                            // Phase 4: Arrival effects at target location
                            spawnArrivalEffect(targetLoc);

                            // Sound effects for both
                            p.playSound(targetLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8f, 1.2f);
                            target.playSound(targetLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.6f, 1.5f);
                            p.playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.7f);

                            // Messages
                            p.sendMessage(ChatUtils.toComponent(
                                    "&4&l⚡ &7Teleportasi ke &e" + target.getName() + " &7berhasil!"));
                            target.sendMessage(ChatUtils.toComponent(
                                    "&4&l⚡ &e" + p.getName() + " &7telah teleport ke lokasimu! &8(Investor TP)"));

                        }, 5L); // 0.25s after boom
                    }
                    return;
                }

                // Charge-up spiral particles
                double angle = tick * 0.8;
                double radius = 1.5 - (tick * 0.08); // Spiral inward
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = tick * 0.15;

                Location particleLoc = origin.clone().add(x, y, z);

                // Dark sculk-like particles spiraling
                p.getWorld().spawnParticle(Particle.SONIC_BOOM, origin.clone().add(0, 1, 0),
                        0, 0, 0, 0, 0);
                p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, particleLoc,
                        3, 0.1, 0.1, 0.1, 0.02);
                p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLoc,
                        2, 0.05, 0.05, 0.05, 0.01);

                // Ambient sound ticks
                if (tick % 5 == 0) {
                    p.getWorld().playSound(origin, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.3f,
                            1.5f + (tick * 0.05f));
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnSonicBoom(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Location center = loc.clone().add(0, 1, 0);

        // Big sonic boom at center
        world.spawnParticle(Particle.SONIC_BOOM, center, 1, 0, 0, 0, 0);

        // Ring of particles expanding outward
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= 8) {
                    this.cancel();
                    return;
                }

                double radius = tick * 0.8;
                for (int i = 0; i < 16; i++) {
                    double angle = (2 * Math.PI / 16) * i;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    world.spawnParticle(Particle.SCULK_CHARGE_POP, center.clone().add(x, 0, z),
                            2, 0.05, 0.05, 0.05, 0.01);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Explosion sound
        world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.0f);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
    }

    private void spawnArrivalEffect(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Location center = loc.clone().add(0, 1, 0);

        // Implosion effect — particles converging inward
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= 10) {
                    this.cancel();
                    return;
                }

                double radius = 3.0 - (tick * 0.3);
                for (int i = 0; i < 12; i++) {
                    double angle = (2 * Math.PI / 12) * i + (tick * 0.3);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = Math.sin(tick * 0.5) * 0.5;

                    world.spawnParticle(Particle.END_ROD, center.clone().add(x, y, z),
                            1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, center.clone().add(x, y + 0.5, z),
                            1, 0, 0, 0, 0.02);
                }

                if (tick == 5) {
                    // Secondary sonic boom at arrival
                    world.spawnParticle(Particle.SONIC_BOOM, center, 1, 0, 0, 0, 0);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
