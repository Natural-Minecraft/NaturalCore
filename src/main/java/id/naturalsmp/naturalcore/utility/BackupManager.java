package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private final NaturalCore plugin;
    private final File backupFolder;

    public BackupManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.backupFolder = new File(plugin.getDataFolder(), "backups");
        if (!backupFolder.exists())
            backupFolder.mkdirs();

        // Start Auto-Backup Task (Every 6 hours)
        startAutoBackup();
    }

    private void startAutoBackup() {
        new BukkitRunnable() {
            @Override
            public void run() {
                createBackup("AutoBackup");
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60 * 60, 20L * 60 * 60 * 6); // Delay 1h, Period 6h
    }

    public synchronized void createBackup(String source) {
        plugin.getLogger().info("Starting " + source + " for critical data...");

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
        File zipFile = new File(backupFolder, "Backup_" + timeStamp + ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Backup critical folders
            zipFolder(new File(plugin.getDataFolder(), "player-profiles"), "player-profiles", zos);
            zipFile(new File(plugin.getDataFolder(), "warps.json"), "warps.json", zos);
            zipFile(new File(plugin.getDataFolder(), "homes.json"), "homes.json", zos);

            plugin.getLogger().info("Backup successfully completed! Saved at: " + zipFile.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("FAILED to create backup: " + e.getMessage());
        }
    }

    private void zipFolder(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        if (!folder.exists())
            return;
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipFolder(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zipFile(file, parentFolder + "/" + file.getName(), zos);
        }
    }

    private void zipFile(File file, String fileName, ZipOutputStream zos) throws IOException {
        if (!file.exists())
            return;
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
            zos.closeEntry();
        }
    }
}
