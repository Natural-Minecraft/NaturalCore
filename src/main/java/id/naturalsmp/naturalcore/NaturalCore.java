package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.KickAllCommand;
import id.naturalsmp.naturalcore.admin.BroadcastCommand;
import id.naturalsmp.naturalcore.admin.RestartAlertCommand;
import id.naturalsmp.naturalcore.admin.NaturalCoreCommand;

import id.naturalsmp.naturalcore.economy.VaultManager;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalCore extends JavaPlugin {

    // Singleton Instance (Agar bisa diakses dari class lain)
    private static NaturalCore instance;
    private VaultManager vaultManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Pesan Startup Keren
        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &aStarting up..."));

        // 2. Setup Config Default
        saveDefaultConfig();

        // 3. Setup Economy (Vault)
        vaultManager = new VaultManager(this);
        if (!vaultManager.setupEconomy()) {
            getLogger().severe("Vault tidak ditemukan! Fitur ekonomi dimatikan.");
        }

        // 4. Register Commands (Kita cicil satu per satu)
        // Format: getCommand("nama_di_plugin_yml").setExecutor(new ClassCommand());

        // --- Admin Module ---
        if (getCommand("kickall") != null) getCommand("kickall").setExecutor(new KickAllCommand());
        if (getCommand("restartalert") != null) getCommand("restartalert").setExecutor(new RestartAlertCommand());
        if (getCommand("bc") != null) getCommand("bc").setExecutor(new BroadcastCommand());
        if (getCommand("nacore") != null) getCommand("nacore").setExecutor(new NaturalCoreCommand());

        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &asudah aktif sepenuhnya!"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatUtils.colorize("&c&lNaturalCore &idisabling..."));
        // Di sini nanti kita akan save data Altar/Trader sebelum server mati
    }

    // Getter untuk Instance
    public static NaturalCore getInstance() {
        return instance;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }
}