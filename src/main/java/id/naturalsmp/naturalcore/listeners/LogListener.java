package id.naturalsmp.naturalcore.listeners;

import id.naturalsmp.naturalcore.utility.NaturalLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class LogListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // We don't want the full renderer because it appends the Display
        // prefix/name/tier again
        String finalOutput = PlainTextComponentSerializer.plainText().serialize(event.message());

        String formatInfo = "";
        id.naturalsmp.naturalcore.chat.ChatColorManager colorManager = id.naturalsmp.naturalcore.NaturalCore
                .getInstance().getChatColorManager();
        if (colorManager != null && player.hasPermission("naturalsmp.chat.color")) {
            String colorCode = colorManager.getPlayerColor(player).replace("&", "");
            String colorName = switch (colorCode) {
                case "0" -> "black";
                case "1" -> "dark_blue";
                case "2" -> "dark_green";
                case "3" -> "dark_aqua";
                case "4" -> "dark_red";
                case "5" -> "dark_purple";
                case "6" -> "gold";
                case "7" -> "gray";
                case "8" -> "dark_gray";
                case "9" -> "blue";
                case "a" -> "green";
                case "b" -> "aqua";
                case "c" -> "red";
                case "d" -> "light_purple";
                case "e" -> "yellow";
                case "f" -> "white";
                default -> colorCode;
            };

            String font = colorManager.getPlayerFont(player);
            boolean bold = colorManager.isBold(player);
            boolean italic = colorManager.isItalic(player);

            java.util.List<String> formats = new java.util.ArrayList<>();
            if (bold)
                formats.add("bold");
            if (italic)
                formats.add("italic");
            formats.add("font: { " + (font != null ? font.toLowerCase() : "default") + " }");

            formatInfo = " [color chat: { " + colorName + " }, format: { " + String.join(", ", formats) + " }]";
        }

        String prefix = "";
        if (id.naturalsmp.naturalcore.NaturalCore.getInstance().getVaultManager() != null &&
                id.naturalsmp.naturalcore.NaturalCore.getInstance().getVaultManager().getChat() != null) {
            prefix = id.naturalsmp.naturalcore.NaturalCore.getInstance().getVaultManager().getChat()
                    .getPlayerPrefix(player);
        }

        String tier = "";
        id.naturalsmp.naturalcore.tier.TierManager tm = id.naturalsmp.naturalcore.NaturalCore.getInstance()
                .getTierManager();
        if (tm != null) {
            tier = tm.getPlayerTierId(player);
        }

        NaturalLogger.getInstance().logChat(player.getName(), prefix, tier, formatInfo, finalOutput);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage(); // contains the initial slash, e.g. "/shop"
        String[] args = message.substring(1).split(" ");
        String cmd = args[0].toLowerCase();

        // 1. Private Chat Check
        if (cmd.equals("msg") || cmd.equals("tell") || cmd.equals("w") || cmd.equals("whisper") || cmd.equals("pm")
                || cmd.equals("reply") || cmd.equals("r") || cmd.equals("tc") || cmd.equals("teamchat")) {
            // For reply or team chat, target might be inferred, we log what they typed
            NaturalLogger.getInstance().logPrivateChat(player.getName(), "Somebody/Team", message);
            return;
        }

        // 2. Admin Check
        boolean isAdminCommand = false;
        if (player.hasPermission("naturalsmp.admin") || player.isOp()) {
            isAdminCommand = true;
        } else if (cmd.equals("give") || cmd.equals("effect") || cmd.equals("vanish") || cmd.equals("v")
                || cmd.equals("god") || cmd.equals("staff") || cmd.equals("sm") || cmd.equals("staffmode")
                || cmd.equals("sc") || cmd.equals("staffchat")) {
            isAdminCommand = true;
        }

        if (isAdminCommand) {
            NaturalLogger.getInstance().logAdmin(player.getName(), message);
        } else {
            // 3. Normal Command Check
            NaturalLogger.getInstance().logCommand(player.getName(), message);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NaturalLogger.getInstance().logConnection(player.getName(), player.getUniqueId(), "JOINED", null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String legacyMsg = "";
        if (event.quitMessage() != null) {
            legacyMsg = PlainTextComponentSerializer.plainText().serialize(event.quitMessage());
        }
        NaturalLogger.getInstance().logConnection(player.getName(), player.getUniqueId(), "QUIT", legacyMsg);
    }
}
