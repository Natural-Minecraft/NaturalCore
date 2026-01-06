package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TraderCommand implements CommandExecutor {

    private final CurrencyManager currencyManager; // (Opsional/Unused for now)
    private final TraderManager manager;
    private final TradeEditor editor; // (Opsional)

    public TraderCommand(CurrencyManager currencyManager, TraderManager manager, TradeEditor editor) {
        this.currencyManager = currencyManager;
        this.manager = manager;
        this.editor = editor;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // --- PLAYER COMMAND: /wt ---
        if (label.equalsIgnoreCase("wanderingtrader") || label.equalsIgnoreCase("wt")) {
            if (!(sender instanceof Player)) return true;
            openTraderMenu((Player) sender);
            return true;
        }

        // --- ADMIN COMMANDS ---
        if (!sender.hasPermission("op")) {
            sender.sendMessage(ChatUtils.colorize("&cNo permission."));
            return true;
        }

        // 1. /settrader (GUI Input Barang)
        if (label.equalsIgnoreCase("settrader")) {
            if (!(sender instanceof Player)) return true;
            Player p = (Player) sender;
            Inventory inv = Bukkit.createInventory(null, 36, ChatUtils.colorize("&cInput 35 Barang"));

            // Load item yang sudah ada
            for (int i = 0; i < 35; i++) {
                if (manager.getItem(i) != null) {
                    inv.setItem(i, manager.getItem(i));
                }
            }
            p.openInventory(inv);
            return true;
        }

        // 2. /resettrader
        if (label.equalsIgnoreCase("resettrader")) {
            manager.resetAll();
            sender.sendMessage(ChatUtils.colorize("&8[&6Trader&8] &aData trader berhasil direset total!"));
            return true;
        }

        // 3. /setharga <slot> <harga>
        if (label.equalsIgnoreCase("setharga")) {
            if (args.length < 2) {
                sender.sendMessage(ChatUtils.colorize("&cUsage: /setharga <slot 0-34> <harga>"));
                return true;
            }
            try {
                int slot = Integer.parseInt(args[0]);
                double price = Double.parseDouble(args[1]);
                manager.setPrice(slot, price);
                manager.saveData();
                sender.sendMessage(ChatUtils.colorize("&8[&6Trader&8] &aHarga slot " + slot + " diset ke &e$" + price));
            } catch (NumberFormatException e) {
                sender.sendMessage("&cAngka tidak valid!");
            }
            return true;
        }

        // 4. /setallharga <harga>
        if (label.equalsIgnoreCase("setallharga")) {
            if (args.length < 1) return true;
            double price = Double.parseDouble(args[0]);
            for (int i = 0; i < 35; i++) manager.setPrice(i, price);
            manager.saveData();
            sender.sendMessage(ChatUtils.colorize("&8[&6Trader&8] &aSemua item diset ke &e$" + price));
            return true;
        }

        // 5. /setallstok <jumlah>
        if (label.equalsIgnoreCase("setallstok")) {
            if (args.length < 1) return true;
            int stok = Integer.parseInt(args[0]);
            for (int i = 0; i < 35; i++) manager.setStock(i, stok);
            manager.saveData();
            sender.sendMessage(ChatUtils.colorize("&8[&6Trader&8] &aSemua stok diset ke &e" + stok));
            return true;
        }

        return true;
    }

    private void openTraderMenu(Player p) {
        String hari = manager.getCurrentDayName();
        // Title Real-Time
        String title = "&8WT | " + hari + " | " + manager.getNow().getHour() + ":" + String.format("%02d", manager.getNow().getMinute()) + " WIB";

        Inventory inv = Bukkit.createInventory(null, 27, ChatUtils.colorize(title));

        // Glass Pane Filler
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        gMeta.setDisplayName(" ");
        glass.setItemMeta(gMeta);

        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Isi Item Sesuai Hari
        int startIndex = manager.getTodayStartIndex();
        int guiSlot = 11;

        for (int i = 0; i < 5; i++) {
            int realIdx = startIndex + i;
            ItemStack item = manager.getItem(realIdx);

            if (item != null && item.getType() != Material.AIR) {
                ItemStack display = item.clone();
                ItemMeta meta = display.getItemMeta();

                // Format Lore Skript Style
                List<String> lore = new ArrayList<>();
                lore.add(ChatUtils.colorize("&r"));
                lore.add(ChatUtils.colorize("&7Harga: &a$" + manager.getPrice(realIdx)));
                lore.add(ChatUtils.colorize("&7Stok: &e" + manager.getStock(realIdx)));

                if (manager.getStock(realIdx) > 0) {
                    lore.add(ChatUtils.colorize("&eKlik untuk membeli"));
                } else {
                    lore.add(ChatUtils.colorize("&cSTOK HABIS"));
                }

                meta.setLore(lore);
                display.setItemMeta(meta);
                inv.setItem(guiSlot, display);
            } else {
                // Tampilkan Barrier kalau slot kosong
                ItemStack barrier = new ItemStack(Material.BARRIER);
                ItemMeta bMeta = barrier.getItemMeta();
                bMeta.setDisplayName(ChatUtils.colorize("&cKosong"));
                barrier.setItemMeta(bMeta);
                inv.setItem(guiSlot, barrier);
            }
            guiSlot++;
        }
        p.openInventory(inv);
    }
}