package id.naturalsmp.naturalcore.hud;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import id.naturalsmp.naturalcore.NaturalCore;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts Action Bar packets sent by other plugins (like MMOItems,
 * MythicLib)
 * and routes them through NaturalCore's HUD system with premium aesthetics.
 *
 * In Minecraft 1.19.4+, action bars are sent via SYSTEM_CHAT with overlay=true,
 * NOT via SET_ACTION_BAR_TEXT.
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

        // =====================================================================
        // Listener 1: SYSTEM_CHAT — Modern 1.19.4+ action bar packet
        // MMOItems/MythicLib sends action bar via this in 1.21
        // The packet has a boolean "overlay" field — true = action bar
        // =====================================================================
        protocolManager.addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isCancelled())
                    return;

                Player player = event.getPlayer();
                if (player == null)
                    return;

                // Check if this is an action bar (overlay = true)
                try {
                    Boolean isOverlay = event.getPacket().getBooleans().read(0);
                    if (isOverlay == null || !isOverlay) {
                        return; // Regular chat message, not action bar
                    }
                } catch (Exception e) {
                    return; // Can't read overlay flag, skip
                }

                // NaturalCore's own packets — let them through
                if (bypassPlayers.contains(player.getUniqueId())) {
                    return;
                }

                // This is an action bar from another plugin — intercept!
                event.setCancelled(true);

                String text = extractTextFromSystemChat(event);
                if (text != null && !text.isBlank()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        hudManager.showNotification(player, text, 40);
                    });
                }
            }
        });

        // =====================================================================
        // Listener 2: SET_ACTION_BAR_TEXT — Legacy fallback
        // Some plugins might still use this on older protocol versions
        // =====================================================================
        try {
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

                    if (bypassPlayers.contains(player.getUniqueId())) {
                        return;
                    }

                    event.setCancelled(true);

                    String text = extractTextFromActionBar(event);
                    if (text != null && !text.isBlank()) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            hudManager.showNotification(player, text, 40);
                        });
                    }
                }
            });
        } catch (Exception e) {
            plugin.getLogger().info("[HUD] SET_ACTION_BAR_TEXT not available, using SYSTEM_CHAT only.");
        }

        plugin.getLogger()
                .info("[HUD] ProtocolLib action bar interception registered (SYSTEM_CHAT + SET_ACTION_BAR_TEXT).");
    }

    /**
     * Extract text from SYSTEM_CHAT packet (modern 1.19.4+)
     */
    private String extractTextFromSystemChat(PacketEvent event) {
        // Method 1: Try Adventure Component directly (Paper)
        try {
            var adventureModifier = event.getPacket().getModifier()
                    .withType(net.kyori.adventure.text.Component.class);
            if (adventureModifier.size() > 0) {
                net.kyori.adventure.text.Component comp = (net.kyori.adventure.text.Component) adventureModifier
                        .read(0);
                if (comp != null) {
                    return LegacyComponentSerializer.legacySection().serialize(comp)
                            .replace("§", "&");
                }
            }
        } catch (Exception ignored) {
        }

        // Method 2: Try WrappedChatComponent (JSON)
        try {
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent != null) {
                String json = chatComponent.getJson();
                if (json != null && !json.isEmpty()) {
                    net.kyori.adventure.text.Component adventureComp = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
                            .gson().deserialize(json);
                    return LegacyComponentSerializer.legacySection().serialize(adventureComp)
                            .replace("§", "&");
                }
            }
        } catch (Exception ignored) {
        }

        // Method 3: Raw string
        try {
            var strings = event.getPacket().getStrings();
            if (strings.size() > 0) {
                return strings.read(0);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Extract text from SET_ACTION_BAR_TEXT packet (legacy)
     */
    private String extractTextFromActionBar(PacketEvent event) {
        try {
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent != null) {
                String json = chatComponent.getJson();
                if (json != null && !json.isEmpty()) {
                    net.kyori.adventure.text.Component adventureComp = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
                            .gson().deserialize(json);
                    return LegacyComponentSerializer.legacySection().serialize(adventureComp)
                            .replace("§", "&");
                }
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
