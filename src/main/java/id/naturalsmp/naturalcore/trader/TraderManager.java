package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TraderManager {

    private final NaturalCore plugin;
    private boolean traderAvailable;
    private int traderStock;
    
    public TraderManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.traderAvailable = false;
        this.traderStock = 10;
        startTraderScheduler();
    }
    
    private void startTraderScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now();
                int hour = now.getHour();
                
                // Trader available jam 12:00 - 14:00 dan 20:00 - 22:00
                boolean shouldBeAvailable = (hour >= 12 && hour < 14) || (hour >= 20 && hour < 22);
                
                if (shouldBeAvailable && !traderAvailable) {
                    traderAvailable = true;
                    traderStock = 10;
                    Bukkit.broadcastMessage(ChatUtils.color("&6&l⚡ Travelling Trader telah tiba!"));
                } else if (!shouldBeAvailable && traderAvailable) {
                    traderAvailable = false;
                    Bukkit.broadcastMessage(ChatUtils.color("&e&l✈ Travelling Trader telah pergi!"));
                }
            }
        }.runTaskTimer(plugin, 0L, 1200L); // Check setiap menit
    }
    
    public void openTraderGUI(Player player) {
        if (!traderAvailable) {
            player.sendMessage(ChatUtils.color("&c&l✘ &cTrader sedang tidak tersedia!"));
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.color("&6&l⚡ Travelling Trader"));
        
        // Add items for sale
        inv.setItem(10, new ItemBuilder(Material.DIAMOND)
                .setName("&b&lDiamond")
                .setLore(
                    "&7Price: &f$1000",
                    "&7Stock: &f" + traderStock,
                    "",
                    "&eKlik untuk membeli!"
                )
                .build());
        
        inv.setItem(12, new ItemBuilder(Material.EMERALD)
                .setName("&a&lEmerald")
                .setLore(
                    "&7Price: &f$500",
                    "&7Stock: &f" + traderStock,
                    "",
                    "&eKlik untuk membeli!"
                )
                .build());
        
        inv.setItem(14, new ItemBuilder(Material.GOLDEN_APPLE)
                .setName("&6&lGolden Apple")
                .setLore(
                    "&7Price: &f$2000",
                    "&7Stock: &f" + traderStock,
                    "",
                    "&eKlik untuk membeli!"
                )
                .build());
        
        player.openInventory(inv);
    }
    
    public boolean isTraderAvailable() {
        return traderAvailable;
    }
    
    public String getNextArrivalTime() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        
        if (hour < 12) {
            return "12:00 (Siang)";
        } else if (hour < 20) {
            return "20:00 (Malam)";
        } else {
            return "12:00 (Besok)";
        }
    }
    
    public void decreaseStock() {
        if (traderStock > 0) {
            traderStock--;
        }
    }
    
    public int getStock() {
        return traderStock;
    }
}
