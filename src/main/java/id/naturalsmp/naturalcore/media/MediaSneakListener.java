package id.naturalsmp.naturalcore.media;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MediaSneakListener implements Listener {

    private final NaturalCore plugin;
    private final ProtocolManager protocolManager;

    // Observer UUID -> Media Player UUID
    private final Map<UUID, UUID> activeGlows = new HashMap<>();
    private BukkitTask glowCheckTask;

    public MediaSneakListener(NaturalCore plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        startGlowDistanceCheck();
    }

    /**
     * Periodic task: every 10 ticks (~0.5s) check if glow should be removed
     * because observer stopped sneaking, target moved away, or either went offline.
     */
    private void startGlowDistanceCheck() {
        glowCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<UUID, UUID>> it = activeGlows.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, UUID> entry = it.next();
                Player observer = Bukkit.getPlayer(entry.getKey());
                Player target = Bukkit.getPlayer(entry.getValue());

                boolean shouldRemove = false;

                if (observer == null || !observer.isOnline() || target == null || !target.isOnline()) {
                    shouldRemove = true;
                } else if (!observer.isSneaking()) {
                    shouldRemove = true;
                } else if (observer.getLocation().distanceSquared(target.getLocation()) > 4.0) {
                    // Target moved beyond 2 blocks
                    shouldRemove = true;
                } else if (!observer.hasLineOfSight(target)) {
                    shouldRemove = true;
                }

                if (shouldRemove) {
                    it.remove();
                    if (observer != null && observer.isOnline() && target != null && target.isOnline()) {
                        sendGlowPacket(observer, target, false);
                    }
                }
            }
        }, 10L, 10L);
    }

    public void stop() {
        if (glowCheckTask != null) {
            glowCheckTask.cancel();
        }
        activeGlows.clear();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player observer = e.getPlayer();

        if (e.isSneaking()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!observer.isOnline() || !observer.isSneaking()) return;

                Entity target = getTargetEntity(observer, 2.0);

                if (target instanceof Player mediaTarget) {
                    if (isMedia(mediaTarget)) {
                        validateLineOfSightAndGlow(observer, mediaTarget);
                    }
                }
            }, 5L);
        } else {
            // Remove glow on un-sneak
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
        if (e.getHand() != EquipmentSlot.HAND) return; // Only process main hand to prevent double firing
        Player observer = e.getPlayer();
        if (!(e.getRightClicked() instanceof Player mediaTarget)) return;

        UUID glowingTarget = activeGlows.get(observer.getUniqueId());
        if (glowingTarget != null && glowingTarget.equals(mediaTarget.getUniqueId())) {
            String link = plugin.getMediaManager().getLink(mediaTarget.getUniqueId());

            // Sound effects
            observer.playSound(observer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            mediaTarget.playSound(mediaTarget.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

            // Send clickable link to observer
            Component header = ChatUtils.toComponent("§6§lNaturalSMP §8» §aLink Channel " + mediaTarget.getName() + ":");
            observer.sendMessage(header);

            if (link.startsWith("http")) {
                Component clickable = Component.text(link)
                        .color(NamedTextColor.AQUA)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(link));
                observer.sendMessage(clickable);
            } else {
                observer.sendMessage(ChatUtils.toComponent("§b" + link));
            }

            // Notify media player via Action Bar
            mediaTarget.sendActionBar(ChatUtils.toComponent("§e" + observer.getName() + " §asedang melihat link channel-mu!"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID quittingId = e.getPlayer().getUniqueId();

        // If quitting player was an observer, remove their glow entry
        UUID targetId = activeGlows.remove(quittingId);
        if (targetId != null) {
            Player target = Bukkit.getPlayer(targetId);
            if (target != null && target.isOnline()) {
                sendGlowPacket(e.getPlayer(), target, false);
            }
        }

        // If quitting player was a media target, clean up all observers watching them
        activeGlows.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(quittingId)) {
                Player obs = Bukkit.getPlayer(entry.getKey());
                if (obs != null && obs.isOnline()) {
                    sendGlowPacket(obs, e.getPlayer(), false);
                }
                return true;
            }
            return false;
        });
    }

    private void validateLineOfSightAndGlow(Player observer, Player target) {
        if (observer.hasLineOfSight(target) && observer.getLocation().distanceSquared(target.getLocation()) <= 4.0) {
            activeGlows.put(observer.getUniqueId(), target.getUniqueId());
            sendGlowPacket(observer, target, true);
        }
    }

    private boolean isMedia(Player p) {
        try {
            User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
            if (user == null) return false;
            for (InheritanceNode node : user.getNodes(NodeType.INHERITANCE)) {
                String group = node.getGroupName().toLowerCase();
                if (group.contains("youtube") || group.contains("tiktok")) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void sendGlowPacket(Player observer, Player target, boolean glowing) {
        if (!observer.isOnline() || !target.isOnline()) return;

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, target.getEntityId());

        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(target).deepClone();

        byte mask = 0;
        if (watcher.hasIndex(0)) {
            mask = (byte) watcher.getByte(0);
        }

        if (glowing) {
            mask |= 0x40;
        } else {
            mask &= ~0x40;
        }

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
            if (entity instanceof Player && entity != player && player.hasLineOfSight(entity)) {
                org.bukkit.util.Vector direction = player.getLocation().getDirection();
                org.bukkit.util.Vector toEntity = entity.getLocation().toVector()
                        .subtract(player.getLocation().toVector()).normalize();
                if (direction.dot(toEntity) > 0.8D) {
                    return entity;
                }
            }
        }
        return null;
    }
}
