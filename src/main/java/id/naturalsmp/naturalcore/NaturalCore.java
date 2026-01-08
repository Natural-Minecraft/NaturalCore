package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.NaturalCoreCommand;
import id.naturalsmp.naturalcore.NaturalCoreExpansion;
import id.naturalsmp.naturalcore.admin.BroadcastCommand;
import id.naturalsmp.naturalcore.admin.KickAllCommand;
import id.naturalsmp.naturalcore.admin.RestartAlertCommand;
import id.naturalsmp.naturalcore.admin.GiveBalCommand;
import id.naturalsmp.naturalcore.chat.ChatListener;
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
import id.naturalsmp.naturalcore.moderation.VanishManager;
import id.naturalsmp.naturalcore.moderation.VanishListener;
import id.naturalsmp.naturalcore.moderation.ModerationCommand;
import id.naturalsmp.naturalcore.fun.FunCommand;
import id.naturalsmp.naturalcore.fun.FunListener;
import id.naturalsmp.naturalcore.general.RTPCommand;
import id.naturalsmp.naturalcore.chat.MessageManager;
import id.naturalsmp.naturalcore.chat.PrivateMessageCommand;
import id.naturalsmp.naturalcore.utility.WorldUtilCommand;
import id.naturalsmp.naturalcore.utility.EssentialPerksCommand;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalCore extends JavaPlugin {

    private static NaturalCore instance;

    // Managers
    private VaultManager vaultManager;
    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private HomeManager homeManager;
    private VanishManager vanishManager;
    private TeleportManager teleportManager;
    private TraderManager traderManager;
    private TradeEditor tradeEditor;
    private CurrencyManager currencyManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Startup Log
        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &aStarting up..."));

        // 2. Setup Config
        saveDefaultConfig();

        // 3. Setup Vault (Economy & Chat)
        this.vaultManager = new VaultManager(this);

        // Setup Economy
        if (!vaultManager.setupEconomy()) {
            getLogger().warning("Vault/Economy tidak ditemukan! Fitur uang terbatas.");
        } else {
            registerCmd("givebal", new GiveBalCommand());
        }

        // Setup Chat (LuckPerms Link)
        if (vaultManager.setupChat()) {
            getLogger().info("Vault Chat Hooked! (Prefix/Suffix enabled)");
        } else {
            getLogger().warning("Vault Chat tidak ditemukan. Prefix/Suffix tidak akan muncul.");
        }

        // 4. Warp Module
        this.warpManager = new WarpManager(this);
        WarpCommand warpCmd = new WarpCommand(this);
        registerCmd("warp", warpCmd);
        registerCmd("warps", warpCmd);
        registerCmd("setwarp", warpCmd);
        registerCmd("delwarp", warpCmd);
        registerCmd("setwarpicon", warpCmd);

        // 5. Spawn Module
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

        // 9. Trader Module (Citizens Check)
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getLogger().info("Citizens ditemukan. Mengaktifkan Trader Module...");

            this.currencyManager = new CurrencyManager(this);
            this.tradeEditor = new TradeEditor(this);
            this.traderManager = new TraderManager(this, tradeEditor);

            TraderCommand traderCmd = new TraderCommand(currencyManager, traderManager, tradeEditor);
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

        // 10. Admin Core Commands
        registerCmd("nacore", new NaturalCoreCommand(this));
        registerCmd("kickall", new KickAllCommand());
        registerCmd("restartalert", new RestartAlertCommand());
        registerCmd("bc", new BroadcastCommand());

        // 11. Teleport Module
        this.teleportManager = new TeleportManager(this);
        TeleportCommand tpCmd = new TeleportCommand(teleportManager);
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

        // B. Inventory (Update: Split Self vs Admin)
        InventoryCommand invCmd = new InventoryCommand();
        registerCmd("invsee", invCmd);
        registerCmd("enderchest", invCmd); // Self (/ec)
        registerCmd("endersee", invCmd);   // Admin (/endersee)

        // C. Utility (Player)
        PlayerUtilCommand playerUtil = new PlayerUtilCommand();
        registerCmd("fly", playerUtil);
        registerCmd("heal", playerUtil);
        registerCmd("feed", playerUtil);

        // D. Utility (Menu)
        MenuUtilCommand menuUtil = new MenuUtilCommand();
        registerCmd("trash", menuUtil);
        registerCmd("craft", menuUtil);
        registerCmd("anvil", menuUtil);

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

        // 15. Moderation Module (Refactored)
        this.vanishManager = new VanishManager(this);

        ModerationCommand modCmd = new ModerationCommand(this);
        registerCmd("god", modCmd);
        registerCmd("vanish", modCmd);
        registerCmd("whois", modCmd);

        // Register Vanish Listener (Realtime Hide)
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);

        // 16. PlaceholderAPI Expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new NaturalCoreExpansion(this).register();
            getLogger().info("PlaceholderAPI ditemukan. Expansion terdaftar.");
        }

        // 17. Messaging System (NEW)
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        this.messageManager = new MessageManager();
        PrivateMessageCommand pmCmd = new PrivateMessageCommand(this);
        registerCmd("msg", pmCmd);
        registerCmd("reply", pmCmd);

        // 18. World Utils (NEW)
        WorldUtilCommand worldCmd = new WorldUtilCommand();
        registerCmd("day", worldCmd);
        registerCmd("night", worldCmd);
        registerCmd("sun", worldCmd);
        registerCmd("rain", worldCmd);

        // 19. Perks (Hat, Repair, Nick) (NEW)
        EssentialPerksCommand perksCmd = new EssentialPerksCommand();
        registerCmd("hat", perksCmd);
        registerCmd("repair", perksCmd);
        registerCmd("nick", perksCmd);

        // Selesai
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
    public SpawnManager getSpawnManager() { return spawnManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public VanishManager getVanishManager() { return vanishManager; }
    public MessageManager getMessageManager() { return messageManager; }

    // --- HELPER UNTUK MENCEGAH CRASH ---
    private void registerCmd(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("SKIPPING COMMAND: '" + name + "' (Tidak ditemukan di plugin.yml, tapi server aman)");
        }
    }
}