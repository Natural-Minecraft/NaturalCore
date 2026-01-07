package id.naturalsmp.naturalcore;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
        return "1.3";
    }

    @Override
    public boolean persist() {
        return true; // Penting agar tidak unhook saat reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return "";

        Player p = player.getPlayer();

        // %naturalcore_homes%
        if (params.equalsIgnoreCase("homes")) {
            return String.valueOf(plugin.getHomeManager().getHomes(p).size());
        }

        // %naturalcore_maxhomes%
        if (params.equalsIgnoreCase("maxhomes")) {
            return String.valueOf(plugin.getHomeManager().getMaxHomes(p));
        }

        return null;
    }
}