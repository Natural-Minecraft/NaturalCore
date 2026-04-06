package id.naturalsmp.naturalcore.media;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MediaSneakListener implements Listener {

    private final NaturalCore plugin;
    private final ProtocolManager protocolManager;

    // Track which players are currently seeing which media player glowing
    // Observer UUID -> Media Player UUID
    private final Map<UUID, UUID> activeGlows = new HashMap<>();

    public MediaSneakListener(NaturalCore plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player observer = e.getPlayer();

        if (e.isSneaking()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!observer.isOnline() || !observer.isSneaking()) return;

                Entity target = getTargetEntity(observer, 2.0); // 2 blocks max distance (from inside/very close)

                if (target instanceof Player mediaTarget) {
                    if (isMedia(mediaTarget)) {
                        validateLineOfSightAndGlow(observer, mediaTarget);
                    }
                }
            }, 5L); // Slight delay to ensure they are fully sneaking
        } else {
            // Remove glow if they stop sneaking
            UUID targetId = activeGlows.remove(observer.getUniqueId());
            if (targetId != null) {
                Player target = Bukkit.getPlayer(targetId);
                if (target != null && target.isOnline()) {
                    sendGlowPacket(observer, target, false);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        Player observer = e.getPlayer();
        if (!(e.getRightClicked() instanceof Player mediaTarget)) return;

        if (activeGlows.containsKey(observer.getUniqueId())) {
            UUID glowingTarget = activeGlows.get(observer.getUniqueId());
            if (glowingTarget.equals(mediaTarget.getUniqueId())) {
                String link = plugin.getMediaManager().getLink(mediaTarget.getUniqueId());

                observer.playSound(observer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                observer.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §aLink Channel " + mediaTarget.getName() + ":"));
                observer.sendMessage(ChatUtils.colorize("§b" + link));

                mediaTarget.playSound(mediaTarget.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                mediaTarget.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §e" + observer.getName() + " §asedang melihat link channel-mu!"));
            }
        }
    }

    private void validateLineOfSightAndGlow(Player observer, Player target) {
        if (observer.hasLineOfSight(target) && observer.getLocation().distanceSquared(target.getLocation()) <= 4.0) { // 2 blocks squared
            activeGlows.put(observer.getUniqueId(), target.getUniqueId());
            sendGlowPacket(observer, target, true);
        }
    }

    private boolean isMedia(Player p) {
        return p.hasPermission("naturalsmp.media");
    }

    private void sendGlowPacket(Player observer, Player target, boolean glowing) {
        if (!observer.isOnline() || !target.isOnline()) return;

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, target.getEntityId());

        // Byte index 0 is normally Entity Status flag
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(target).deepClone();
        
        // Ensure index 0 exists (Status mask)
        byte mask = 0;
        if (watcher.hasIndex(0)) {
            mask = (byte) watcher.getByte(0);
        }

        if (glowing) {
            mask |= 0x40; // 0x40 is glowing
        } else {
            mask &= ~0x40;
        }

        // Send WrappedDataValue array for 1.19+ and 1.20+
        WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataValue dataValue = new WrappedDataValue(0, byteSerializer, mask);

        packet.getDataValueCollectionModifier().write(0, Collections.singletonList(dataValue));

        try {
            protocolManager.sendServerPacket(observer, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Entity getTargetEntity(Player player, double range) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof Player && player.hasLineOfSight(entity)) {
                // Determine if player is actually looking at them
                org.bukkit.util.Vector direction = player.getLocation().getDirection();
                org.bukkit.util.Vector toEntity = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                if (direction.dot(toEntity) > 0.8D) {
                    return entity;
                }
            }
        }
        return null;
    }
}
