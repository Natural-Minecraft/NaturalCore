package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.*;
import id.naturalsmp.naturalcore.chat.ChatListener;
import id.naturalsmp.naturalcore.chat.ChatPreviewGUI;
import id.naturalsmp.naturalcore.economy.VaultManager;
import id.naturalsmp.naturalcore.home.HomeGUI;
import id.naturalsmp.naturalcore.home.HomeManager;
import id.naturalsmp.naturalcore.home.HomeCommand;
import id.naturalsmp.naturalcore.spawn.SpawnCommand;
import id.naturalsmp.naturalcore.spawn.SpawnManager;
import id.naturalsmp.naturalcore.spawn.SpawnListener;

import id.naturalsmp.naturalcore.chat.GlobalNotificationListener;
import id.naturalsmp.naturalcore.listeners.GuiSoundListener;
import id.naturalsmp.naturalcore.season.*;
import id.naturalsmp.naturalcore.banner.*;
import id.naturalsmp.naturalcore.utility.EnvironmentCommand;

import id.naturalsmp.naturalcore.general.*;
import id.naturalsmp.naturalcore.teleport.TeleportManager;
import id.naturalsmp.naturalcore.utility.*;
import id.naturalsmp.naturalcore.sync.ConnectionManager;
import id.naturalsmp.naturalcore.sync.SyncCommand;

import id.naturalsmp.naturalcore.teleport.TeleportCommand;

import java.io.File;
import id.naturalsmp.naturalcore.teleport.TeleportListener;
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

import id.naturalsmp.naturalcore.general.RTPCommand;
import id.naturalsmp.naturalcore.chat.*;
import id.naturalsmp.naturalcore.general.StartCommand;
import id.naturalsmp.naturalcore.staff.StaffChatCommand;
import id.naturalsmp.naturalcore.staff.StaffModeCommand;
import id.naturalsmp.naturalcore.utility.CleanCommand;
import id.naturalsmp.naturalcore.utility.MenuCommand;
import id.naturalsmp.naturalcore.topup.TopupSuccessGUI;
import id.naturalsmp.naturalcore.topup.TopupCommand;

import id.naturalsmp.naturalcore.afk.AFKManager;
import id.naturalsmp.naturalcore.announcement.BroadcastManager;
import id.naturalsmp.naturalcore.chat.suffix.SuffixManager;
import id.naturalsmp.naturalcore.combat.CombatManager;
import id.naturalsmp.naturalcore.hud.HUDManager;
import id.naturalsmp.naturalcore.permissions.PermissionManager;
import id.naturalsmp.naturalcore.playtime.PlaytimeManager;
import id.naturalsmp.naturalcore.profile.ProfileGUI;
import id.naturalsmp.naturalcore.profile.ProfileManager;
import id.naturalsmp.naturalcore.season.SeasonResetManager;
import id.naturalsmp.naturalcore.staff.StaffGUI;
import id.naturalsmp.naturalcore.staff.StaffManager;
import id.naturalsmp.naturalcore.tier.TierGUI;
import id.naturalsmp.naturalcore.tier.TierManager;
import id.naturalsmp.naturalcore.trade.TradeGUI;
import id.naturalsmp.naturalcore.trade.TradeManager;
import id.naturalsmp.naturalcore.utility.BackupManager;
import id.naturalsmp.naturalcore.utility.NaturalLaggManager;
import id.naturalsmp.naturalcore.utility.ServerHealthManager;
import id.naturalsmp.naturalcore.utility.ServerStatusGUI;
import id.naturalsmp.naturalcore.utils.ConfigUpdater;
import id.naturalsmp.naturalcore.database.RankPriceDatabase;
import id.naturalsmp.naturalcore.media.MediaCommand;
import id.naturalsmp.naturalcore.media.MediaGUI;
import id.naturalsmp.naturalcore.media.MediaManager;
import id.naturalsmp.naturalcore.media.MediaSneakListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalCore extends JavaPlugin {

    private static NaturalCore instance;

    // Managers
    private VaultManager vaultManager;
    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private HomeManager homeManager;
    private id.naturalsmp.naturalcore.home.HomeGUI homeGUI;
    private VanishManager vanishManager;
    private TeleportManager teleportManager;
    private MessageManager messageManager;

    private SeasonManager seasonManager;
    private BannerManager bannerManager;
    private ProfileManager profileManager;
    private ProfileGUI profileGUI;
    private id.naturalsmp.naturalcore.chat.prefix.PrefixManager prefixManager;
    private SuffixManager suffixManager;
    private AFKManager afkManager;
    private TierManager tierManager;
    private TierGUI tierGUI;
    private id.naturalsmp.naturalcore.chat.ChatColorManager chatColorManager;
    private SeasonResetManager seasonResetManager;
    private HUDManager hudManager;
    private PermissionManager permissionManager;
    private StaffManager staffManager;
    private StaffGUI staffGUI;
    private NaturalLaggManager laggManager;
    private ServerHealthManager healthManager;
    private ServerStatusGUI statusGUI;
    private TradeManager tradeManager;
    private TradeGUI tradeGUI;
    private CombatManager combatManager;
    private PlaytimeManager playtimeManager;
    private BroadcastManager broadcastManager;
    private BackupManager backupManager;
    private RankPriceDatabase rankPriceDatabase;
    private id.naturalsmp.naturalcore.database.NaturalCoreDatabase coreDatabase;
    private ChatGameManager chatGameManager;
    private ConnectionManager connectionManager;
    
    private MediaManager mediaManager;
    private MediaGUI mediaGUI;
    private MediaSneakListener mediaSneakListener;

    @Override
    public void onEnable() {
        instance = this;

        // Startup log
        getLogger()
                .info(ChatUtils.colorize("&6&lNaturalCore &av" + getDescription().getVersion() + " &7Starting up..."));

        // Setup Config & Migration
        saveDefaultConfig();
        id.naturalsmp.naturalcore.utils.ConfigUpdater.updateConfig(this, "config.yml");
        id.naturalsmp.naturalcore.utils.ConfigUpdater.updateConfig(this, "messages.yml");
        id.naturalsmp.naturalcore.utils.ConfigUpdater.updateConfig(this, "commands.yml");

        // Initialize Core Database
        this.coreDatabase = new id.naturalsmp.naturalcore.database.NaturalCoreDatabase(this);
        if (coreDatabase.isEnabled()) {
            coreDatabase.connect();
        }
        generateEssentialsConfig(); // Generate reference for EssentialsX

        // Init text folder if not exists
        File textFolder = new File(getDataFolder(), "text");
        if (!textFolder.exists())
            textFolder.mkdirs();

        // Migrate or Save text resources
        saveTextResource("announcements.yml");

        saveTextResource("tips.yml");

        // 2B. Init Backup System (v2.0)
        this.backupManager = new id.naturalsmp.naturalcore.utility.BackupManager(this);
        this.seasonResetManager = new id.naturalsmp.naturalcore.season.SeasonResetManager(this);
        this.prefixManager = new id.naturalsmp.naturalcore.chat.prefix.PrefixManager(this);
        this.suffixManager = new id.naturalsmp.naturalcore.chat.suffix.SuffixManager(this);

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
        registerCmd("hub", spawnCmd);
        registerCmd("lobby", spawnCmd);
        getServer().getPluginManager().registerEvents(new SpawnListener(this), this);

        // 6. Home Module
        this.homeManager = new HomeManager(this);
        this.homeGUI = new HomeGUI(this);
        getServer().getPluginManager().registerEvents(homeGUI, this);

        HomeCommand homeCmd = new HomeCommand(homeManager, homeGUI);
        registerCmd("sethome", homeCmd);
        registerCmd("delhome", homeCmd);
        registerCmd("home", homeCmd);
        registerCmd("homes", homeCmd);

        // 7. Fun Module
        // Fun Module removed (moved to NaturalFun)

        // 9. Trader Module (Moved to NaturalFun)

        // 8. General / RTP
        RTPCommand rtpCmd = new RTPCommand();
        registerCmd("resource", rtpCmd);
        registerCmd("survival", rtpCmd);

        // 10. Admin Core Commands
        // Init GUI Listener
        NaturalCoreGUI adminGUI = new NaturalCoreGUI(this);
        getServer().getPluginManager().registerEvents(adminGUI, this);

        NaturalCoreCommand nacoreCmd = new NaturalCoreCommand(this);
        registerCmd("nacore", nacoreCmd);
        getCommand("nacore").setTabCompleter(nacoreCmd);

        registerCmd("kickall", new KickAllCommand());
        registerCmd("restartalert", new RestartAlertCommand());
        registerCmd("restart", new RestartAlertCommand());
        registerCmd("restartcancel", new RestartCancelCommand());


        // 11. Rank Price Database (MySQL)
        this.rankPriceDatabase = new RankPriceDatabase(this);
        this.rankPriceDatabase.fetchPrices();

        // 12. Ranks Module
        id.naturalsmp.naturalcore.admin.RankGUI rankGUI = new id.naturalsmp.naturalcore.admin.RankGUI(this);
        getServer().getPluginManager().registerEvents(rankGUI, this);
        registerCmd("ranks", new id.naturalsmp.naturalcore.admin.RankCommand(rankGUI));

        // 13. Permissions Module
        this.permissionManager = new id.naturalsmp.naturalcore.permissions.PermissionManager(this);

        // Rank Editor (v1.9.9)
        RankEditorGUI rankEditor = new RankEditorGUI(this);
        getServer().getPluginManager().registerEvents(rankEditor, this);

        // Social System (v1.9.9)
        SocialListener socialListener = new SocialListener(this);
        getServer().getPluginManager().registerEvents(socialListener, this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.listeners.PingListener(), this);

        registerCmd("rankeditor", (sender, cmd, label, args) -> {
            if (sender instanceof Player)
                rankEditor.openMainMenu((Player) sender);
            return true;
        });

        BroadcastCommand bcCmd = new BroadcastCommand();
        registerCmd("bc", bcCmd);
        registerCmd("bcworld", bcCmd);

        AdvancedChatCommand advChat = new AdvancedChatCommand();
        registerCmd("shout", advChat);
        registerCmd("sudo", advChat);

        AdminControlCommand adminCtrl = new AdminControlCommand(this);
        registerCmd("freeze", adminCtrl);
        registerCmd("kill", adminCtrl);
        getServer().getPluginManager().registerEvents(new FreezeListener(adminCtrl), this);

        getServer().getPluginManager()
                .registerEvents(new id.naturalsmp.naturalcore.listeners.CommandDisablerListener(this), this);

        // 11. Teleport Module
        this.teleportManager = new TeleportManager(this);
        TeleportCommand tpCmd = new TeleportCommand(teleportManager);
        registerCmd("tp", tpCmd);
        registerCmd("tphere", tpCmd);
        registerCmd("tpa", tpCmd);
        registerCmd("tpahere", tpCmd);
        registerCmd("tpaccept", tpCmd);
        registerCmd("tpdeny", tpCmd);
        registerCmd("otp", new id.naturalsmp.naturalcore.general.OfflineTPCommand(this));
        registerCmd("itp", new id.naturalsmp.naturalcore.teleport.InvestorTPCommand(this));

        // 12. Seasons Module
        this.seasonManager = new SeasonManager(this);
        registerCmd("season", new SeasonCommand(seasonManager));
        getServer().getPluginManager().registerEvents(new SeasonListener(seasonManager), this);

        // 13. Banner Module (Interactive Board)
        this.bannerManager = new BannerManager(this);
        BannerCommand bannerCmd = new BannerCommand(bannerManager);
        registerCmd("banner", bannerCmd);
        getCommand("banner").setTabCompleter(bannerCmd);
        getServer().getPluginManager().registerEvents(new BannerListener(bannerManager), this);

        // 12. Essentials Modules
        // A. Gamemode
        GamemodeCommand gmCmd = new GamemodeCommand();
        registerCmd("gamemode", gmCmd);
        registerCmd("gmc", gmCmd);
        registerCmd("gms", gmCmd);
        registerCmd("gma", gmCmd);
        registerCmd("gmsp", gmCmd);
        registerCmd("creative", gmCmd);
        // Note: /survival is now handled by RTPCommand exclusively
        registerCmd("adventure", gmCmd);
        registerCmd("spectator", gmCmd);

        // B. Inventory (Update: Split Self vs Admin)
        InventoryCommand invCmd = new InventoryCommand();
        registerCmd("invsee", invCmd);
        registerCmd("enderchest", invCmd); // Self (/ec)
        registerCmd("endersee", invCmd); // Admin (/endersee)

        // B2. Server Info Modules (New)
        ServerInfoCommand infoCmd = new ServerInfoCommand(this);
        registerCmd("list", infoCmd);
        registerCmd("lag", infoCmd);
        registerCmd("info", infoCmd);
        registerCmd("help", infoCmd);

        // B3. Item Management (New)
        ItemBuilderCommand itemCmd = new ItemBuilderCommand(this);
        registerCmd("give", itemCmd);
        registerCmd("itemname", itemCmd);
        registerCmd("lore", itemCmd);

        // B4. Fun Modules (New)
        FunCommand funCmd = new FunCommand(this);
        registerCmd("fireball", funCmd);
        registerCmd("firework", funCmd);
        registerCmd("jumpto", funCmd);

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
            if (sender instanceof Player)
                baltopGUI.openGUI((Player) sender);
            return true;
        });

        // 15. Moderation Module (Refactored)
        this.vanishManager = new VanishManager(this);

        ModerationCommand modCmd = new ModerationCommand(this);
        registerCmd("god", modCmd);
        registerCmd("vanish", modCmd);
        registerCmd("whois", modCmd);

        // Register Vanish Listener
        getServer().getPluginManager().registerEvents(this.vanishManager, this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);

        // 16. PlaceholderAPI Expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new NaturalCoreExpansion(this).register();
            getLogger().info("PlaceholderAPI ditemukan. Expansion terdaftar.");
        }

        // 17. Emoji System (Moved to NaturalFun)

        // 17C. AFK System
        registerCmd("afk", new id.naturalsmp.naturalcore.afk.AFKCommand(this));

        // 17B. ChatColor System (v1.7)
        this.chatColorManager = new ChatColorManager(this);
        registerCmd("chatcolor", new ChatColorCommand(this));
        getServer().getPluginManager().registerEvents(new ChatColorGUI(this), this);

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        // Modern Mention Tab Completion (v2.2.3)
        MentionTabManager mentionTab = new MentionTabManager(this);
        getServer().getPluginManager().registerEvents(mentionTab, this);
        mentionTab.initialSync();

        getServer().getPluginManager().registerEvents(new ChatPreviewGUI(), this);
        getServer().getPluginManager().registerEvents(new GlobalNotificationListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiSoundListener(), this);

        // Chat Games System
        this.chatGameManager = new ChatGameManager(this);
        this.chatGameManager.start();
        getLogger().info("Chat Games System: ENABLED");
        this.hudManager = new HUDManager(this);
        this.messageManager = new MessageManager();
        PrivateMessageCommand pmCmd = new PrivateMessageCommand(this);
        registerCmd("msg", pmCmd);
        registerCmd("reply", pmCmd);

        // NaturalLogger System (v1.9.9)
        id.naturalsmp.naturalcore.utility.NaturalLogger.init(this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.listeners.LogListener(), this);
        getLogger().info("NaturalLogger System: ENABLED");

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

        // 19B. New Utility Commands (v1.8.5)
        registerCmd("menu", new MenuCommand(this));
        EnvironmentCommand envCmd = new EnvironmentCommand();
        registerCmd("ptime", envCmd);
        registerCmd("pweather", envCmd);
        registerCmd("start", new StartCommand(this));

        // 20. v1.7 Utilities
        id.naturalsmp.naturalcore.utility.HologramUtil.init(this);
        this.laggManager = new id.naturalsmp.naturalcore.utility.NaturalLaggManager(this);
        this.staffManager = new id.naturalsmp.naturalcore.staff.StaffManager(this);
        this.staffGUI = new id.naturalsmp.naturalcore.staff.StaffGUI(this);
        this.healthManager = new id.naturalsmp.naturalcore.utility.ServerHealthManager(this);
        this.statusGUI = new id.naturalsmp.naturalcore.utility.ServerStatusGUI(this);
        this.tradeGUI = new id.naturalsmp.naturalcore.trade.TradeGUI(this);
        this.tradeManager = new id.naturalsmp.naturalcore.trade.TradeManager(this);
        this.combatManager = new id.naturalsmp.naturalcore.combat.CombatManager(this);
        this.playtimeManager = new id.naturalsmp.naturalcore.playtime.PlaytimeManager(this);
        this.broadcastManager = new id.naturalsmp.naturalcore.announcement.BroadcastManager(this);

        NaturalLaggCommand laggCmd = new NaturalLaggCommand(this);
        registerCmd("lagg", laggCmd);
        registerCmd("clean", new CleanCommand(this));
        registerCmd("staffmode", new StaffModeCommand(this));
        registerCmd("staffchat", new StaffChatCommand(this));
        registerCmd("staff", new id.naturalsmp.naturalcore.staff.StaffCommand(this, staffGUI));
        registerCmd("trade", new id.naturalsmp.naturalcore.trade.TradeCommand(this));
        registerCmd("playtime", new id.naturalsmp.naturalcore.playtime.PlaytimeCommand(this));
        registerCmd("back", new id.naturalsmp.naturalcore.general.BackCommand(this));

        // 21. Profile System (v1.8)
        this.profileManager = new id.naturalsmp.naturalcore.profile.ProfileManager(this);
        this.profileGUI = new id.naturalsmp.naturalcore.profile.ProfileGUI(this);
        registerCmd("profile", new id.naturalsmp.naturalcore.profile.ProfileCommand(this));
        getServer().getPluginManager().registerEvents(profileGUI, this);
        getLogger().info("Profile System: ENABLED (CoinsEngine: " + profileManager.hasCoinsEngine() + ")");

        // 22. Suffix System (v2.3)
        id.naturalsmp.naturalcore.chat.suffix.SuffixGUI suffixGUI = new id.naturalsmp.naturalcore.chat.suffix.SuffixGUI(this);
        getServer().getPluginManager().registerEvents(suffixGUI, this);
        registerCmd("suffix", new id.naturalsmp.naturalcore.chat.suffix.SuffixCommand(this));

        // 23. AFK System (v1.8)
        this.afkManager = new id.naturalsmp.naturalcore.afk.AFKManager(this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.afk.AFKListener(this), this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.staff.StaffListener(this), this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.staff.StaffToolListener(this),
                this);
        getServer().getPluginManager()
                .registerEvents(new id.naturalsmp.naturalcore.listeners.CommandOverrideListener(this), this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.combat.CombatListener(this), this);

        // 24. Tier System (v1.8)
        this.tierManager = new id.naturalsmp.naturalcore.tier.TierManager(this);
        this.tierGUI = new id.naturalsmp.naturalcore.tier.TierGUI(this); // Listeners inside
        getServer().getPluginManager().registerEvents(tierGUI, this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.tier.TierTopGUI(this), this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.tier.TierAdminGUI(this), this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.tier.TierEditorGUI(this, 0), this);
        registerCmd("tier", new id.naturalsmp.naturalcore.tier.TierCommand(this));
        registerCmd("tieradmin", new id.naturalsmp.naturalcore.tier.TierAdminCommand(this));
        registerCmd("chatview", new id.naturalsmp.naturalcore.chat.ChatSnapshotCommand());

        // Prefix System
        registerCmd("prefix", new id.naturalsmp.naturalcore.chat.prefix.PrefixCommand(this));
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.chat.prefix.PrefixGUI(this), this);

        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.teleport.PlayerDeathListener(this),
                this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);

        // 25. TopUp Notification Module
        TopupSuccessGUI topupGUI = new TopupSuccessGUI(this);
        getServer().getPluginManager().registerEvents(topupGUI, this);
        registerCmd("topupnotification", new TopupCommand(this, topupGUI));

        // 25.5. Social Links
        SocialCommand socialCmd = new SocialCommand();
        registerCmd("links", socialCmd);
        registerCmd("discord", socialCmd);
        registerCmd("instagram", socialCmd);
        registerCmd("whatsapp", socialCmd);
        registerCmd("appeal", socialCmd);
        registerCmd("wiki", socialCmd);
        registerCmd("tiktok", socialCmd);

        // 26. Sell Confirmation System (v2.2.2)
        SellConfirmCommand sellConfirmHandler = new SellConfirmCommand();
        registerCmd("sellall", sellConfirmHandler);
        registerCmd("sellhand", sellConfirmHandler);
        registerCmd("sellhandall", sellConfirmHandler);

        // 27. Sync Client
        if (getConfig().getBoolean("sync.enabled", false)) {
            String syncHost = getConfig().getString("sync.host", "127.0.0.1");
            int syncPort = getConfig().getInt("sync.port", 25666);
            String syncName = getConfig().getString("sync.name", "naturalcore");
            String syncPassword = getConfig().getString("sync.password", "defaultPassword");
            
            this.connectionManager = new ConnectionManager(this, syncHost, syncPort, syncName, syncPassword);
            registerCmd("sync", new SyncCommand(this));
            getLogger().info("Sync client initialized pointing to " + syncHost + ":" + syncPort);
        }

        // 28. Media Module
        this.mediaManager = new MediaManager(this);
        this.mediaGUI = new MediaGUI(this);
        MediaCommand mediaCmd = new MediaCommand(this);
        registerCmd("media", mediaCmd);
        registerCmd("creators", mediaCmd);
        registerCmd("creator", mediaCmd);
        registerCmd("youtuber", mediaCmd);
        registerCmd("tiktoker", mediaCmd);
        if (getCommand("media") != null) getCommand("media").setTabCompleter(mediaCmd);
        getServer().getPluginManager().registerEvents(mediaGUI, this);
        this.mediaSneakListener = new MediaSneakListener(this);
        getServer().getPluginManager().registerEvents(mediaSneakListener, this);

        // Selesai
        Bukkit.getConsoleSender().sendMessage(
                ChatUtils.colorize(
                    "\n&a===============\n" +
                    "&6 _   _       _                  _     &e____\n" +
                    "&6| \\ | | __ _| |_ _   _ _ __ __ _| |   &e/ ___|___  _ __ ___\n" +
                    "&6|  \\| |/ _` | __| | | | '__/ _` | |   &e| |   / _ \\| '__/ _ \\\n" +
                    "&6| |\\  | (_| | |_| |_| | | | (_| | |   &e| |__| (_) | | |  __/\n" +
                    "&6|_| \\_|\\__,_|\\__|\\__,_|_|    \\____\\___&e/|_|  \\___|\n" +
                    "&f       >> &6NaturalCore v" + getDescription().getVersion() + " Enabled! <<\n" +
                    "&a===============\n"
                )
        );
    }

    private void generateEssentialsConfig() {
        File file = new File(getDataFolder(), "essentials_config.yml");
        getLogger().info("Generating EssentialsX disabled-commands list...");

        org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
        java.util.List<String> disabled = new java.util.ArrayList<>();

        // Get all commands from plugin.yml
        java.util.Map<String, java.util.Map<String, Object>> cmdMap = getDescription().getCommands();
        if (cmdMap != null) {
            for (String cmd : cmdMap.keySet()) {
                disabled.add(cmd.toLowerCase());
                // Add aliases too
                Object aliases = cmdMap.get(cmd).get("aliases");
                if (aliases instanceof java.util.List) {
                    for (Object alias : (java.util.List<?>) aliases) {
                        disabled.add(alias.toString().toLowerCase());
                    }
                }
            }
        }

        // Remove duplicates and sort
        disabled = disabled.stream().distinct().sorted().collect(java.util.stream.Collectors.toList());

        config.set("disabled-commands", disabled);
        try {
            config.save(file);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTextResource(String name) {
        File file = new File(getDataFolder(), "text/" + name);
        if (!file.exists()) {
            saveResource("text/" + name, false);
        }
    }

    public void reload() {
        reloadConfig();
        id.naturalsmp.naturalcore.utils.ConfigUtils.reload();

        // Comprehensive Module Reload

        if (suffixManager != null)
            suffixManager.loadConfigs();
        if (tierManager != null)
            tierManager.loadConfigs();
        if (warpManager != null)
            warpManager.loadWarps();
        if (spawnManager != null)
            spawnManager.loadSpawn();
        if (chatColorManager != null)
            chatColorManager.load();
        if (hudManager != null)
            hudManager.reload();
        if (seasonManager != null)
            seasonManager.loadData();
        if (permissionManager != null)
            permissionManager.loadRanks();
        if (laggManager != null)
            laggManager.reload();
        if (mediaManager != null)
            mediaManager.loadData();
        if (broadcastManager != null)
            broadcastManager.reload();

        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &aAll system configurations reloaded successfully!"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatUtils.colorize("&c&lNaturalCore &idisabling..."));
        if (warpManager != null) {
            warpManager.saveWarps();
        }

        if (seasonManager != null) {
            seasonManager.saveData();
        }
        if (mediaManager != null) {
            mediaManager.saveData();
        }
        if (mediaSneakListener != null) {
            mediaSneakListener.stop();
        }
        if (bannerManager != null) {
            bannerManager.saveAll();
        }
        if (afkManager != null) {
            afkManager.cleanup();
        }
        if (chatGameManager != null) {
            chatGameManager.stop();
        }
        if (combatManager != null) {
            combatManager.stop();
        }
        if (broadcastManager != null) {
            broadcastManager.stop();
        }
        if (hudManager != null) {
            hudManager.stop();
        }
        if (healthManager != null) {
            healthManager.stop();
        }
        if (connectionManager != null) {
            connectionManager.shutdown();
        }
        if (playtimeManager != null) {
            playtimeManager.stop();
        }
        if (laggManager != null) {
            laggManager.stop();
        }
        if (id.naturalsmp.naturalcore.utility.NaturalLogger.getInstance() != null) {
            id.naturalsmp.naturalcore.utility.NaturalLogger.getInstance().stop();
        }
    }

    // --- GETTERS ---
    public static NaturalCore getInstance() {
        return instance;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public SuffixManager getSuffixManager() {
        return suffixManager;
    }

    public id.naturalsmp.naturalcore.chat.prefix.PrefixManager getPrefixManager() {
        return prefixManager;
    }

    public TierManager getTierManager() {
        return tierManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public id.naturalsmp.naturalcore.profile.ProfileManager getProfileManager() {
        return profileManager;
    }

    public id.naturalsmp.naturalcore.profile.ProfileGUI getProfileGUI() {
        return profileGUI;
    }

    public id.naturalsmp.naturalcore.home.HomeGUI getHomeGUI() {
        return homeGUI;
    }

    public id.naturalsmp.naturalcore.afk.AFKManager getAFKManager() {
        return afkManager;
    }

    public id.naturalsmp.naturalcore.utility.ServerStatusGUI getStatusGUI() {
        return statusGUI;
    }

    public id.naturalsmp.naturalcore.trade.TradeManager getTradeManager() {
        return tradeManager;
    }

    public id.naturalsmp.naturalcore.trade.TradeGUI getTradeGUI() {
        return tradeGUI;
    }

    public id.naturalsmp.naturalcore.combat.CombatManager getCombatManager() {
        return combatManager;
    }

    public id.naturalsmp.naturalcore.playtime.PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }

    public id.naturalsmp.naturalcore.announcement.BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

    public id.naturalsmp.naturalcore.tier.TierGUI getTierGUI() {
        return tierGUI;
    }

    public id.naturalsmp.naturalcore.season.SeasonResetManager getSeasonResetManager() {
        return seasonResetManager;
    }

    public HUDManager getHudManager() {
        return hudManager;
    }

    // --- CHAT COLOR ---

    public id.naturalsmp.naturalcore.chat.ChatColorManager getChatColorManager() {
        return chatColorManager;
    }

    public id.naturalsmp.naturalcore.permissions.PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public id.naturalsmp.naturalcore.database.NaturalCoreDatabase getCoreDatabase() {
        return coreDatabase;
    }


    public id.naturalsmp.naturalcore.utility.NaturalLaggManager getLaggManager() {
        return laggManager;
    }

    public id.naturalsmp.naturalcore.staff.StaffManager getStaffManager() {
        return staffManager;
    }

    public id.naturalsmp.naturalcore.utility.ServerHealthManager getHealthManager() {
        return healthManager;
    }

    public id.naturalsmp.naturalcore.utility.BackupManager getBackupManager() {
        return backupManager;
    }

    public ChatGameManager getChatGameManager() {
        return chatGameManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public RankPriceDatabase getRankPriceDatabase() {
        return rankPriceDatabase;
    }

    public id.naturalsmp.naturalcore.media.MediaManager getMediaManager() {
        return mediaManager;
    }

    public id.naturalsmp.naturalcore.media.MediaGUI getMediaGUI() {
        return mediaGUI;
    }

    // --- HELPER UNTUK MENCEGAH CRASH ---
    private void registerCmd(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("SKIPPING COMMAND: '" + name + "' (Tidak ditemukan di plugin.yml, tapi server aman)");
        }
    }

    public void saveDungeonStats(java.util.UUID uuid, String dungeon, String difficulty, long time, String party) {
        getLogger().info(ChatUtils.colorize("&a[DungeonStats] &fPlayer &e" + uuid + " &ffinished &6" + dungeon + " &7("
                + difficulty + ") &fin &b" + (time / 1000) + "s&f. Party: " + party));
    }
}