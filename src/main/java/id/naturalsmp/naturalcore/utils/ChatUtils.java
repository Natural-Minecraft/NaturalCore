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
    private static final net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer AMPERSAND_SERIALIZER = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
            .legacyAmpersand();
    private static final net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer SECTION_SERIALIZER = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
            .legacySection();

    /**
     * Mengubah kode warna (&a, &l) dan Hex Color (&#RRGGBB) menjadi warna asli.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty())
            return "";

        String result = message;

        // 1. Process MiniMessage tag if present
        // Use a more robust check to avoid processing if it looks like raw brackets but
        // not tags
        if (result.contains("<") && (result.contains(">") || result.contains("gradient") || result.contains("color"))) {
            try {
                // MiniMessage deserialize will preserve legacy § codes as Literal text
                result = SECTION_SERIALIZER.serialize(MINI_MESSAGE.deserialize(result));
            } catch (Exception ignored) {
            }
        }

        // 2. Process Hex Color (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(result);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            try {
                String hexCode = matcher.group(1);
                matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
            } catch (Exception e) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group()));
            }
        }
        result = matcher.appendTail(buffer).toString();

        // 3. Process Legacy Colors (&a, &l, etc)
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static String serialize(net.kyori.adventure.text.Component component) {
        if (component == null)
            return "";
        return SECTION_SERIALIZER.serialize(component);
    }

    /**
     * Mengubah string menjadi Component yang SANGAT aman untuk fase Login.
     */
    public static net.kyori.adventure.text.Component toLoginSafeComponent(String message) {
        if (message == null || message.isEmpty())
            return net.kyori.adventure.text.Component.empty();

        // 1. Colorize with legacy handling
        String colored = colorize(message);

        // 2. Convert to component via legacy serializer
        return SECTION_SERIALIZER.deserialize(colored);
    }

    /**
     * Mengubah string (Legacy atau MiniMessage) menjadi Adventure Component.
     */
    public static net.kyori.adventure.text.Component toComponent(String message) {
        if (message == null || message.isEmpty())
            return net.kyori.adventure.text.Component.empty();

        // Handle <center> tags (Legacy support)
        if (message.contains("<center>")) {
            int start = message.indexOf("<center>");
            int end = message.indexOf("</center>");
            if (end > start) {
                String before = message.substring(0, start);
                String content = message.substring(start + 8, end);
                String after = message.substring(end + 9);
                return toComponent(before).append(toComponent(center(content))).append(toComponent(after));
            }
        }

        // 1. Colorize first (to handle mixed & codes and <tags>)
        // This ensures "&l" becomes a section sign code, which deserialize() respects
        // if we are careful.
        // HOWEVER, standard MiniMessage serializer might escape section signs.
        // A safer hybrid approach:

        // If it contains specific MiniMessage tags, use MiniMessage first, then Legacy.
        String colored = colorize(message);

        // Use LegacySection serializer which respects § codes.
        return SECTION_SERIALIZER.deserialize(colored);
    }

    /**
     * Center text for Minecraft chat (default 320px width).
     */
    public static String center(String text) {
        if (text == null || text.isEmpty())
            return "";

        String stripped = stripColor(text);
        int totalWidth = 320;
        int textWidth = 0;

        for (char c : stripped.toCharArray()) {
            if (c == 'i' || c == '!' || c == '.' || c == ',')
                textWidth += 2;
            else if (c == 'l' || c == '\'' || c == ';')
                textWidth += 3;
            else if (c == 't' || c == '[' || c == ']' || c == 'I')
                textWidth += 4;
            else if (c == 'f' || c == 'k' || c == '(' || c == ')' || c == '{' || c == '}')
                textWidth += 5;
            else if (c == ' ')
                textWidth += 4;
            else
                textWidth += 6;
        }

        int paddingWidth = (totalWidth / 2) - (textWidth / 2);
        if (paddingWidth <= 0)
            return text;

        return " ".repeat(paddingWidth / 4) + text;
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
     * VERSI BARU: Menggunakan visual index, bukan physical index.
     * Memberikan tepat 'visualWidth' karakter yang terlihat (visual characters).
     */
    public static String getVisualSlice(String input, int visualStart, int visualWidth) {
        if (input == null || input.isEmpty())
            return " ".repeat(visualWidth);

        // Colorize first to normalize all codes to §
        String colored = colorize(input);
        StringBuilder result = new StringBuilder();
        int currentVisualPos = 0;
        int physicalPos = 0;
        StringBuilder colorBuffer = new StringBuilder();

        while (physicalPos < colored.length()) {
            int codePoint = colored.codePointAt(physicalPos);
            int charCount = Character.charCount(codePoint);

            // Check if this is a color symbol (§)
            if (codePoint == (int) ChatColor.COLOR_CHAR && physicalPos + 1 < colored.length()) {
                // Detect Hex sequence: §x§R§R§G§G§B§B (14 characters total)
                if (physicalPos + 13 < colored.length() && colored.charAt(physicalPos + 1) == 'x') {
                    String fullHex = colored.substring(physicalPos, physicalPos + 14);
                    if (currentVisualPos < visualStart) {
                        colorBuffer.setLength(0);
                        colorBuffer.append(fullHex);
                    } else if (currentVisualPos < visualStart + visualWidth) {
                        result.append(fullHex);
                    }
                    physicalPos += 14;
                    continue;
                }

                // Normal Legacy code: §c
                String code = colored.substring(physicalPos, physicalPos + 2);
                if (currentVisualPos < visualStart) {
                    if (code.equalsIgnoreCase("§r")) {
                        colorBuffer.setLength(0);
                    } else {
                        colorBuffer.append(code);
                    }
                } else if (currentVisualPos < visualStart + visualWidth) {
                    result.append(code);
                }
                physicalPos += 2;
                continue;
            }

            // Normal character or surrogate pair
            if (currentVisualPos >= visualStart && currentVisualPos < visualStart + visualWidth) {
                result.append(colored, physicalPos, physicalPos + charCount);
            }

            currentVisualPos++;
            physicalPos += charCount;

            if (currentVisualPos >= visualStart + visualWidth)
                break;
        }

        // Padding if shorter than requested width
        String slice = result.toString();
        int currentLen = stripColor(slice).length();
        if (currentLen < visualWidth) {
            slice += " ".repeat(visualWidth - currentLen);
        }

        return colorBuffer.toString() + slice;
    }

    /**
     * Menghitung panjang string tanpa menghitung kode warna.
     */
    public static int getVisualLength(String input) {
        if (input == null)
            return 0;
        return ChatColor.stripColor(colorize(input)).length();
    }
}