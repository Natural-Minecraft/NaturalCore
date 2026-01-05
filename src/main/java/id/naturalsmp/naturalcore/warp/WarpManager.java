package id.naturalsmp.naturalcore.warp;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarpManager {

    private final NaturalCore plugin;
    private final Map<String, Warp> warps = new HashMap<>();
    private final File warpFolder;

    public WarpManager(NaturalCore plugin) {
        this.plugin = plugin;
        // Tentukan lokasi folder: plugins/NaturalCore/warps/
        this.warpFolder = new File(plugin.getDataFolder(), "warps");

        // Buat folder jika belum ada
        if (!warpFolder.exists()) {
            warpFolder.mkdirs();
        }

        loadWarps();
    }

    public void createWarp(String id, Location loc) {
        // Cari slot kosong otomatis
        int slot = 0;
        while (isSlotTaken(slot)) {
            slot++;
        }

        // Buat object warp baru
        Warp newWarp = new Warp(id.toLowerCase(), loc, slot);
        warps.put(id.toLowerCase(), newWarp);

        // Simpan ke file <id>.yml
        saveWarpToFile(newWarp);
    }

    public void deleteWarp(String id) {
        String lowerId = id.toLowerCase();
        if (warps.containsKey(lowerId)) {
            warps.remove(lowerId);

            // Hapus file fisiknya
            File file = new File(warpFolder, lowerId + ".yml");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public Warp getWarp(String id) {
        return warps.get(id.toLowerCase());
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

    public boolean isSlotTaken(int slot) {
        return warps.values().stream().anyMatch(w -> w.getSlot() == slot);
    }

    // --- LOGIKA LOAD DARI FOLDER ---

    public void loadWarps() {
        warps.clear();

        // Ambil semua file di dalam folder 'warps'
        File[] files = warpFolder.listFiles();
        if (files == null) return;

        for (File file : files) {
            // Cuma baca file yang akhiran .yml
            if (file.isFile() && file.getName().endsWith(".yml")) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                try {
                    String id = file.getName().replace(".yml", "");

                    String displayName = config.getString("display", "&a" + id);
                    Material icon = Material.valueOf(config.getString("icon", "ENDER_PEARL"));
                    int slot = config.getInt("slot", 0);
                    List<String> lore = config.getStringList("lore");

                    // Load Location Data
                    String worldName = config.getString("location.world");
                    if (worldName == null || Bukkit.getWorld(worldName) == null) {
                        plugin.getLogger().warning("World untuk warp '" + id + "' tidak ditemukan! Skip.");
                        continue;
                    }

                    double x = config.getDouble("location.x");
                    double y = config.getDouble("location.y");
                    double z = config.getDouble("location.z");
                    float yaw = (float) config.getDouble("location.yaw");
                    float pitch = (float) config.getDouble("location.pitch");

                    Location loc = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

                    // Masukkan ke memori
                    warps.put(id, new Warp(id, displayName, loc, icon, slot, lore));

                } catch (Exception e) {
                    plugin.getLogger().severe("Gagal memuat warp dari file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
        plugin.getLogger().info("Berhasil memuat " + warps.size() + " warp.");
    }

    // --- LOGIKA SAVE KE FILE INDIVIDUAL ---

    public void saveWarps() {
        // Loop semua warp di memori dan simpan satu per satu
        for (Warp warp : warps.values()) {
            saveWarpToFile(warp);
        }
    }

    private void saveWarpToFile(Warp warp) {
        File file = new File(warpFolder, warp.getId() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("display", warp.getDisplayName());
        config.set("icon", warp.getIcon().name());
        config.set("slot", warp.getSlot());
        config.set("lore", warp.getLore());

        // Simpan Lokasi Rapi
        config.set("location.world", warp.getLocation().getWorld().getName());
        config.set("location.x", warp.getLocation().getX());
        config.set("location.y", warp.getLocation().getY());
        config.set("location.z", warp.getLocation().getZ());
        config.set("location.yaw", warp.getLocation().getYaw());
        config.set("location.pitch", warp.getLocation().getPitch());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan warp: " + warp.getId());
            e.printStackTrace();
        }
    }
}