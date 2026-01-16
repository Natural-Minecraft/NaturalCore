package id.naturalsmp.naturalcore.economy;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class BaltopGUI implements Listener {

    public void openGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27,
                ChatUtils.colorize(ConfigUtils.getString("messages.gui.baltop.title")));

        // Background Filler
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        fMeta.setDisplayName(" ");
        filler.setItemMeta(fMeta);
        for (int i = 0; i < 27; i++)
            inv.setItem(i, filler);

        // Async Calculation biar server gak lag
        Bukkit.getScheduler().runTaskAsynchronously(NaturalCore.getInstance(), () -> {
            Economy eco = NaturalCore.getInstance().getVaultManager().getEconomy();

            // Ambil semua player offline (Hati-hati jika data ribuan)
            List<OfflinePlayer> players = Arrays.asList(Bukkit.getOfflinePlayers());

            // Sortir Top 10
            List<OfflinePlayer> top10 = players.stream()
                    .sorted((p1, p2) -> Double.compare(eco.getBalance(p2), eco.getBalance(p1)))
                    .limit(10)
                    .collect(Collectors.toList());

            // Balik ke Main Thread buat update GUI
            Bukkit.getScheduler().runTask(NaturalCore.getInstance(), () -> {
                int slot = 9; // Mulai baris ke-2
                int rank = 1;
                String symbol = ConfigUtils.getString("economy.vault.symbol");

                for (OfflinePlayer op : top10) {
                    if (slot >= 18)
                        break; // Max slot

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setOwningPlayer(op);

                    String playerName = (op.getName() != null ? op.getName() : "Unknown");
                    meta.setDisplayName(ChatUtils.colorize(ConfigUtils.getString("messages.gui.baltop.item-name")
                            .replace("%rank%", String.valueOf(rank))
                            .replace("%player%", playerName)));

                    List<String> rawLore = ConfigUtils.getMessageList("gui.baltop.item-lore");
                    List<String> lore = new ArrayList<>();
                    if (rawLore != null) {
                        for (String s : rawLore) {
                            lore.add(ChatUtils.colorize(s
                                    .replace("%symbol%", symbol)
                                    .replace("%amount%", String.format("%,.0f", eco.getBalance(op)))));
                        }
                    }
                    meta.setLore(lore);

                    head.setItemMeta(meta);
                    inv.setItem(slot, head);

                    slot++;
                    rank++;
                }
                p.openInventory(inv);
            });
        });
    }

    // --- SECURITY ---
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (ChatUtils.stripColor(e.getView().getTitle()).contains("ɴᴀᴛᴜʀᴀʟ")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (ChatUtils.stripColor(e.getView().getTitle()).contains("ɴᴀᴛᴜʀᴀʟ")) {
            e.setCancelled(true);
        }
    }
}