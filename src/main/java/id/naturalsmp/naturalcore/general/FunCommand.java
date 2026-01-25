package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FunCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public FunCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigUtils.getString("messages.global.only-player"));
            return true;
        }

        Player p = (Player) sender;
        String cmd = label.toLowerCase();

        switch (cmd) {
            case "fireball" -> {
                if (!p.hasPermission("naturalsmp.fun.fireball")) {
                    p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                    return true;
                }
                Fireball fb = p.launchProjectile(Fireball.class);
                fb.setYield(2.0f); // Default power
                p.sendMessage(ChatUtils.colorize(
                        ConfigUtils.getString("messages.fun.fireball-launch", "&#FF5500&l🔥 &7Fireball diluncurkan!")));
            }
            case "firework" -> {
                if (!p.hasPermission("naturalsmp.fun.firework")) {
                    p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                    return true;
                }
                spawnRandomFirework(p.getLocation());
                p.sendMessage(ChatUtils.colorize(ConfigUtils.getString("messages.fun.firework-launch",
                        "&#FF55FF&l🎆 &7Kembang api diluncurkan!")));
            }
            case "jumpto", "j" -> {
                if (!p.hasPermission("naturalsmp.jumpto")) {
                    p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                    return true;
                }
                Block target = p.getTargetBlockExact(100);
                if (target == null || target.getType() == Material.AIR) {
                    p.sendMessage(ChatUtils.colorize("&cTarget terlalu jauh atau tidak valid!"));
                    return true;
                }
                Location loc = target.getLocation().add(0.5, 1, 0.5);
                loc.setYaw(p.getLocation().getYaw());
                loc.setPitch(p.getLocation().getPitch());
                p.teleport(loc);
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                p.sendMessage(
                        ChatUtils.colorize(ConfigUtils.getString("messages.fun.jump-success", "&#55FF55&l⚡ &7Jump!")));
            }
        }

        return true;
    }

    private void spawnRandomFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        Random r = new Random();

        FireworkEffect.Type type = FireworkEffect.Type.values()[r.nextInt(FireworkEffect.Type.values().length)];
        Color c1 = Color.fromRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
        Color c2 = Color.fromRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(r.nextBoolean())
                .withColor(c1)
                .withFade(c2)
                .with(type)
                .trail(r.nextBoolean())
                .build();

        fwm.addEffect(effect);
        fwm.setPower(r.nextInt(2) + 1);
        fw.setFireworkMeta(fwm);
    }
}
