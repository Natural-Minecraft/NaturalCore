package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.BroadcastCommand;
import id.naturalsmp.naturalcore.admin.KickAllCommand;
import id.naturalsmp.naturalcore.admin.NaturalCoreCommand;
import id.naturalsmp.naturalcore.admin.RestartAlertCommand;
import id.naturalsmp.naturalcore.admin.GiveBalCommand;
import id.naturalsmp.naturalcore.economy.VaultManager;
import id.naturalsmp.naturalcore.trader.CurrencyManager;
import id.naturalsmp.naturalcore.trader.TradeEditor;
import id.naturalsmp.naturalcore.trader.TraderCommand;
import id.naturalsmp.naturalcore.trader.TraderListener;
import id.naturalsmp.naturalcore.trader.TraderManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.warp.WarpCommand;
import id.naturalsmp.naturalcore.warp.WarpManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalCore extends JavaPlugin {

    private static NaturalCore instance;
    private VaultManager vaultManager;
    private WarpManager warpManager;

    // Variable Trader Module
    private TraderManager traderManager;
    private TradeEditor tradeEditor;
    private CurrencyManager currencyManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Pesan Startup
        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &aStarting up..."));

        // 2. Setup Config
        saveDefaultConfig();

        // 3. Setup Economy (Vault)
        this.vaultManager = new VaultManager(this);
        if (!vaultManager.setupEconomy()) {
            getLogger().warning("Vault/Economy tidak ditemukan!");
        } else {
            // Register Command
            getCommand("givebal").setExecutor(new GiveBalCommand());
        }

        // 4. Setup Warp Module
        this.warpManager = new WarpManager(this);
        WarpCommand warpCmd = new WarpCommand(this);

        // Register Warp Commands
        if (getCommand("warp") != null) getCommand("warp").setExecutor(warpCmd);
        if (getCommand("warps") != null) getCommand("warps").setExecutor(warpCmd);
        if (getCommand("setwarp") != null) getCommand("setwarp").setExecutor(warpCmd);
        if (getCommand("delwarp") != null) getCommand("delwarp").setExecutor(warpCmd);
        if (getCommand("setwarpicon") != null) getCommand("setwarpicon").setExecutor(warpCmd);

        // 5. Setup Trader Module (Citizens Check)
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getLogger().info("Citizens ditemukan. Mengaktifkan Trader Module...");

            // Initialize Helpers & Manager
            this.currencyManager = new CurrencyManager(this);
            this.tradeEditor = new TradeEditor(this);
            this.traderManager = new TraderManager(this, tradeEditor);

            // Register Commands
            TraderCommand traderCmd = new TraderCommand(currencyManager, traderManager, tradeEditor);

            if (getCommand("wanderingtrader") != null) getCommand("wanderingtrader").setExecutor(traderCmd);
            if (getCommand("settrader") != null) getCommand("settrader").setExecutor(traderCmd);
            if (getCommand("resettrader") != null) getCommand("resettrader").setExecutor(traderCmd);
            if (getCommand("setharga") != null) getCommand("setharga").setExecutor(traderCmd);
            if (getCommand("setallharga") != null) getCommand("setallharga").setExecutor(traderCmd);
            if (getCommand("setallstok") != null) getCommand("setallstok").setExecutor(traderCmd);

            // Register Listener
            getServer().getPluginManager().registerEvents(new TraderListener(traderManager, tradeEditor), this);

        } else {
            getLogger().warning("Citizens tidak ditemukan! Modul Trader dinonaktifkan.");
        }

        // 6. Setup Admin Commands
        // Kita daftarkan command ini sebagai command mandiri, BUKAN sub-command.
        // Karena di GUI /nacore nanti cuma ditampilkan sebagai Info/Shortcut.
        if (getCommand("kickall") != null) getCommand("kickall").setExecutor(new KickAllCommand());
        if (getCommand("restartalert") != null) getCommand("restartalert").setExecutor(new RestartAlertCommand());
        if (getCommand("bc") != null) getCommand("bc").setExecutor(new BroadcastCommand());

        // 7. Register MAIN Dashboard Command (/nacore)
        // Ini akan membuka GUI Menu Admin
        if (getCommand("nacore") != null) {
            getCommand("nacore").setExecutor(new NaturalCoreCommand(this));
        }

        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &asudah aktif sepenuhnya!"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatUtils.colorize("&c&lNaturalCore &idisabling..."));
        // Save data warp sebelum mati
        if (warpManager != null) {
            warpManager.saveWarps();
        }
    }

    public static NaturalCore getInstance() {
        return instance;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public TraderManager getTraderManager() {
        return traderManager;
    }
}