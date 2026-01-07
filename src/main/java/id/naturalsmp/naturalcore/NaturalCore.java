package id.naturalsmp.naturalcore;
import id.naturalsmp.naturalcore.NaturalCoreCommand;

import id.naturalsmp.naturalcore.admin.BroadcastCommand;
import id.naturalsmp.naturalcore.admin.KickAllCommand;
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

import id.naturalsmp.naturalcore.economy.EconomyCommand;
import id.naturalsmp.naturalcore.economy.BaltopGUI;

import id.naturalsmp.naturalcore.moderation.PunishmentManager;
import id.naturalsmp.naturalcore.moderation.ModerationCommand;

import id.naturalsmp.naturalcore.fun.FunCommand;
import id.naturalsmp.naturalcore.fun.FunListener;
import id.naturalsmp.naturalcore.general.RTPCommand;

import id.naturalsmp.naturalcore.moderation.GodVanishCommand;

import org.bukkit.entity.Player;
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

    private PunishmentManager punishmentManager;

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
        // Fun
        FunCommand funCmd = new FunCommand();
        getCommand("gg").setExecutor(funCmd);
        getCommand("noob").setExecutor(funCmd);
        getServer().getPluginManager().registerEvents(new FunListener(), this);
        // General
        RTPCommand rtpCmd = new RTPCommand();
        getCommand("resource").setExecutor(rtpCmd);
        getCommand("survival").setExecutor(rtpCmd);

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

        getCommand("nacore").setExecutor(new NaturalCoreCommand(this));

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

        // 9. Setup Economy
        EconomyCommand ecoCmd = new EconomyCommand();
        getCommand("bal").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);
        getCommand("setbal").setExecutor(ecoCmd); // Dan lain-lain

        // GUI Baltop
        BaltopGUI baltopGUI = new BaltopGUI();
        getServer().getPluginManager().registerEvents(baltopGUI, this);
        getCommand("baltop").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) baltopGUI.openGUI((Player) sender);
            return true;
        });

        // 10. Setup Moderation
        this.punishmentManager = new PunishmentManager(this);
        ModerationCommand modCmd = new ModerationCommand(this);
        GodVanishCommand gvCmd = new GodVanishCommand();

        getCommand("god").setExecutor(gvCmd);
        getCommand("vanish").setExecutor(gvCmd);
        getCommand("whois").setExecutor(gvCmd);

        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.moderation.ModerationListener(this), this);
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

    public id.naturalsmp.naturalcore.home.HomeManager getHomeManager() {
        return homeManager;
    }

    public id.naturalsmp.naturalcore.moderation.PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }
}