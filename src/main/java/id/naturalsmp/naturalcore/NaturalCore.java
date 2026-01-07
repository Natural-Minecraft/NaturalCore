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

        // 3. Setup Economy (Vault)
        this.vaultManager = new VaultManager(this);
        if (!vaultManager.setupEconomy()) {
            getLogger().warning("Vault/Economy tidak ditemukan! Beberapa fitur mungkin error.");
        } else {
            registerCmd("givebal", new GiveBalCommand());
        }

        // 4. Setup Warp Module
        this.warpManager = new WarpManager(this);
        WarpCommand warpCmd = new WarpCommand(this);
        registerCmd("warp", warpCmd);
        registerCmd("warps", warpCmd);
        registerCmd("setwarp", warpCmd);
        registerCmd("delwarp", warpCmd);
        registerCmd("setwarpicon", warpCmd);

        // 5. Spawn Command
        this.spawnManager = new SpawnManager(this);
        SpawnCommand spawnCmd = new SpawnCommand(spawnManager);
        registerCmd("spawn", spawnCmd);
        registerCmd("setspawn", spawnCmd);

        // 6. Home Module
        this.homeManager = new HomeManager(this);
        HomeGUI homeGUI = new HomeGUI(this);
        getServer().getPluginManager().registerEvents(homeGUI, this);

        HomeCommand homeCmd = new HomeCommand(homeManager, homeGUI);
        registerCmd("sethome", homeCmd);
        registerCmd("delhome", homeCmd);
        registerCmd("home", homeCmd);
        registerCmd("homes", homeCmd);

        // 7. Fun Module
        FunCommand funCmd = new FunCommand();
        registerCmd("gg", funCmd);
        registerCmd("noob", funCmd);
        getServer().getPluginManager().registerEvents(new FunListener(), this);

        // 8. General / RTP
        RTPCommand rtpCmd = new RTPCommand();
        registerCmd("resource", rtpCmd);
        registerCmd("survival", rtpCmd);

        // 9. Trader (Citizens Check)
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getLogger().info("Citizens ditemukan. Mengaktifkan Trader Module...");

            this.currencyManager = new CurrencyManager(this);
            this.tradeEditor = new TradeEditor(this);
            this.traderManager = new TraderManager(this, tradeEditor);

            TraderCommand traderCmd = new TraderCommand(currencyManager, traderManager, tradeEditor);

            // Gunakan "wt" sesuai key di plugin.yml yang baru
            registerCmd("wt", traderCmd);
            registerCmd("settrader", traderCmd);
            registerCmd("resettrader", traderCmd);
            registerCmd("setharga", traderCmd);
            registerCmd("setallharga", traderCmd);
            registerCmd("setallstok", traderCmd);

            getServer().getPluginManager().registerEvents(new TraderListener(traderManager, tradeEditor), this);
        } else {
            getLogger().warning("Citizens tidak ditemukan! Modul Trader dinonaktifkan.");
        }

        // 10. Admin Commands
        registerCmd("nacore", new NaturalCoreCommand(this));
        registerCmd("kickall", new KickAllCommand());
        registerCmd("restartalert", new RestartAlertCommand());
        registerCmd("bc", new BroadcastCommand());

        // 11. Setup Teleport Module
        TeleportManager tpManager = new TeleportManager(this);
        TeleportCommand tpCmd = new TeleportCommand(tpManager);
        registerCmd("tp", tpCmd);
        registerCmd("tphere", tpCmd);
        registerCmd("tpa", tpCmd);
        registerCmd("tpahere", tpCmd);
        registerCmd("tpaccept", tpCmd);
        registerCmd("tpdeny", tpCmd);

        // 12. Essentials Modules
        // A. Gamemode
        GamemodeCommand gmCmd = new GamemodeCommand();
        registerCmd("gamemode", gmCmd);
        registerCmd("gmc", gmCmd);
        registerCmd("gms", gmCmd);
        registerCmd("gma", gmCmd);
        registerCmd("gmsp", gmCmd);

        // B. Inventory
        InventoryCommand invCmd = new InventoryCommand();
        registerCmd("invsee", invCmd);
        registerCmd("enderchest", invCmd);

        // C. Utility (Player)
        PlayerUtilCommand playerUtil = new PlayerUtilCommand();
        registerCmd("fly", playerUtil);
        registerCmd("heal", playerUtil);
        registerCmd("feed", playerUtil);

        // D. Utility (Menu)
        MenuUtilCommand menuUtil = new MenuUtilCommand();
        registerCmd("trash", menuUtil);
        registerCmd("craft", menuUtil);

        // 13. Economy Module
        EconomyCommand ecoCmd = new EconomyCommand();
        registerCmd("bal", ecoCmd);
        registerCmd("pay", ecoCmd);
        registerCmd("setbal", ecoCmd);
        registerCmd("takebal", ecoCmd);

        // 14. Baltop GUI
        BaltopGUI baltopGUI = new BaltopGUI();
        getServer().getPluginManager().registerEvents(baltopGUI, this);
        registerCmd("baltop", (sender, cmd, label, args) -> {
            if (sender instanceof Player) baltopGUI.openGUI((Player) sender);
            return true;
        });

        // 15. Moderation Module (Lite)
        this.punishmentManager = new PunishmentManager(this); // Tetap di-init untuk GodVanishCommand
        GodVanishCommand gvCmd = new GodVanishCommand();
        registerCmd("god", gvCmd);
        registerCmd("vanish", gvCmd);
        registerCmd("whois", gvCmd);

        // Listener Moderation (untuk God/Vanish logic)
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.moderation.ModerationListener(this), this);

        getLogger().info(ChatUtils.colorize("&6&lNaturalCore v" + getDescription().getVersion() + " &asudah aktif sepenuhnya!"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatUtils.colorize("&c&lNaturalCore &idisabling..."));
        if (warpManager != null) {
            warpManager.saveWarps();
        }
    }

    // --- GETTERS ---
    public static NaturalCore getInstance() { return instance; }
    public VaultManager getVaultManager() { return vaultManager; }
    public WarpManager getWarpManager() { return warpManager; }
    public TraderManager getTraderManager() { return traderManager; }
    public HomeManager getHomeManager() { return homeManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public SpawnManager getSpawnManager() { return spawnManager; }

    // --- HELPER UNTUK MENCEGAH CRASH ---
    private void registerCmd(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            // Peringatan ini akan muncul di console jika ada command yang belum ada di plugin.yml
            // Tapi server TIDAK akan crash.
            getLogger().warning("SKIPPING COMMAND: '" + name + "' (Tidak ditemukan di plugin.yml)");
        }
    }
}