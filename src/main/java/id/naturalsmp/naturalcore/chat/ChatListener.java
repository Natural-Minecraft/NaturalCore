package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

        private final NaturalCore plugin;
        private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

        public ChatListener(NaturalCore plugin) {
                this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onChat(AsyncChatEvent event) {
                if (!ConfigUtils.getBoolean("chat.enabled"))
                        return;

                Player player = event.getPlayer();
                Component originalMessage = event.message();
                String messageText = LegacyComponentSerializer.legacySection().serialize(originalMessage);

                // 1. Colorize & Emoji Support (Legacy conversion for now)
                if (player.hasPermission("naturalsmp.chat.color")) {
                        messageText = ChatUtils.colorize(messageText);
                }

                if (EmojiManager.getInstance() != null) {
                        messageText = EmojiManager.getInstance().parseEmojis(player, messageText);
                }

                Component message = LegacyComponentSerializer.legacySection().deserialize(messageText);

                // 2. Process Mentions
                message = processMentions(player, message);

                // 3. Process Interactive Placeholders
                message = processPlaceholders(player, message);

                // 4. Custom Formatters [/command] and [hover:text]
                message = processCustomFormatters(message);

                // 5. Final Formatting
                Component finalFormat = buildFinalFormat(player, message);

                event.renderer((source, sourceDisplayName, msg, viewer) -> finalFormat);
        }

        private Component processMentions(Player sender, Component message) {
                for (Player target : Bukkit.getOnlinePlayers()) {
                        String mentionTag = "@" + target.getName();

                        TextReplacementConfig config = TextReplacementConfig.builder()
                                        .match(Pattern.compile("(?i)" + Pattern.quote(mentionTag)))
                                        .replacement((result, builder) -> {
                                                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f,
                                                                2f);
                                                target.sendActionBar(ChatUtils
                                                                .colorize("&e" + sender.getName() + " mentions you!"));

                                                return Component.text(mentionTag)
                                                                .color(NamedTextColor.YELLOW)
                                                                .hoverEvent(HoverEvent.showText(
                                                                                Component.text("Ping: "
                                                                                                + target.getPing()
                                                                                                + "ms",
                                                                                                NamedTextColor.GRAY)));
                                        })
                                        .build();

                        message = message.replaceText(config);
                }
                return message;
        }

        private Component processPlaceholders(Player player, Component message) {
                // [item]
                if (messageContains(message, "[item]")) {
                        ItemStack item = player.getInventory().getItemInMainHand();
                        if (item != null && item.getType() != Material.AIR) {
                                Component itemComp = item.displayName().hoverEvent(item.asHoverEvent());
                                UUID id = ChatSnapshotManager.createInventorySnapshot(player.getName() + "'s Item",
                                                new ItemStack[] { item }); // Simple snapshot
                                message = message.replaceText(TextReplacementConfig.builder()
                                                .match(Pattern.compile("\\[item\\]"))
                                                .replacement(
                                                                blueBracket(
                                                                                Component
                                                                                                .text(ChatUtils
                                                                                                                .stripColor(LegacyComponentSerializer
                                                                                                                                .legacySection()
                                                                                                                                .serialize(item.displayName()))
                                                                                                                + " x"
                                                                                                                + item.getAmount())
                                                                                                .color(NamedTextColor.YELLOW)
                                                                                                .hoverEvent(item.asHoverEvent())
                                                                                                .clickEvent(ClickEvent
                                                                                                                .runCommand("/chatview "
                                                                                                                                + id
                                                                                                                                + " inv"))))
                                                .build());
                        }
                }

                // [inv]
                if (messageContains(message, "[inv]")) {
                        UUID id = ChatSnapshotManager.createInventorySnapshot(player.getName(),
                                        player.getInventory().getContents());
                        message = message.replaceText(TextReplacementConfig.builder()
                                        .match(Pattern.compile("\\[inv\\]"))
                                        .replacement(blueBracket(Component.text("Inventory")
                                                        .color(NamedTextColor.YELLOW)
                                                        .hoverEvent(
                                                                        HoverEvent.showText(Component.text(
                                                                                        "Click to view inventory",
                                                                                        NamedTextColor.GRAY)))
                                                        .clickEvent(ClickEvent.runCommand("/chatview " + id + " inv"))))
                                        .build());
                }

                // [ender]
                if (messageContains(message, "[ender]")) {
                        UUID id = ChatSnapshotManager.createEnderSnapshot(player.getName(),
                                        player.getEnderChest().getContents());
                        message = message.replaceText(TextReplacementConfig.builder()
                                        .match(Pattern.compile("\\[ender\\]"))
                                        .replacement(blueBracket(Component.text("Enderchest")
                                                        .color(NamedTextColor.YELLOW)
                                                        .hoverEvent(HoverEvent
                                                                        .showText(Component.text(
                                                                                        "Click to view enderchest",
                                                                                        NamedTextColor.GRAY)))
                                                        .clickEvent(ClickEvent
                                                                        .runCommand("/chatview " + id + " ender"))))
                                        .build());
                }

                // [pos]
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("\\[pos\\]"))
                                .replacement(
                                                blueBracket(
                                                                Component
                                                                                .text(player.getLocation().getBlockX()
                                                                                                + ", "
                                                                                                + player.getLocation()
                                                                                                                .getBlockY()
                                                                                                + ", "
                                                                                                + player.getLocation()
                                                                                                                .getBlockZ())
                                                                                .color(NamedTextColor.YELLOW)))
                                .build());

                // [ping]
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("\\[ping\\]"))
                                .replacement(blueBracket(
                                                Component.text(player.getPing() + "ms").color(NamedTextColor.YELLOW)))
                                .build());

                // [money]
                double bal = 0;
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getEconomy() != null) {
                        bal = plugin.getVaultManager().getEconomy().getBalance(player);
                }
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("\\[money\\]"))
                                .replacement(blueBracket(Component.text("$" + ChatUtils.format(bal))
                                                .color(NamedTextColor.YELLOW)))
                                .build());

                // [time]
                long time = player.getWorld().getTime();
                String timeStr = String.format("%02d:%02d", (time / 1000 + 6) % 24, (time % 1000) * 60 / 1000);
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("\\[time\\]"))
                                .replacement(blueBracket(Component.text(timeStr).color(NamedTextColor.YELLOW)))
                                .build());

                return message;
        }

        private Component processCustomFormatters(Component message) {
                // [/command]
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("\\[/([^\\]]+)\\]"))
                                .replacement((result, builder) -> {
                                        String cmd = result.group(1);
                                        return blueBracket(Component.text("/" + cmd).color(NamedTextColor.YELLOW)
                                                        .clickEvent(ClickEvent.suggestCommand("/" + cmd))
                                                        .hoverEvent(HoverEvent
                                                                        .showText(Component.text(
                                                                                        "Click to suggest command",
                                                                                        NamedTextColor.GRAY))));
                                })
                                .build());

                // [hover:text]
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("\\[hover:([^\\]]+)\\]"))
                                .replacement((result, builder) -> {
                                        String text = result.group(1);
                                        return blueBracket(Component.text(text).color(NamedTextColor.YELLOW)
                                                        .hoverEvent(
                                                                        HoverEvent.showText(Component.text(
                                                                                        "Informasi Tambahan",
                                                                                        NamedTextColor.GRAY))));
                                })
                                .build());

                return message;
        }

        private Component blueBracket(Component inner) {
                return Component.text("[").color(NamedTextColor.BLUE)
                                .append(inner)
                                .append(Component.text("]").color(NamedTextColor.BLUE));
        }

        private boolean messageContains(Component message, String search) {
                return LegacyComponentSerializer.legacySection().serialize(message).contains(search);
        }

        private Component buildFinalFormat(Player player, Component processedMessage) {
                String tag = "";
                if (plugin.getTagsManager() != null)
                        tag = plugin.getTagsManager().getPlayerTag(player);
                String tierSuffix = (plugin.getTierManager() != null) ? plugin.getTierManager().getPlayerSuffix(player)
                                : "";

                String rank = "Player";
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getChat() != null) {
                        rank = plugin.getVaultManager().getChat().getPrimaryGroup(player);
                }
                String tier = (plugin.getTierManager() != null) ? plugin.getTierManager().getPlayerTierId(player) : "1";

                Component playerHover = Component.text("Name: ").color(NamedTextColor.GRAY)
                                .append(Component.text(player.getName()).color(NamedTextColor.WHITE))
                                .append(Component.newline())
                                .append(Component.text("Rank: ").color(NamedTextColor.GRAY)
                                                .append(Component.text(rank).color(NamedTextColor.GOLD)))
                                .append(Component.newline())
                                .append(Component.text("Tier: ").color(NamedTextColor.GRAY)
                                                .append(Component.text(tier).color(NamedTextColor.AQUA)));

                String prefix = "";
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getChat() != null) {
                        prefix = plugin.getVaultManager().getChat().getPlayerPrefix(player);
                }

                Component playerPart = legacy.deserialize(ChatUtils.colorize(tag))
                                .append(legacy.deserialize(ChatUtils.colorize(prefix)))
                                .append(Component.text(player.getName()).hoverEvent(HoverEvent.showText(playerHover))
                                                .color(NamedTextColor.WHITE))
                                .append(legacy.deserialize(ChatUtils.colorize(tierSuffix)));

                return playerPart.append(Component.text(" » ").color(NamedTextColor.DARK_GRAY))
                                .append(processedMessage);
        }
}