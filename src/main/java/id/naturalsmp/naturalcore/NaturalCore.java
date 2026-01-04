package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.KickAllCommand;
import id.naturalsmp.naturalcore.admin.BroadcastCommand;
import id.naturalsmp.naturalcore.admin.RestartAlertCommand;
import id.naturalsmp.naturalcore.admin.NaturalCoreCommand;

import id.naturalsmp.naturalcore.economy.VaultManager;

import id.naturalsmp.naturalcore.trader.CurrencyManager;
import id.naturalsmp.naturalcore.trader.TradeEditor;
import id.naturalsmp.naturalcore.trader.TraderCommand;
import id.naturalsmp.naturalcore.trader.TraderListener;
import id.naturalsmp.naturalcore.trader.TraderManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;

import id.naturalsmp.naturalcore.warp.WarpManager;
import id.naturalsmp.naturalcore.warp.WarpCommand;

import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalCore extends JavaPlugin {

    // Singleton Instance (Agar bisa diakses dari class lain)
    private static NaturalCore instance;
    private VaultManager vaultManager;
    
    // Trader Module
    private CurrencyManager currencyManager;
    private TradeEditor tradeEditor;
    private TraderManager traderManager;

    private WarpManager warpManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &aStarting up..."));

        saveDefaultConfig();

        vaultManager = new VaultManager(this);
        if (!vaultManager.setupEconomy()) {
            getLogger().severe("Vault tidak ditemukan! Fitur ekonomi dimatikan.");
        }

        currencyManager = new CurrencyManager(this);
        tradeEditor = new TradeEditor(this);
        traderManager = new TraderManager(this, tradeEditor);
        
        // Register Trader Listener
        getServer().getPluginManager().registerEvents(new TraderListener(traderManager, tradeEditor), this);

        // 5. Register Commands (Kita cicil satu per satu)
        // Format: getCommand("nama_di_plugin_yml").setExecutor(new ClassCommand());

        // --- Admin Module ---
        if (getCommand("kickall") != null) getCommand("kickall").setExecutor(new KickAllCommand());
        if (getCommand("restartalert") != null) getCommand("restartalert").setExecutor(new RestartAlertCommand());
        if (getCommand("bc") != null) getCommand("bc").setExecutor(new BroadcastCommand());
        if (getCommand("nacore") != null) getCommand("nacore").setExecutor(new NaturalCoreCommand());

        // --- Trader Module ---
        TraderCommand traderCmd = new TraderCommand(currencyManager, traderManager, tradeEditor);
        if (getCommand("givecurrency") != null) getCommand("givecurrency").setExecutor(traderCmd);
        if (getCommand("tradeeditor") != null) getCommand("tradeeditor").setExecutor(traderCmd);
        if (getCommand("wanderingtrader") != null) getCommand("wanderingtrader").setExecutor(traderCmd);

        // Setup Warp System
        this.warpManager = new WarpManager(this);

        WarpCommand warpCmd = new WarpCommand(this);
        getCommand("warp").setExecutor(warpCmd);
        getCommand("warps").setExecutor(warpCmd);
        getCommand("setwarp").setExecutor(warpCmd);
        getCommand("delwarp").setExecutor(warpCmd);
        getCommand("setwarpicon").setExecutor(warpCmd);

        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &asudah aktif sepenuhnya!"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatUtils.colorize("&c&lNaturalCore &idisabling..."));
        // Di sini nanti kita akan save data Altar/Trader sebelum server mati
        if (traderManager != null) {
            traderManager.onDisable();
        }
    }

    // Getter untuk Instance
    public static NaturalCore getInstance() {
        return instance;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }
}
