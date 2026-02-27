package id.naturalsmp.naturalcore.hud;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import id.naturalsmp.naturalcore.NaturalCore;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts Action Bar packets sent by other plugins (like MMOItems,
 * MythicLib)
 * and routes them through NaturalCore's HUD system with premium aesthetics.
 */
public class HUDPacketListener {

    private final NaturalCore plugin;
    private final HUDManager hudManager;
    private ProtocolManager protocolManager;

    // UUIDs currently sending NaturalCore's own action bars — bypass interception
    private final Set<UUID> bypassPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public HUDPacketListener(NaturalCore plugin, HUDManager hudManager) {
        this.plugin = plugin;
        this.hudManager = hudManager;
    }

    public void markBypassing(UUID playerId) {
        bypassPlayers.add(playerId);
    }

    public void unmarkBypassing(UUID playerId) {
        bypassPlayers.remove(playerId);
    }

    public void register() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            plugin.getLogger().warning("[HUD] ProtocolLib not found! Action bar interception disabled.");
            return;
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.SET_ACTION_BAR_TEXT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isCancelled())
                    return;

                Player player = event.getPlayer();
                if (player == null)
                    return;

                // NaturalCore's own packets — let them through
                if (bypassPlayers.contains(player.getUniqueId())) {
                    return;
                }

                // Intercept: this packet is from another plugin (MMOItems, MythicLib, etc.)
                event.setCancelled(true);

                // Extract text and route to our notification system
                String text = extractText(event);
                if (text != null && !text.isBlank()) {
                    // Schedule on main thread to ensure thread safety
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        hudManager.showNotification(player, text, 40);
                    });
                }
            }
        });

        plugin.getLogger().info("[HUD] ProtocolLib action bar interception registered successfully.");
    }

    /**
     * Extracts plain text from the action bar packet.
     * Tries multiple methods for compatibility with different server versions.
     */
    private String extractText(PacketEvent event) {
        // Method 1: Try WrappedChatComponent (JSON chat)
        try {
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent != null) {
                String json = chatComponent.getJson();
                if (json != null && !json.isEmpty()) {
                    net.kyori.adventure.text.Component adventureComponent = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
                            .gson().deserialize(json);
                    return PlainTextComponentSerializer.plainText().serialize(adventureComponent);
                }
            }
        } catch (Exception ignored) {
        }

        // Method 2: Try Adventure Component directly (Paper servers)
        try {
            var adventureModifier = event.getPacket().getModifier()
                    .withType(net.kyori.adventure.text.Component.class);
            if (adventureModifier.size() > 0) {
                net.kyori.adventure.text.Component comp = (net.kyori.adventure.text.Component) adventureModifier
                        .read(0);
                if (comp != null) {
                    return PlainTextComponentSerializer.plainText().serialize(comp);
                }
            }
        } catch (Exception ignored) {
        }

        // Method 3: Raw string fallback
        try {
            var stringModifier = event.getPacket().getStrings();
            if (stringModifier.size() > 0) {
                return stringModifier.read(0);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public void unregister() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(plugin);
        }
    }
}
