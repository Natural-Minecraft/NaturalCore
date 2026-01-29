package id.naturalsmp.naturalcore.utils;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.economy.VaultManager;
import net.md_5.bungee.api.ChatColor; // Pastikan import Bungee API
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    // Regex untuk mendeteksi format &#123456
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###.##");

    private static final net.kyori.adventure.text.minimessage.MiniMessage MINI_MESSAGE = net.kyori.adventure.text.minimessage.MiniMessage
            .miniMessage();
    private static final net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer LEGACY_SERIALIZER = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
            .legacyAmpersand();

    /**
     * Mengubah kode warna (&a, &l) dan Hex Color (&#RRGGBB) menjadi warna asli.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty())
            return "";

        // Jika mengandung < (MiniMessage)
        if (message.contains("<")) {
            try {
                return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&',
                        net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                                .serialize(MINI_MESSAGE.deserialize(message)));
            } catch (Exception e) {
                // Fallback jika MiniMessage gagal (mungkin karena mixed legacy color)
                // Kita bersihkan dulu legacy color sebelum coba parsing ulang jika perlu,
                // tapi cara termudah adalah lanjut ke legacy colorization biasa.
            }
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            try {
                String hexCode = matcher.group(1);
                matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
            } catch (Exception e) {
                matcher.appendReplacement(buffer, "");
            }
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String serialize(net.kyori.adventure.text.Component component) {
        if (component == null)
            return "";
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .serialize(component);
    }

    /**
     * Mengubah string menjadi Component yang SANGAT aman untuk fase Login.
     * Menggunakan format § (legacy) dan mematikan semua fitur Adventure yang
     * kompleks.
     */
    public static net.kyori.adventure.text.Component toLoginSafeComponent(String message) {
        if (message == null || message.isEmpty())
            return net.kyori.adventure.text.Component.empty();

        // 1. Colorize with legacy handling
        String colored = colorize(message).replace("&", "§");

        // 2. Convert to component via legacy serializer to ensure it's "flat"
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .deserialize(colored);
    }

    /**
     * Mengubah string (Legacy atau MiniMessage) menjadi Adventure Component.
     */
    public static net.kyori.adventure.text.Component toComponent(String message) {
        if (message == null || message.isEmpty())
            return net.kyori.adventure.text.Component.empty();

        net.kyori.adventure.text.Component component;
        // Jika mengandung < (MiniMessage)
        if (message.contains("<")) {
            try {
                component = MINI_MESSAGE.deserialize(message);
            } catch (Exception e) {
                // Fallback ke legacy jika MiniMessage gagal
                component = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .deserialize(colorize(message).replace("&", "§"));
            }
        } else {
            // Fallback ke legacy colorization lalu ke component
            component = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                    .deserialize(colorize(message).replace("&", "§"));
        }

        // Flatten component to legacy for maximum compatibility (prevents
        // DecoderException in Login stage)
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .deserialize(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .serialize(component));
    }

    public static java.util.List<String> colorize(java.util.List<String> list) {
        if (list == null)
            return null;
        java.util.List<String> colored = new java.util.ArrayList<>();
        for (String s : list) {
            colored.add(colorize(s));
        }
        return colored;
    }

    /**
     * Menghapus semua kode warna dari string (untuk validasi atau log console).
     */
    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }

    /**
     * Format angka desimal (10000 -> 10,000).
     */
    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Mengganti placeholder %displayname% dan %player%
     * Mengambil Prefix/Suffix dari Vault (LuckPerms).
     */
    public static String formatMessage(Player p, String message) {
        if (message == null)
            return "";

        String prefix = "";
        String suffix = "";

        // 1. Ambil Data Vault dengan Safety Check
        try {
            NaturalCore plugin = NaturalCore.getInstance();
            if (plugin != null) {
                VaultManager vm = plugin.getVaultManager();
                if (vm != null) {
                    Chat chat = vm.getChat();
                    if (chat != null) {
                        prefix = chat.getPlayerPrefix(p);
                        suffix = chat.getPlayerSuffix(p);
                    }
                }
            }
        } catch (Exception ignored) {
            // Jika Vault error, biarkan prefix/suffix kosong agar plugin tidak crash
        }

        // 2. Buat DisplayName (Gabungan Prefix + Nama + Suffix)
        // Kita colorize per bagian agar aman jika prefix mengandung warna
        String displayName = (prefix != null ? prefix : "") + p.getName() + (suffix != null ? suffix : "");

        // 3. Replace Placeholders
        // %displayname% -> [Owner] Steve [Ganteng]
        // %player% -> Steve (Nama Asli)
        String result = message
                .replace("%displayname%", displayName)
                .replace("%player%", p.getName());

        // 4. Colorize hasil akhirnya
        return colorize(result);
    }

    /**
     * Ambil substring dengan tetap mempertahankan kode warna sebelumnya.
     * Sangat berguna untuk animasi sliding text.
     */
    public static String colorAwareSubstring(String input, int start, int end) {
        if (input == null || input.isEmpty())
            return "";

        // Safety bounds
        int safeStart = Math.min(input.length(), Math.max(0, start));
        int safeEnd = Math.min(input.length(), Math.max(safeStart, end));

        // Jika start di tengah-tengah '§e', majukan sedikit agar tidak pecah
        if (safeStart > 0 && input.charAt(safeStart - 1) == ChatColor.COLOR_CHAR) {
            safeStart--;
        }

        String sub = input.substring(safeStart, safeEnd);
        String lastColors = org.bukkit.ChatColor.getLastColors(input.substring(0, safeStart));

        return lastColors + sub;
    }

    /**
     * Menghitung panjang string tanpa menghitung kode warna.
     */
    public static int getVisualLength(String input) {
        if (input == null)
            return 0;
        return ChatColor.stripColor(input).length();
    }
}