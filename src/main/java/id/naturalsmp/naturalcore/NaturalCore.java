package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.NaturalCoreCommand;
import id.naturalsmp.naturalcore.NaturalCoreExpansion;
import id.naturalsmp.naturalcore.admin.*;
import id.naturalsmp.naturalcore.chat.ChatListener;
import id.naturalsmp.naturalcore.chat.ChatTabCompleter;
import id.naturalsmp.naturalcore.chat.ChatPreviewGUI;
import id.naturalsmp.naturalcore.economy.VaultManager;
import id.naturalsmp.naturalcore.home.HomeGUI;
import id.naturalsmp.naturalcore.home.HomeManager;
import id.naturalsmp.naturalcore.home.HomeCommand;
import id.naturalsmp.naturalcore.spawn.SpawnCommand;
import id.naturalsmp.naturalcore.spawn.SpawnManager;
import id.naturalsmp.naturalcore.spawn.SpawnListener;
import id.naturalsmp.naturalcore.chat.MentionListener;
import id.naturalsmp.naturalcore.chat.GlobalNotificationListener;
import id.naturalsmp.naturalcore.listeners.GuiSoundListener;
import id.naturalsmp.naturalcore.season.*;
import id.naturalsmp.naturalcore.banner.*;
import id.naturalsmp.naturalcore.utility.EnvironmentCommand;
import id.naturalsmp.naturalcore.utility.MenuCommand;
import id.naturalsmp.naturalcore.general.StartCommand;
import id.naturalsmp.naturalcore.teleport.TeleportManager;

import id.naturalsmp.naturalcore.teleport.TeleportCommand;
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
import id.naturalsmp.naturalcore.chat.EmojiManager;
import id.naturalsmp.naturalcore.chat.EmojiCommand;
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
    private MessageManager messageManager;
    private EmojiManager emojiManager;
    private SeasonManager seasonManager;
    private BannerManager bannerManager;
    private id.naturalsmp.naturalcore.profile.ProfileManager profileManager;
    private id.naturalsmp.naturalcore.profile.ProfileGUI profileGUI;
    private id.naturalsmp.naturalcore.chat.tags.TagsManager tagsManager;
    private id.naturalsmp.naturalcore.afk.AFKManager afkManager;
    private id.naturalsmp.naturalcore.tier.TierManager tierManager;
    private id.naturalsmp.naturalcore.tier.TierGUI tierGUI;
    private id.naturalsmp.naturalcore.chat.ChatColorManager chatColorManager;
    private id.naturalsmp.naturalcore.season.SeasonResetManager seasonResetManager;
    private id.naturalsmp.naturalcore.hud.HUDManager hudManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Startup Log
        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &aStarting up..."));

        // 2. Setup Config & Migration
        saveDefaultConfig();
        id.naturalsmp.naturalcore.utils.ConfigUpdater.updateConfig(this, "config.yml");
        id.naturalsmp.naturalcore.utils.ConfigUpdater.updateConfig(this, "messages.yml");

        // Init Managers
        this.seasonResetManager = new id.naturalsmp.naturalcore.season.SeasonResetManager(this);
        this.tagsManager = new id.naturalsmp.naturalcore.chat.tags.TagsManager(this);

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
        registerCmd("spawn", spawnCmd);
        registerCmd("setspawn", spawnCmd);
        getServer().getPluginManager().registerEvents(new SpawnListener(this), this);

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
        registerCmd("bc", new BroadcastCommand());

        // 11. Teleport Module
        this.teleportManager = new TeleportManager(this);
        TeleportCommand tpCmd = new TeleportCommand(teleportManager);
        registerCmd("tp", tpCmd);
        registerCmd("tphere", tpCmd);
        registerCmd("tpa", tpCmd);
        registerCmd("tpahere", tpCmd);

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
        registerCmd("endersee", invCmd); // Admin (/endersee)

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

        // Register Vanish Listener (Realtime Hide)
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);

        // 16. PlaceholderAPI Expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new NaturalCoreExpansion(this).register();
            getLogger().info("PlaceholderAPI ditemukan. Expansion terdaftar.");
        }

        // 17. Emoji System (NEW - Terinspirasi ChatEmojis)
        this.emojiManager = new EmojiManager(this);
        registerCmd("emoji", new EmojiCommand(this));
        getLogger().info("Emoji System: ENABLED");
        // Emoji GUI Register
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.chat.EmojiGUI(this), this);

        // 17B. ChatColor System (v1.7)
        this.chatColorManager = new id.naturalsmp.naturalcore.chat.ChatColorManager(this);
        registerCmd("chatcolor", new id.naturalsmp.naturalcore.chat.ChatColorCommand(this));
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.chat.ChatColorGUI(this), this);

        // 18. Messaging System
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        // MentionListener is now integrated into ChatListener (Adventure API)
        getServer().getPluginManager().registerEvents(new ChatTabCompleter(), this);
        getServer().getPluginManager().registerEvents(new ChatPreviewGUI(), this);
        getServer().getPluginManager().registerEvents(new GlobalNotificationListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiSoundListener(), this);
        this.hudManager = new id.naturalsmp.naturalcore.hud.HUDManager(this);
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

        // 19B. New Utility Commands (v1.8.5)
        registerCmd("menu", new id.naturalsmp.naturalcore.utility.MenuCommand(this));
        EnvironmentCommand envCmd = new EnvironmentCommand();
        registerCmd("ptime", envCmd);
        registerCmd("pweather", envCmd);
        registerCmd("start", new id.naturalsmp.naturalcore.general.StartCommand(this));

        // 20. v1.7 Utilities
        registerCmd("clean", new id.naturalsmp.naturalcore.utility.CleanCommand());
        registerCmd("back", new id.naturalsmp.naturalcore.general.BackCommand(this));
        registerCmd("otp", new id.naturalsmp.naturalcore.general.OfflineTPCommand(this));

        // 21. Profile System (v1.8)
        this.profileManager = new id.naturalsmp.naturalcore.profile.ProfileManager(this);
        this.profileGUI = new id.naturalsmp.naturalcore.profile.ProfileGUI(this);
        registerCmd("profile", new id.naturalsmp.naturalcore.profile.ProfileCommand(this));
        getServer().getPluginManager().registerEvents(profileGUI, this);
        getLogger().info("Profile System: ENABLED (CoinsEngine: " + profileManager.hasCoinsEngine() + ")");

        // 22. Tags System (v1.8)
        id.naturalsmp.naturalcore.chat.tags.TagsGUI tagsGUI = new id.naturalsmp.naturalcore.chat.tags.TagsGUI(this);
        getServer().getPluginManager().registerEvents(tagsGUI, this);
        registerCmd("tags", new id.naturalsmp.naturalcore.chat.tags.TagsCommand(this));

        // 23. AFK System (v1.8)
        this.afkManager = new id.naturalsmp.naturalcore.afk.AFKManager(this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.afk.AFKListener(this), this);

        // 24. Tier System (v1.8)
        this.tierManager = new id.naturalsmp.naturalcore.tier.TierManager(this);
        this.tierGUI = new id.naturalsmp.naturalcore.tier.TierGUI(this); // Listeners inside
        getServer().getPluginManager().registerEvents(tierGUI, this);
        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.tier.TierTopGUI(this), this);
        registerCmd("tier", new id.naturalsmp.naturalcore.tier.TierCommand(this));
        registerCmd("chatview", new id.naturalsmp.naturalcore.chat.ChatSnapshotCommand());

        getServer().getPluginManager().registerEvents(new id.naturalsmp.naturalcore.teleport.PlayerDeathListener(this),
                this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);

        // Selesai
        getLogger().info(
                ChatUtils.colorize("&6&lNaturalCore v" + getDescription().getVersion() + " &asudah aktif sepenuhnya!"));
    }

    public void reload() {
        reloadConfig();
        id.naturalsmp.naturalcore.utils.ConfigUtils.reload();
        if (emojiManager != null)
            emojiManager.loadEmojis();
        if (tagsManager != null)
            tagsManager.loadTags();
        if (chatColorManager != null)
            chatColorManager.load();
        if (hudManager != null)
            hudManager.reload();
        getLogger().info(ChatUtils.colorize("&6&lNaturalCore &aReloaded successfully!"));
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
        if (bannerManager != null) {
            bannerManager.saveAll();
        }
        if (afkManager != null) {
            afkManager.cleanup();
        }
    }

    // --- GETTERS ---
    public static NaturalCore getInstance() {
        return instance;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public id.naturalsmp.naturalcore.chat.EmojiManager getEmojiManager() {
        return emojiManager;
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

    public id.naturalsmp.naturalcore.chat.tags.TagsManager getTagsManager() {
        return tagsManager;
    }

    public id.naturalsmp.naturalcore.afk.AFKManager getAFKManager() {
        return afkManager;
    }

    public id.naturalsmp.naturalcore.tier.TierManager getTierManager() {
        return tierManager;
    }

    public id.naturalsmp.naturalcore.tier.TierGUI getTierGUI() {
        return tierGUI;
    }

    public id.naturalsmp.naturalcore.season.SeasonResetManager getSeasonResetManager() {
        return seasonResetManager;
    }

    // --- CHAT COLOR ---

    public id.naturalsmp.naturalcore.chat.ChatColorManager getChatColorManager() {
        return chatColorManager;
    }

    // --- HELPER UNTUK MENCEGAH CRASH ---
    private void registerCmd(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("SKIPPING COMMAND: '" + name + "' (Tidak ditemukan di plugin.yml, tapi server aman)");
        }
    }
}