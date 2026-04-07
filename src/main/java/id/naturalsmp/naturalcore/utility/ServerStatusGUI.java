package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ServerStatusGUI implements Listener {

    private final NaturalCore plugin;
    private final String[] bars = { " ", "▂", "▃", "▄", "▅", "▆", "▇", "█" };

    public ServerStatusGUI(NaturalCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        Inventory inv = GUIUtils.createGUI(new StatusHolder(), 45,
                "&#00FFAA&lＳＥＲＶＥＲ &8| &#00D4FF&lＳＴＡＴＵＳ");

        ServerHealthManager health = plugin.getHealthManager();

        // Decoration
        ItemStack cyanGlass = GUIUtils.createFiller(Material.CYAN_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, cyanGlass);
            inv.setItem(36 + i, cyanGlass);
        }

        // TPS Item
        inv.setItem(13, createItem(Material.NETHER_STAR, "&#00FFD4&lTPS STATUS",
                "&7Stabilitas jantung server.",
                "",
                "&8» &fCurrent: " + formatTps(health.getCurrentTps()),
                "&8» &fHistory: " + generateGraph(health.getTpsHistory(), 20.0),
                "",
                "&7Last 2.5 minutes check."));

        // RAM Item
        inv.setItem(21, createItem(Material.CHEST, "&#FFAA00&lMEMORY USAGE",
                "&7Penyimpanan data dinamis.",
                "",
                "&8» &fUsed: &e" + (int) health.getCurrentRam() + " MB",
                "&8» &fMax: &7" + (int) health.getMaxRam() + " MB",
                "&8» &fLoad: " + generateBar(health.getCurrentRam(), health.getMaxRam()),
                "",
                "&7GC is optimized for G1."));

        // Entity Item
        inv.setItem(23, createItem(Material.ARMOR_STAND, "&#FFD400&lENTITIES & CHUNKS",
                "&7Beban objek dunia.",
                "",
                "&8» &fTotal Entities: &e" + health.getTotalEntities(),
                "&8» &fLoaded Chunks: &e" + health.getTotalChunks(),
                "",
                "&7Monitoring by NaturalLagg."));

        // Refresh/Close
        inv.setItem(40, GUIUtils.createClosePaper());

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
    }

    private String formatTps(double tps) {
        if (tps >= 18.0)
            return "&#55FF55" + String.format("%.2f", tps) + " &7(Healthy)";
        if (tps >= 15.0)
            return "&#FFAA00" + String.format("%.2f", tps) + " &7(Stressed)";
        return "&#FF5555" + String.format("%.2f", tps) + " &c&l(LAGGING)";
    }

    private String generateGraph(List<Double> history, double max) {
        StringBuilder sb = new StringBuilder("&f");
        // Create a copy to avoid ConcurrentModificationException if needed,
        // though history is now synchronized, iteration still needs care or snapshots.
        List<Double> snapshot;
        synchronized (history) {
            snapshot = new ArrayList<>(history);
        }

        for (int i = snapshot.size() - 1; i >= 0; i--) {
            double val = snapshot.get(i);
            int index = (int) ((val / max) * (bars.length - 1));
            index = Math.max(0, Math.min(index, bars.length - 1));
            sb.append(bars[index]);
        }
        return sb.toString();
    }

    private String generateBar(double current, double total) {
        int length = 10;
        int filled = (int) ((current / total) * length);
        StringBuilder sb = new StringBuilder("&7[");
        for (int i = 0; i < length; i++) {
            if (i < filled)
                sb.append("&#00FF00:");
            else
                sb.append("&8:");
        }
        sb.append("&7] &f" + (int) ((current / total) * 100) + "%");
        return sb.toString();
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<Component> loreList = new ArrayList<>();
            for (String l : lore)
                loreList.add(ChatUtils.toComponent(l));
            meta.lore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof StatusHolder))
            return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory())
            return;
        if (e.getRawSlot() == 40) {
            e.getWhoClicked().closeInventory();
        }
    }

    public static class StatusHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
