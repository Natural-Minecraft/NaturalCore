package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TraderListener implements Listener {

    private final TraderManager manager;
    private final TradeEditor editor; // Tidak terlalu dipakai disini tapi biar struktur aman

    public TraderListener(TraderManager manager, TradeEditor editor) {
        this.manager = manager;
        this.editor = editor;
    }

    // --- HANDLE ADMIN INPUT BARANG ---
    @EventHandler
    public void onEditorClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals(ChatUtils.colorize("&cInput 35 Barang"))) {
            for (int i = 0; i < 35; i++) {
                ItemStack item = e.getInventory().getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    manager.setItem(i, item);
                } else {
                    manager.setItem(i, null); // Clear slot jika kosong
                }
            }
            manager.saveData();
            e.getPlayer().sendMessage(ChatUtils.colorize("&8[&6Trader&8] &aStok barang disimpan!"));
        }
    }

    // --- HANDLE PLAYER BUYING ---
    @EventHandler
    public void onBuy(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!title.contains("WT |")) return; // Cek Title GUI Player

        e.setCancelled(true); // Biar gak bisa ambil item display

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE || clicked.getType() == Material.BARRIER) return;

        // Cek Slot (Harus 11-15)
        int slot = e.getSlot();
        if (slot < 11 || slot > 15) return;

        // Hitung Index Asli di Database (Offset + StartIndex)
        int offset = slot - 11;
        int realIndex = manager.getTodayStartIndex() + offset;

        // Validasi Stok
        if (manager.getStock(realIndex) <= 0) {
            p.sendMessage(ChatUtils.colorize("&8[&6Trader&8] &cMaaf, stok barang ini sudah habis!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Validasi Uang (Vault)
        double price = manager.getPrice(realIndex);
        Economy eco = NaturalCore.getInstance().getVaultManager().getEconomy();

        if (eco.getBalance(p) < price) {
            p.sendMessage(ChatUtils.colorize("&8[&6Trader&8] &cUang tidak cukup! (Kurang: &7$" + (price - eco.getBalance(p)) + "&c)"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // --- PROSES TRANSAKSI ---
        eco.withdrawPlayer(p, price);
        manager.reduceStock(realIndex);

        // Beri Item (Bersihkan Lore Harga Dulu)
        ItemStack finalItem = manager.getItem(realIndex).clone();
        ItemMeta meta = finalItem.getItemMeta();
        meta.setLore(new ArrayList<>()); // Hapus lore harga
        finalItem.setItemMeta(meta);

        p.getInventory().addItem(finalItem);

        p.sendMessage(ChatUtils.colorize("&8[&6Trader&8] &aBerhasil membeli item seharga &e$" + price));
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        // Refresh GUI biar stok berkurang visualnya
        p.performCommand("wt");
    }
}