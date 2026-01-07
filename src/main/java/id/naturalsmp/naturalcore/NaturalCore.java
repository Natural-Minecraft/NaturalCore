package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.BroadcastCommand;
import id.naturalsmp.naturalcore.admin.KickAllCommand;
import id.naturalsmp.naturalcore.admin.NaturalCoreCommand;
import id.naturalsmp.naturalcore.admin.RestartAlertCommand;
import id.naturalsmp.naturalcore.admin.GiveBalCommand;
import id.naturalsmp.naturalcore.economy.VaultManager;

import id.naturalsmp.naturalcore.home.HomeGUI;
import id.naturalsmp.naturalcore.home.HomeManager;
import id.naturalsmp.naturalcore.home.HomeCommand;

import id.naturalsmp.naturalcore.spawn.SpawnCommand;
import id.naturalsmp.naturalcore.spawn.SpawnManager;

import id.naturalsmp.naturalcore.trader.CurrencyManager;
import id.naturalsmp.naturalcore.trader.TradeEditor;
import id.naturalsmp.naturalcore.trader.TraderCommand;
import id.naturalsmp.naturalcore.trader.TraderListener;
import id.naturalsmp.naturalcore.trader.TraderManager;

import id.naturalsmp.naturalcore.teleport.TeleportManager;
import id.naturalsmp.naturalcore.teleport.TeleportCommand;

import id.naturalsmp.naturalcore.utils.ChatUtils;

import id.naturalsmp.naturalcore.warp.WarpCommand;
import id.naturalsmp.naturalcore.warp.WarpManager;

import id.naturalsmp.naturalcore.gamemode.GamemodeCommand;
import id.naturalsmp.naturalcore.inventory.InventoryCommand;

import id.naturalsmp.naturalcore.utility.PlayerUtilCommand;
import id.naturalsmp.naturalcore.utility.MenuUtilCommand;

import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalCore extends JavaPlugin {

    private static NaturalCore instance;
    private VaultManager vaultManager;

    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private HomeManager homeManager;

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

        // Setup Economy (Vault)
        this.vaultManager = new VaultManager(this);
        if (!vaultManager.setupEconomy()) {
            getLogger().warning("Vault/Economy tidak ditemukan!");
        } else {
            // Register Command
            getCommand("givebal").setExecutor(new GiveBalCommand());
        }

        // Setup Warp Module
        this.warpManager = new WarpManager(this);
        WarpCommand warpCmd = new WarpCommand(this);

        if (getCommand("warp") != null) getCommand("warp").setExecutor(warpCmd);
        if (getCommand("warps") != null) getCommand("warps").setExecutor(warpCmd);
        if (getCommand("setwarp") != null) getCommand("setwarp").setExecutor(warpCmd);
        if (getCommand("delwarp") != null) getCommand("delwarp").setExecutor(warpCmd);
        if (getCommand("setwarpicon") != null) getCommand("setwarpicon").setExecutor(warpCmd);

        // Spawn Command
        this.spawnManager = new SpawnManager(this);
        SpawnCommand spawnCmd = new SpawnCommand(spawnManager);

        if (getCommand("spawn") != null) getCommand("spawn").setExecutor(spawnCmd);
        if (getCommand("setspawn") != null) getCommand("setspawn").setExecutor(spawnCmd);

        // Home
        this.homeManager = new HomeManager(this);
        HomeGUI homeGUI = new HomeGUI(this);
        getServer().getPluginManager().registerEvents(homeGUI, this);

        HomeCommand homeCmd = new HomeCommand(homeManager, homeGUI);

        if (getCommand("sethome") != null) getCommand("sethome").setExecutor(homeCmd);
        if (getCommand("delhome") != null) getCommand("delhome").setExecutor(homeCmd);
        if (getCommand("home") != null) getCommand("home").setExecutor(homeCmd);
        if (getCommand("homes") != null) getCommand("homes").setExecutor(homeCmd);

        // Trader (Citizens Check)
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

        // Admin Commands
        if (getCommand("kickall") != null) getCommand("kickall").setExecutor(new KickAllCommand());
        if (getCommand("restartalert") != null) getCommand("restartalert").setExecutor(new RestartAlertCommand());
        if (getCommand("bc") != null) getCommand("bc").setExecutor(new BroadcastCommand());

        // Register MAIN Dashboard Command (/nacore)
        // Ini akan membuka GUI Menu Admin
        if (getCommand("nacore") != null) {
            getCommand("nacore").setExecutor(new NaturalCoreCommand(this));
        }

        // Setup Teleport Module
        TeleportManager tpManager = new TeleportManager(this);
        TeleportCommand tpCmd = new TeleportCommand(tpManager);

        getCommand("tp").setExecutor(tpCmd);
        getCommand("tphere").setExecutor(tpCmd);
        getCommand("tpa").setExecutor(tpCmd);
        getCommand("tpahere").setExecutor(tpCmd);
        getCommand("tpaccept").setExecutor(tpCmd);
        getCommand("tpdeny").setExecutor(tpCmd);

        // A. Gamemode
        GamemodeCommand gmCmd = new GamemodeCommand();
        getCommand("gamemode").setExecutor(gmCmd);
        getCommand("gmc").setExecutor(gmCmd);
        getCommand("gms").setExecutor(gmCmd);
        getCommand("gma").setExecutor(gmCmd);
        getCommand("gmsp").setExecutor(gmCmd);

        // B. Inventory
        InventoryCommand invCmd = new InventoryCommand();
        getCommand("invsee").setExecutor(invCmd);
        getCommand("enderchest").setExecutor(invCmd);

        // C. Utility (Fly, Heal, Feed)
        PlayerUtilCommand playerUtil = new PlayerUtilCommand();
        getCommand("fly").setExecutor(playerUtil);
        getCommand("heal").setExecutor(playerUtil);
        getCommand("feed").setExecutor(playerUtil);

        // D. Utility (Menu)
        MenuUtilCommand menuUtil = new MenuUtilCommand();
        getCommand("trash").setExecutor(menuUtil);
        getCommand("craft").setExecutor(menuUtil);

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