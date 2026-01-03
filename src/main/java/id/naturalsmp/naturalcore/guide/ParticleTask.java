package id.naturalsmp.naturalcore.guide;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {

    private final Location location;
    private final Particle particle;
    private int ticks;
    
    public ParticleTask(Location location, Particle particle) {
        this.location = location;
        this.particle = particle;
        this.ticks = 0;
    }

    @Override
    public void run() {
        if (ticks >= 100) { // Stop after 5 seconds (100 ticks)
            cancel();
            return;
        }
        
        // Spawn particles in a circle pattern
        double radius = 1.5;
        double angle = (ticks * 10) % 360;
        double radians = Math.toRadians(angle);
        
        double x = location.getX() + (radius * Math.cos(radians));
        double z = location.getZ() + (radius * Math.sin(radians));
        double y = location.getY() + (Math.sin(ticks * 0.1) * 0.5);
        
        Location particleLoc = new Location(location.getWorld(), x, y, z);
        
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(particle, particleLoc, 3, 0.1, 0.1, 0.1, 0);
        }
        
        ticks++;
    }
    
    public static void spawnHelixParticles(Location center, Particle particle, int duration) {
        ParticleTask task = new ParticleTask(center, particle);
        task.runTaskTimer(org.bukkit.Bukkit.getPluginManager().getPlugin("NaturalCore"), 0L, 1L);
    }
}
