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

    /**
     * Mengubah kode warna (&a, &l) dan Hex Color (&#RRGGBB) menjadi warna asli.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            try {
                // Konversi &#RRGGBB menjadi ChatColor Hex
                String hexCode = matcher.group(1);
                matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
            } catch (Exception e) {
                // Fallback jika terjadi error pada versi lama
                matcher.appendReplacement(buffer, "");
            }
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
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
        if (message == null) return "";

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
        // %player%      -> Steve (Nama Asli)
        String result = message
                .replace("%displayname%", displayName)
                .replace("%player%", p.getName());

        // 4. Colorize hasil akhirnya
        return colorize(result);
    }
}