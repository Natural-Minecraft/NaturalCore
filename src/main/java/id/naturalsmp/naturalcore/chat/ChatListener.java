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
        private static final java.util.Map<java.util.UUID, Boolean> tierEditMode = new java.util.HashMap<>();

        public static void setSearchMode(java.util.UUID uuid) {
                searchMode.put(uuid, true);
        }

        public static void setTierEditMode(java.util.UUID uuid) {
                tierEditMode.put(uuid, true);
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onChat(AsyncChatEvent event) {
                Player player = event.getPlayer();

                // CHAT GAME ANSWER CHECK
                ChatGameManager gameManager = plugin.getChatGameManager();
                if (gameManager != null && gameManager.hasActiveGame()) {
                        String plainMsg = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                                        .plainText().serialize(event.message());
                        if (gameManager.tryAnswer(player, plainMsg)) {
                                event.setCancelled(true);
                                return;
                        }
                }

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

                // TIER EDIT MODE INTERCEPTION
                if (tierEditMode.containsKey(player.getUniqueId())) {
                        event.setCancelled(true);
                        tierEditMode.remove(player.getUniqueId());

                        String input = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                                        .plainText().serialize(event.message());

                        Bukkit.getScheduler().runTask(plugin, () -> {
                                if (input.equalsIgnoreCase("cancel")) {
                                        player.sendMessage(ChatUtils.toComponent("&#FF5555&l✘ &cEdit dibatalkan."));
                                        return;
                                }

                                try {
                                        double value = Double.parseDouble(input);
                                        id.naturalsmp.naturalcore.tier.TierEditorGUI.EditContext context = id.naturalsmp.naturalcore.tier.TierEditorGUI
                                                        .getContext(player.getUniqueId());

                                        if (context != null) {
                                                id.naturalsmp.naturalcore.tier.TierManager tm = plugin.getTierManager();
                                                id.naturalsmp.naturalcore.tier.TierManager.Tier tier = tm
                                                                .getTier(context.level);

                                                if (tier != null) {
                                                        double curMoney = tier.reqMoney;
                                                        int curKills = tier.reqKills;

                                                        if (context.type == id.naturalsmp.naturalcore.tier.TierEditorGUI.EditType.MONEY) {
                                                                curMoney = value;
                                                        } else {
                                                                curKills = (int) value;
                                                        }

                                                        tm.updateTierRequirement(context.level, curMoney, curKills);
                                                        player.sendMessage(ChatUtils.toComponent(
                                                                        "&#55FF55&l✔ &aRequirement diperbarui untuk level &e"
                                                                                        + context.level));

                                                        // Re-open editor
                                                        new id.naturalsmp.naturalcore.tier.TierEditorGUI(plugin,
                                                                        context.level).openGUI(player);
                                                }
                                        }
                                } catch (NumberFormatException e) {
                                        player.sendMessage(ChatUtils.toComponent(
                                                        "&#FF5555&l✘ &cInput tidak valid! Harap masukkan angka."));
                                }
                        });
                        return;
                }
                if (!ConfigUtils.getBoolean("chat.enabled"))
                        return;

                // Get the raw message as text.
                String messageText = legacySection.serialize(event.message());

                // Apply ChatColor settings
                ChatColorManager colorManager = plugin.getChatColorManager();
                if (colorManager != null && player.hasPermission("naturalsmp.chat.color")) {
                        String color = colorManager.getPlayerColor(player);
                        String font = colorManager.getPlayerFont(player);
                        boolean bold = colorManager.isBold(player);
                        boolean italic = colorManager.isItalic(player);

                        // Apply font
                        messageText = colorManager.applyFont(messageText, font);

                        // Prepend formatting
                        StringBuilder formatted = new StringBuilder();
                        formatted.append(color);
                        if (bold)
                                formatted.append("&l");
                        if (italic)
                                formatted.append("&o");
                        formatted.append(messageText);

                        messageText = formatted.toString();
                }

                // 1. Colorize Support
                if (player.hasPermission("naturalsmp.chat.color")) {
                        messageText = ChatUtils.colorize(messageText);
                }

                // Emoji parsing moved to NaturalFun

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
                // Pre-compile pattern for better performance (though method-local here, could
                // be static class field)
                // Match @ followed by word characters (A-Za-z0-9_)
                // We use a broader regex to catch potential names, then validate
                Pattern mentionPattern = Pattern.compile("(?i)@([a-zA-Z0-9_]+)");

                // Helper for text replacement
                TextReplacementConfig config = TextReplacementConfig.builder()
                                .match(mentionPattern)
                                .replacement((result, builder) -> {
                                        String tagName = result.group(1);

                                        // 1. Check @everyone
                                        if (tagName.equalsIgnoreCase("everyone")) {
                                                if (sender.hasPermission("naturalsmp.mention.everyone")) {
                                                        notifyEveryone(sender);
                                                        return ChatUtils.toComponent(
                                                                        "<gradient:#ff0000:#8b0000><bold>@everyone</bold></gradient>")
                                                                        .hoverEvent(HoverEvent.showText(ChatUtils
                                                                                        .toComponent("&cBroadcast to all players")));
                                                }
                                                return builder;
                                        }

                                        // 2. Check @here
                                        if (tagName.equalsIgnoreCase("here")) {
                                                if (sender.hasPermission("naturalsmp.mention.here")) {
                                                        notifyHere(sender);
                                                        return ChatUtils.toComponent(
                                                                        "<gradient:#ffff55:#ffaa00><bold>@here</bold></gradient>")
                                                                        .hoverEvent(HoverEvent.showText(ChatUtils
                                                                                        .toComponent("&eBroadcast to nearby players")));
                                                }
                                                return builder;
                                        }

                                        // 3. Check specific player
                                        Player target = Bukkit.getPlayerExact(tagName);
                                        // Fallback: Try match by display name (strip color) or partial match?
                                        // For now, strict match or closest match to avoid spam
                                        if (target == null) {
                                                // Try searching online players by name (case-insensitive)
                                                for (Player p : Bukkit.getOnlinePlayers()) {
                                                        if (p.getName().equalsIgnoreCase(tagName)) {
                                                                target = p;
                                                                break;
                                                        }
                                                        // Support tagging by DisplayName (stripped)
                                                        String simpleDisplay = ChatUtils.stripColor(p.getDisplayName());
                                                        if (simpleDisplay.equalsIgnoreCase(tagName)) {
                                                                target = p;
                                                                break;
                                                        }
                                                }
                                        }

                                        if (target != null) {
                                                // Send notification to TARGET only
                                                notifyPlayer(sender, target);

                                                // Return styled component with Premium Gradient
                                                return ChatUtils.toComponent("<gradient:#ffd700:#ffa500>@"
                                                                + target.getName() + "</gradient>")
                                                                .hoverEvent(HoverEvent.showText(
                                                                                ChatUtils.toComponent(
                                                                                                "&8&m------------------------&r\n"
                                                                                                                +
                                                                                                                "&6&lUser Info\n"
                                                                                                                +
                                                                                                                "&7Name: &f"
                                                                                                                + target.getName()
                                                                                                                + "\n" +
                                                                                                                "&7Ping: &a"
                                                                                                                + target.getPing()
                                                                                                                + "ms\n"
                                                                                                                +
                                                                                                                "\n" +
                                                                                                                "&e&lKLIK UNTUK PM\n"
                                                                                                                +
                                                                                                                "&8&m------------------------")))
                                                                .clickEvent(ClickEvent.suggestCommand(
                                                                                "/msg " + target.getName() + " "));
                                        }

                                        // No match? Return original text
                                        return builder;
                                })
                                .build();

                return message.replaceText(config);
        }

        private void notifyPlayer(Player sender, Player target) {
                // Avoid self-notification
                if (sender.equals(target))
                        return;

                target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                target.sendActionBar(
                                ChatUtils.toComponent("<gradient:#ffd700:#ffa500><bold>TAG!</bold></gradient> <yellow>"
                                                + sender.getName() + " <gray>tagged you!"));
        }

        private void notifyEveryone(Player sender) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!p.equals(sender)) {
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.2f);
                                p.sendActionBar(ChatUtils.toComponent(
                                                "<gradient:#ff0000:#8b0000><bold>@everyone</bold></gradient> <gray>from <white>"
                                                                + sender.getName()));
                        }
                }
        }

        private void notifyHere(Player sender) {
                // Radius e.g. 50 blocks or World? InteractiveChat usually does World or Radius.
                // Let's do Radius 50 for "Here"
                double radius = 100.0;
                for (Player p : sender.getWorld().getPlayers()) {
                        if (!p.equals(sender)
                                        && p.getLocation().distanceSquared(sender.getLocation()) <= radius * radius) {
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.5f);
                                p.sendActionBar(ChatUtils.toComponent(
                                                "<gradient:#ffff55:#ffaa00><bold>@here</bold></gradient> <gray>from <white>"
                                                                + sender.getName()));
                        }
                }
        }

        private Component processPlaceholders(Player player, Component message) {
                // [item]
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && item.getType() != Material.AIR) {
                        UUID id = ChatSnapshotManager.createItemSnapshot(player.getName(), item);
                        Component itemDisplay = item.displayName();
                        if (item.getAmount() > 1) {
                                itemDisplay = itemDisplay.append(
                                                Component.text(" x" + item.getAmount()).color(NamedTextColor.GRAY));
                        }

                        message = message.replaceText(TextReplacementConfig.builder()
                                        .match(Pattern.compile("(?i)\\[(item|i)\\]"))
                                        .replacement(aestheticBracket(itemDisplay
                                                        .hoverEvent(createSafeHover(item))
                                                        .clickEvent(ClickEvent
                                                                        .runCommand("/chatview " + id + " item"))))
                                        .build());
                }

                // [inv]
                if (messageContains(message, "[inv]")) {
                        UUID id = ChatSnapshotManager.createInventorySnapshot(player);
                        message = message.replaceText(TextReplacementConfig.builder()
                                        .match(Pattern.compile("(?i)\\[inv\\]"))
                                        .replacement(aestheticBracket(Component.text("Inventory")
                                                        .color(NamedTextColor.GREEN)
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
                                        .match(Pattern.compile("(?i)\\[ender\\]"))
                                        .replacement(aestheticBracket(Component.text("Enderchest")
                                                        .color(NamedTextColor.LIGHT_PURPLE)
                                                        .hoverEvent(HoverEvent.showText(Component.text(
                                                                        "Click to view enderchest",
                                                                        NamedTextColor.GRAY)))
                                                        .clickEvent(ClickEvent
                                                                        .runCommand("/chatview " + id + " ender"))))
                                        .build());
                }

                // [pos]
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("(?i)\\[pos\\]"))
                                .replacement(aestheticBracket(Component.text(
                                                player.getLocation().getBlockX() + ", " +
                                                                player.getLocation().getBlockY() + ", " +
                                                                player.getLocation().getBlockZ())
                                                .color(NamedTextColor.GREEN)))
                                .build());

                // [ping]
                int ping = player.getPing();
                NamedTextColor pingColor = (ping < 100) ? NamedTextColor.GREEN
                                : (ping < 200) ? NamedTextColor.YELLOW : NamedTextColor.RED;
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("(?i)\\[ping\\]"))
                                .replacement(aestheticBracket(Component.text(ping + "ms").color(pingColor)))
                                .build());

                // [money]
                double bal = 0;
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getEconomy() != null) {
                        bal = plugin.getVaultManager().getEconomy().getBalance(player);
                }
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("(?i)\\[money\\]"))
                                .replacement(aestheticBracket(Component.text("$" + ChatUtils.format(bal))
                                                .color(NamedTextColor.GOLD)))
                                .build());

                // [time]
                long time = player.getWorld().getTime();
                String timeStr = String.format("%02d:%02d", (time / 1000 + 6) % 24, (time % 1000) * 60 / 1000);
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("(?i)\\[time\\]"))
                                .replacement(aestheticBracket(Component.text(timeStr).color(NamedTextColor.AQUA)))
                                .build());

                return message;
        }

        private Component processCustomFormatters(Component message) {
                // Web Links (naturalsmp.net)
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("(?i)\\b(?:(?:www|store|vote|discord|instagram|dc|ig|tiktok|tt|links)\\.)?naturalsmp\\.net\\b"))
                                .replacement((result, builder) -> {
                                        String matched = result.group();
                                        return Component.text(matched)
                                                        .color(NamedTextColor.BLUE)
                                                        .decorate(net.kyori.adventure.text.format.TextDecoration.UNDERLINED)
                                                        .clickEvent(ClickEvent.openUrl("https://" + matched))
                                                        .hoverEvent(HoverEvent.showText(Component.text("Klik untuk membuka link!", NamedTextColor.GRAY)));
                                })
                                .build());

                // [match:text]
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("(?i)\\[match:([^\\]]+)\\]"))
                                .replacement((result, builder) -> {
                                        String text = result.group(1);
                                        return aestheticBracket(Component.text(text).color(NamedTextColor.WHITE));
                                })
                                .build());

                // [/command]
                message = message.replaceText(TextReplacementConfig.builder()
                                .match(Pattern.compile("\\[/([^\\]]+)\\]"))
                                .replacement((result, builder) -> {
                                        String cmd = result.group(1);
                                        return aestheticBracket(Component.text("/" + cmd).color(NamedTextColor.YELLOW)
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
                                        return aestheticBracket(Component.text(text).color(NamedTextColor.YELLOW)
                                                        .hoverEvent(
                                                                        HoverEvent.showText(Component.text(
                                                                                        "Informasi Tambahan",
                                                                                        NamedTextColor.GRAY))));
                                })
                                .build());

                return message;
        }

        private Component aestheticBracket(Component inner) {
                return Component.text("[").color(NamedTextColor.DARK_GRAY)
                                .append(inner)
                                .append(Component.text("]").color(NamedTextColor.DARK_GRAY));
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
                String customSuffix = (plugin.getSuffixManager() != null) ? plugin.getSuffixManager().getPlayerSuffix(player)
                                : "";
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

                // Apply Placeholders
                prefix = ChatUtils.setPlaceholders(player, prefix);
                customSuffix = ChatUtils.setPlaceholders(player, customSuffix);
                tierSuffix = ChatUtils.setPlaceholders(player, tierSuffix);

                // Format: [prefix][name][customSuffix][tierSuffix]
                Component playerPart = legacySection.deserialize(ChatUtils.colorize(prefix))
                                .append(Component.text(player.getName()).hoverEvent(HoverEvent.showText(playerHover))
                                                .color(NamedTextColor.WHITE))
                                .append(legacySection.deserialize(ChatUtils.colorize(customSuffix)))
                                .append(legacySection.deserialize(ChatUtils.colorize(tierSuffix)));

                return playerPart.append(Component.text(" » ").color(NamedTextColor.DARK_GRAY))
                                .append(processedMessage);
        }
}