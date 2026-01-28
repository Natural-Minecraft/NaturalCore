package id.naturalsmp.naturalcore.economy;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class BaltopGUI implements Listener {

    public void openGUI(Player p) {
        Inventory inv = GUIUtils.createGUI(new BaltopHolder(), 27,
                ConfigUtils.getString("messages.gui.baltop.title"));

        // Background Filler
        ItemStack filler = GUIUtils.createFiller(Material.BLACK_STAINED_GLASS_PANE);
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
                    meta.displayName(ChatUtils.toComponent(ConfigUtils.getString("messages.gui.baltop.item-name")
                            .replace("%rank%", String.valueOf(rank))
                            .replace("%player%", playerName)));

                    List<String> rawLore = ConfigUtils.getMessageList("gui.baltop.item-lore");
                    List<Component> lore = new ArrayList<>();
                    if (rawLore != null) {
                        for (String s : rawLore) {
                            lore.add(ChatUtils.toComponent(s
                                    .replace("%symbol%", symbol)
                                    .replace("%amount%", String.format("%,.0f", eco.getBalance(op)))));
                        }
                    }
                    meta.lore(lore);

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
        if (e.getInventory().getHolder() instanceof BaltopHolder) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof BaltopHolder) {
            e.setCancelled(true);
        }
    }

    public static class BaltopHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
