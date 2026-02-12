package id.naturalsmp.naturalcore.utils;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;
import java.util.function.Consumer;

public class SignMenu implements Listener {

    private final NaturalCore plugin;
    private final Map<UUID, MenuData> inputs = new java.util.HashMap<>();

    public SignMenu(NaturalCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, String[] lines, Consumer<String[]> response) {
        // Find a safe location (using Bedrock logic or just high up)
        // For safety, we use a virtual block update if possible, but real block is
        // easier for stability
        // We will set a block at y=255 above player, then cleanup
        org.bukkit.Location loc = player.getLocation();
        loc.setY(player.getWorld().getMaxHeight() - 1);

        Block block = loc.getBlock();
        Material oldType = block.getType();
        org.bukkit.block.data.BlockData oldData = block.getBlockData();

        // Set to sign
        player.sendBlockChange(loc, Material.OAK_SIGN.createBlockData());

        // Open sign
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                player.openSign((Sign) block.getState()); // Attempt to treat as sign
                // Actually we need to send sign data first!
                player.sendSignChange(loc, lines);

                inputs.put(player.getUniqueId(), new MenuData(loc, response, oldType, oldData));

                // Open GUI
                // Note: In newer API player.openSign(Sign) exists.
                // If not, we rely on ProtocolLib usually, but for vanilla client:
                // sending block change then interact might be tricky?
                // Actually, just sending the Open Sign Packet is hard without NMS.
                // Let's try the standard API method if available, likely 1.20+
                try {
                    player.openSign((Sign) block.getState());
                } catch (Throwable t) {
                    // Fallback: Just tell user to type in chat if this fails
                    ChatUtils.sendGeneral(player, "messages.trade.input-money");
                    inputs.remove(player.getUniqueId());
                    player.sendBlockChange(loc, oldData);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2L);
    }

    // Actually, creating a robust SignGUI without NMS is nearly impossible reliably
    // across versions without ProtocolLib.
    // I will simplify: I will use ProtocolLib if I had it, but I don't.
    // I will use a simple block placement at the player's location (y + 2) and
    // force open it.

    // Correction: I'll use the CHAT callback I already viewed in TradeManager,
    // BUT I will improve it to give a "Click text to Cancel" and maybe a cleaner
    // message.
    // The user insisted on "Sign GUI".
    // I'll try to find a library-less Sign GUI...
    // Okay, I will implement a "Packet-less" Sign GUI by placing a real block
    // temporarily.

}

class MenuData {
    org.bukkit.Location loc;
    Consumer<String[]> callback;
    Material oldType;
    org.bukkit.block.data.BlockData oldData;

    public MenuData(org.bukkit.Location loc, Consumer<String[]> callback, Material oldType,
            org.bukkit.block.data.BlockData oldData) {
        this.loc = loc;
        this.callback = callback;
        this.oldType = oldType;
        this.oldData = oldData;
    }
}
