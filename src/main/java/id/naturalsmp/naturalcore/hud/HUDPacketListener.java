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
 * Intercepts Action Bar packets sent by other plugins (like MMOItems)
 * and routes them through NaturalCore's HUD system instead.
 */
public class HUDPacketListener {

    private final NaturalCore plugin;
    private final HUDManager hudManager;
    private ProtocolManager protocolManager;

    // UUIDs of players currently receiving a NaturalCore HUD update.
    // Packets sent while a UUID is in this set are "ours" and should pass through.
    private final Set<UUID> bypassPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public HUDPacketListener(NaturalCore plugin, HUDManager hudManager) {
        this.plugin = plugin;
        this.hudManager = hudManager;
    }

    /**
     * Mark a player as currently receiving a NaturalCore action bar.
     * Call this RIGHT BEFORE sending player.sendActionBar() from NaturalCore.
     */
    public void markBypassing(UUID playerId) {
        bypassPlayers.add(playerId);
    }

    /**
     * Unmark a player after NaturalCore's action bar has been sent.
     * Call this RIGHT AFTER sending player.sendActionBar() from NaturalCore.
     */
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

                // If this packet was sent by NaturalCore itself, let it through
                if (bypassPlayers.contains(player.getUniqueId())) {
                    return;
                }

                // This packet was sent by another plugin (e.g. MMOItems, MythicLib)
                // Cancel the original packet and route it through our HUD system
                event.setCancelled(true);

                // Extract the text content from the packet
                String text = extractText(event);
                if (text != null && !text.isBlank()) {
                    // Show it as a notification for ~40 ticks (2 seconds)
                    hudManager.showNotification(player, text, 40);
                }
            }
        });

        plugin.getLogger().info("[HUD] ProtocolLib action bar interception registered.");
    }

    /**
     * Extracts plain text from the action bar packet.
     * SET_ACTION_BAR_TEXT holds a WrappedChatComponent (JSON chat).
     */
    private String extractText(PacketEvent event) {
        try {
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent != null) {
                String json = chatComponent.getJson();
                if (json != null) {
                    // Parse the JSON chat component to plain text via Adventure
                    net.kyori.adventure.text.Component adventureComponent = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
                            .gson().deserialize(json);
                    return PlainTextComponentSerializer.plainText().serialize(adventureComponent);
                }
            }
        } catch (Exception e) {
            // Fallback: try reading adventure components directly
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
        }
        return null;
    }

    public void unregister() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(plugin);
        }
    }
}
