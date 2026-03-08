package id.naturalsmp.naturalcore;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NaturalCoreExpansion extends PlaceholderExpansion {

    private final NaturalCore plugin;

    public NaturalCoreExpansion(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "naturalcore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "NaturalSMP";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Penting agar tidak unhook saat reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline())
            return "";

        Player p = player.getPlayer();

        // %naturalcore_homes%
        if (params.equalsIgnoreCase("homes")) {
            return String.valueOf(plugin.getHomeManager().getHomes(p).size());
        }

        // %naturalcore_maxhomes%
        if (params.equalsIgnoreCase("maxhomes")) {
            return String.valueOf(plugin.getHomeManager().getMaxHomes(p));
        }

        // %naturalcore_playerrank%
        // Menggunakan ItemsAdder FontImageWrapper untuk langsung resolve
        // karakter unicode font image, agar bisa dipakai di scoreboard.
        if (params.equalsIgnoreCase("playerrank")) {
            id.naturalsmp.naturalcore.permissions.PermissionManager.RankConfig rank = plugin.getPermissionManager()
                    .getHighestRank(p);

            // Ambil nama image dari prefix rank (format: :_rank-member_: )
            String imageName = "_rank-member_"; // default
            if (rank != null && rank.prefix != null) {
                imageName = rank.prefix.replace(":", "").trim();
            }

            // Coba resolve via ItemsAdder API
            if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
                try {
                    // Format: "namespace:imagename" -> "naturalprefix:_rank-member_"
                    FontImageWrapper fontImage = new FontImageWrapper("naturalprefix:" + imageName);
                    if (fontImage.exists()) {
                        return fontImage.getString();
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to resolve font image: " + imageName);
                }
            }

            // Fallback: return raw prefix jika ItemsAdder tidak tersedia
            return rank != null && rank.prefix != null ? rank.prefix : ":_rank-member_:";
        }

        return null;
    }
}