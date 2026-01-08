package id.naturalsmp.naturalcore.utils;

import id.naturalsmp.naturalcore.NaturalCore;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }

    public static String format(double amount) {
        return new DecimalFormat("#,###.##").format(amount);
    }

    /**
     * METHOD BARU: Mengganti placeholder %displayname% dan %player%
     * sekaligus memberi warna.
     */
    public static String formatMessage(Player p, String message) {
        if (message == null) return "";

        // 1. Ambil Data Vault (Prefix/Suffix)
        String prefix = "";
        String suffix = "";

        try {
            Chat chat = NaturalCore.getInstance().getVaultManager().getChat();
            if (chat != null) {
                prefix = chat.getPlayerPrefix(p);
                suffix = chat.getPlayerSuffix(p);
            }
        } catch (Exception ignored) {}

        // 2. Buat DisplayName (Gabungan Prefix + Nama + Suffix)
        String displayName = prefix + p.getName() + suffix;

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