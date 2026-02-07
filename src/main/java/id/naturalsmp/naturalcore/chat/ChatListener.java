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
        private final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();
        private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();

        public ChatListener(NaturalCore plugin) {
                this.plugin = plugin;
        }

        private static final java.util.Map<java.util.UUID, Boolean> searchMode = new java.util.HashMap<>();

        public static void setSearchMode(java.util.UUID uuid) {
                searchMode.put(uuid, true);
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onChat(AsyncChatEvent event) {
                Player player = event.getPlayer();

                // SEARCH MODE INTERCEPTION
                if (searchMode.containsKey(player.getUniqueId())) {
                        event.setCancelled(true);
                        searchMode.remove(player.getUniqueId());

                        String query = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                                        .plainText().serialize(event.message());

                        // Run on main thread because GUI opening must be sync
                        Bukkit.getScheduler().runTask(plugin, () -> {
                                if (query.equalsIgnoreCase("cancel")) {
                                        player.sendMessage(
                                                        ChatUtils.toComponent("&#FF5555&l✘ &cPencarian dibatalkan."));
                                        new id.naturalsmp.naturalcore.utility.TutorialGUI(plugin).openGUI(player, null);
                                } else {
                                        new id.naturalsmp.naturalcore.utility.TutorialGUI(plugin).openGUI(player,
                                                        query);
                                }
                        });
                        return;
                }
                if (!ConfigUtils.getBoolean("chat.enabled"))
                        return;

                // Get the raw message as text. We use legacySection() because it's
                // the most standard way to get text with current formatting.
                String messageText = legacySection.serialize(event.message());

                // 1. Colorize & Emoji Support (Legacy conversion for now)
                if (player.hasPermission("naturalsmp.chat.color")) {
                        messageText = ChatUtils.colorize(messageText);
                }

                if (EmojiManager.getInstance() != null) {
                        messageText = EmojiManager.getInstance().parseEmojis(player, messageText);
                }

                // Convert back to Component
                Component message = legacySection.deserialize(messageText);

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
                                                // Premium Notifications
                                                target.playSound(target.getLocation(),
                                                                Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                                                target.sendActionBar(ChatUtils.colorize(
                                                                "&6&lTag! &e" + sender.getName() + " &7menyapa Anda!"));

                                                target.sendTitle(
                                                                ChatUtils.colorize("&6&lTAGGED!"),
                                                                ChatUtils.colorize("&7Oleh &f" + sender.getName()),
                                                                10, 40, 10);

                                                return Component.text(mentionTag)
                                                                .color(NamedTextColor.GOLD)
                                                                .hoverEvent(HoverEvent.showText(
                                                                                Component.text("Ping: "
                                                                                                + target.getPing()
                                                                                                + "ms\n§eKlik untuk kirim pesan pribadi!",
                                                                                                NamedTextColor.GRAY)))
                                                                .clickEvent(ClickEvent.suggestCommand(
                                                                                "/msg " + target.getName() + " "));
                                        })
                                        .build();

                        message = message.replaceText(config);
                }
                return message;
        }

        private Component processPlaceholders(Player player, Component message) {
                // [item]
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && item.getType() != Material.AIR) {
                        UUID id = ChatSnapshotManager.createItemSnapshot(player.getName(), item);
                        message = message.replaceText(TextReplacementConfig.builder()
                                        .match(Pattern.compile("\\[(item|i)\\]"))
                                        .replacement(blueBracket(Component
                                                        .text(ChatUtils.stripColor(legacySection
                                                                        .serialize(item.displayName()))
                                                                        .replace("[", "")
                                                                        .replace("]", "")
                                                                        + " x"
                                                                        + item.getAmount())
                                                        .color(NamedTextColor.YELLOW)
                                                        .hoverEvent(createSafeHover(item))
                                                        .clickEvent(ClickEvent
                                                                        .runCommand("/chatview " + id + " item"))))
                                        .build());
                }

                // [inv]
                if (messageContains(message, "[inv]")) {
                        UUID id = ChatSnapshotManager.createInventorySnapshot(player);
                        message = message.replaceText(TextReplacementConfig.builder()
                                        .match(Pattern.compile("\\[inv\\]"))
                                        .replacement(blueBracket(Component.text("Inventory")
                                                        .color(NamedTextColor.YELLOW)
                                                        .hoverEvent(HoverEvent.showText(Component.text(
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
                return Component.text("[").color(NamedTextColor.AQUA)
                                .append(inner)
                                .append(Component.text("]").color(NamedTextColor.AQUA));
        }

        private HoverEvent<?> createSafeHover(ItemStack original) {
                // If it's a version that might cause ViaVersion issues (1.20.5+ items),
                // we "clean" the item for the hover.
                ItemStack clean = original.clone();
                org.bukkit.inventory.meta.ItemMeta meta = clean.getItemMeta();
                if (meta != null) {
                        // Clear attribute modifiers which are the main cause of NPE in ViaVersion
                        // rewriters
                        if (meta.hasAttributeModifiers()) {
                                meta.getAttributeModifiers().keySet().forEach(meta::removeAttributeModifier);
                        }
                        clean.setItemMeta(meta);
                }
                return clean.asHoverEvent();
        }

        private boolean messageContains(Component message, String search) {
                return legacySection.serialize(message).contains(search);
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
                                                .append(legacySection.deserialize(ChatUtils.colorize(tier))));

                String prefix = "";
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getChat() != null) {
                        prefix = plugin.getVaultManager().getChat().getPlayerPrefix(player);
                }

                // USE LEGACY SECTION for colorized strings
                Component playerPart = legacySection.deserialize(ChatUtils.colorize(tag))
                                .append(legacySection.deserialize(ChatUtils.colorize(prefix)))
                                .append(Component.text(player.getName()).hoverEvent(HoverEvent.showText(playerHover))
                                                .color(NamedTextColor.WHITE))
                                .append(legacySection.deserialize(ChatUtils.colorize(tierSuffix)));

                return playerPart.append(Component.text(" » ").color(NamedTextColor.DARK_GRAY))
                                .append(processedMessage);
        }
}