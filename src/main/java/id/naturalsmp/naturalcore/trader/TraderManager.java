package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TraderManager {

    private final NaturalCore plugin;
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, Double> prices = new HashMap<>();
    private final Map<Integer, Integer> stock = new HashMap<>();
    private File file;
    private FileConfiguration config;

    // Inject Editor cuma buat dependency, logic utamanya disini
    public TraderManager(NaturalCore plugin, TradeEditor editor) {
        this.plugin = plugin;
        loadData();
    }

    // --- LOGIKA WAKTU (WIB) ---

    public ZonedDateTime getNow() {
        return ZonedDateTime.now(ZoneId.of("Asia/Jakarta"));
    }

    public String getCurrentDayName() {
        return getNow().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
    }

    // Mengubah Hari menjadi Index Awal (Senin=0, Selasa=5, dst)
    public int getTodayStartIndex() {
        DayOfWeek day = getNow().getDayOfWeek();
        // Java: Monday=1, Sunday=7. Kita mau Monday=0.
        // Rumus: (Hari - 1) * 5
        return (day.getValue() - 1) * 5;
    }

    // --- DATA MANAGEMENT ---

    public void setItem(int index, ItemStack item) { items.put(index, item); }
    public ItemStack getItem(int index) { return items.getOrDefault(index, null); }

    public void setPrice(int index, double price) { prices.put(index, price); }
    public double getPrice(int index) { return prices.getOrDefault(index, 0.0); }

    public void setStock(int index, int amount) { stock.put(index, amount); }
    public int getStock(int index) { return stock.getOrDefault(index, 0); }

    public void reduceStock(int index) {
        int current = getStock(index);
        if (current > 0) stock.put(index, current - 1);
        saveData(); // Auto save setiap transaksi biar aman
    }

    public void resetAll() {
        items.clear();
        prices.clear();
        stock.clear();
        saveData();
    }

    // --- SAVE & LOAD (trader.yml) ---

    public void loadData() {
        file = new File(plugin.getDataFolder(), "trader.yml");
        if (!file.exists()) {
            plugin.saveResource("trader.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        if (config.getConfigurationSection("items") == null) return;

        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            int idx = Integer.parseInt(key);
            items.put(idx, config.getItemStack("items." + key + ".item"));
            prices.put(idx, config.getDouble("items." + key + ".price"));
            stock.put(idx, config.getInt("items." + key + ".stock"));
        }
    }

    public void saveData() {
        config.set("items", null); // Clear lama
        for (int i = 0; i < 35; i++) {
            if (items.containsKey(i) && items.get(i) != null) {
                config.set("items." + i + ".item", items.get(i));
                config.set("items." + i + ".price", prices.get(i));
                config.set("items." + i + ".stock", stock.get(i));
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}