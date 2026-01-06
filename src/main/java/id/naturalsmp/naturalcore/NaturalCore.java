package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.BroadcastCommand;
import id.naturalsmp.naturalcore.admin.KickAllCommand;
import id.naturalsmp.naturalcore.admin.NaturalCoreCommand;
import id.naturalsmp.naturalcore.admin.RestartAlertCommand;
import id.naturalsmp.naturalcore.economy.VaultManager;
// Import Module Trader yang Lengkap
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
        getLogger().info(ChatUtils.colorize("NaturalCore Starting up..."));

        // 2. Setup Config
        saveDefaultConfig();

        // 3. Setup Economy (Vault)
        this.vaultManager = new VaultManager(this);
        if (!vaultManager.setupEconomy()) {
            getLogger().warning("Vault/Economy tidak ditemukan!");
        }

        // 4. Create main command handler
        NaturalCoreCommand coreCommand = new NaturalCoreCommand();

        // 5. Setup Admin Commands
        KickAllCommand kickAllCmd = new KickAllCommand();
        RestartAlertCommand restartAlertCmd = new RestartAlertCommand();
        BroadcastCommand broadcastCmd = new BroadcastCommand();

        // Register ke plugin.yml
        if (getCommand("kickall") != null)
            getCommand("kickall").setExecutor(kickAllCmd);
        if (getCommand("restartalert") != null)
            getCommand("restartalert").setExecutor(restartAlertCmd);
        if (getCommand("bc") != null)
            getCommand("bc").setExecutor(broadcastCmd);

        // Register sebagai subcommand /nacore
        coreCommand.registerSubCommand("kickall", kickAllCmd);
        coreCommand.registerSubCommand("restartalert", restartAlertCmd);
        coreCommand.registerSubCommand("ra", restartAlertCmd); // alias
        coreCommand.registerSubCommand("bc", broadcastCmd);
        coreCommand.registerSubCommand("broadcast", broadcastCmd); // alias

        // 6. Setup Warp Module
        this.warpManager = new WarpManager(this);
        WarpCommand warpCmd = new WarpCommand(this);

        // Register ke plugin.yml
        if (getCommand("warp") != null)
            getCommand("warp").setExecutor(warpCmd);
        if (getCommand("warps") != null)
            getCommand("warps").setExecutor(warpCmd);
        if (getCommand("setwarp") != null)
            getCommand("setwarp").setExecutor(warpCmd);
        if (getCommand("delwarp") != null)
            getCommand("delwarp").setExecutor(warpCmd);
        if (getCommand("setwarpicon") != null)
            getCommand("setwarpicon").setExecutor(warpCmd);

        // Register sebagai subcommand /nacore
        coreCommand.registerSubCommand("warp", warpCmd);
        coreCommand.registerSubCommand("warps", warpCmd);
        coreCommand.registerSubCommand("setwarp", warpCmd);
        coreCommand.registerSubCommand("delwarp", warpCmd);
        coreCommand.registerSubCommand("setwarpicon", warpCmd);

        // 7. Setup Trader Module (Puzzle Logic Disini)
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getLogger().info("Citizens ditemukan. Mengaktifkan Trader Module...");

            // A. Instansiasi Helper Class dulu (Currency & Editor)
            this.currencyManager = new CurrencyManager(this);
            this.tradeEditor = new TradeEditor(this);

            // B. Baru buat Manager (Membutuhkan Editor)
            this.traderManager = new TraderManager(this, tradeEditor);

            // C. Create Trader Command
            TraderCommand traderCmd = new TraderCommand(currencyManager, traderManager, tradeEditor);

            // Register ke plugin.yml
            if (getCommand("wanderingtrader") != null) {
                getCommand("wanderingtrader").setExecutor(traderCmd);
            }
            if (getCommand("givecurrency") != null) {
                getCommand("givecurrency").setExecutor(traderCmd);
            }
            if (getCommand("tradeeditor") != null) {
                getCommand("tradeeditor").setExecutor(traderCmd);
            }

            // Register sebagai subcommand /nacore
            coreCommand.registerSubCommand("wanderingtrader", traderCmd);
            coreCommand.registerSubCommand("trader", traderCmd); // alias pendek
            coreCommand.registerSubCommand("wt", traderCmd); // alias super pendek
            coreCommand.registerSubCommand("givecurrency", traderCmd);
            coreCommand.registerSubCommand("tradeeditor", traderCmd);

            // D. Register Listener (Membutuhkan Manager & Editor)
            getServer().getPluginManager().registerEvents(new TraderListener(traderManager, tradeEditor), this);

        } else {
            getLogger().warning("Citizens tidak ditemukan! Modul Trader dinonaktifkan.");
        }

        // 8. Register main /nacore command
        if (getCommand("nacore") != null)
            getCommand("nacore").setExecutor(coreCommand);

        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &asudah aktif sepenuhnya!"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatUtils.colorize("&c&lNaturalCore &idisabling..."));
        // Save logic jika perlu
        if (warpManager != null)
            warpManager.saveWarps();
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