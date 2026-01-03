package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.*;
import id.naturalsmp.naturalcore.economy.*;
import id.naturalsmp.naturalcore.altar.*;
import id.naturalsmp.naturalcore.reforge.*;
import id.naturalsmp.naturalcore.trader.*;
import id.naturalsmp.naturalcore.guide.*;
import id.naturalsmp.naturalcore.quest.*;
import id.naturalsmp.naturalcore.teleport.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class NaturalCore extends JavaPlugin {

    private static NaturalCore instance;
    
    // Manager instances
    private VaultManager vaultManager;
    private AltarManager altarManager;
    private TraderManager traderManager;
    private QuestManager questManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        getLogger().info("=================================");
        getLogger().info("  NaturalCore Plugin Loading...  ");
        getLogger().info("=================================");
        
        // Check dependencies
        if (!checkDependencies()) {
            getLogger().severe("Missing required dependencies! Plugin will not work properly.");
        }
        
        // Initialize managers
        initializeManagers();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        getLogger().info("=================================");
        getLogger().info("  NaturalCore Successfully Loaded!");
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("=================================");
        getLogger().info("  NaturalCore Plugin Disabled!   ");
        getLogger().info("=================================");
    }
    
    private boolean checkDependencies() {
        boolean allPresent = true;
        
        // Check Vault
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found! Economy features will not work.");
            allPresent = false;
        }
        
        // Check Citizens (optional but recommended)
        if (getServer().getPluginManager().getPlugin("Citizens") == null) {
            getLogger().warning("Citizens not found! Quest system will not work.");
        }
        
        // Check DecentHolograms (optional but recommended)
        if (getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
            getLogger().warning("DecentHolograms not found! Altar holograms will not work.");
        }
        
        return allPresent;
    }
    
    private void initializeManagers() {
        // Initialize Vault for economy
        vaultManager = new VaultManager(this);
        
        // Initialize Altar system
        altarManager = new AltarManager(this);
        
        // Initialize Trader system
        traderManager = new TraderManager(this);
        
        // Initialize Quest system
        questManager = new QuestManager(this);
        
        // Initialize Teleport system
        teleportManager = new TeleportManager(this);
    }
    
    private void registerCommands() {
        // Admin commands
        registerCommand("kickall", new KickAllCommand());
        registerCommand("restartalert", new RestartAlertCommand());
        registerCommand("broadcast", new BroadcastCommand());
        
        // Economy commands
        registerCommand("givebal", new GiveBalCommand(vaultManager));
        
        // Altar commands
        AltarCommand altarCommand = new AltarCommand(altarManager);
        registerCommand("altarwand", altarCommand);
        registerCommand("altarsetpos1", altarCommand);
        registerCommand("altarsetpos2", altarCommand);
        registerCommand("altarsettrigger", altarCommand);
        registerCommand("altarsetwarp", altarCommand);
        registerCommand("altarsetworld", altarCommand);
        registerCommand("altarstart", altarCommand);
        registerCommand("altardelete", altarCommand);
        registerCommand("dungeon", new DungeonCommand(this, altarManager));
        
        // Quest commands
        registerCommand("questnpc", new QuestCommand(questManager));
        
        // Reforge commands
        registerCommand("reforge", new ReforgeCommand());
        
        // Trader commands
        registerCommand("trader", new TraderCommand(traderManager));
        
        // Guide commands
        registerCommand("guide", new GuideCommand());
        
        // TPA commands
        registerCommand("tpa", new TPACommand(teleportManager));
        registerCommand("tpaccept", new TPAAcceptCommand(this, teleportManager));
        registerCommand("tpdeny", new TPADenyCommand(teleportManager));
        registerCommand("back", new BackCommand(this, teleportManager));
    }
    
    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().warning("Command '" + name + "' not found in plugin.yml!");
        }
    }
    
    private void registerListeners() {
        // Altar listeners
        getServer().getPluginManager().registerEvents(new AltarListener(altarManager), this);
        
        // Quest listeners
        getServer().getPluginManager().registerEvents(new QuestListener(questManager), this);
        
        // Reforge listeners
        getServer().getPluginManager().registerEvents(new ReforgeListener(), this);
        
        // Trader listeners
        getServer().getPluginManager().registerEvents(new TraderListener(traderManager), this);
        
        // Guide listeners
        getServer().getPluginManager().registerEvents(new GuideListener(), this);
        
        // Teleport listeners
        getServer().getPluginManager().registerEvents(new TeleportListener(teleportManager), this);
    }
    
    public static NaturalCore getInstance() {
        return instance;
    }
    
    public VaultManager getVaultManager() {
        return vaultManager;
    }
    
    public AltarManager getAltarManager() {
        return altarManager;
    }
    
    public TraderManager getTraderManager() {
        return traderManager;
    }
    
    public QuestManager getQuestManager() {
        return questManager;
    }
    
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
}
