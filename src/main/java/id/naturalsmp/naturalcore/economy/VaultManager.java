package id.naturalsmp.naturalcore.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultManager {

    private final JavaPlugin plugin;
    private Economy economy;
    
    public VaultManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }
    
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin tidak ditemukan! Economy features akan disabled.");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Economy provider tidak ditemukan!");
            return;
        }
        
        economy = rsp.getProvider();
        plugin.getLogger().info("Vault economy berhasil di-hook!");
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public boolean hasEconomy() {
        return economy != null;
    }
}
