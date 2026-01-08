package id.naturalsmp.naturalcore.economy;

import id.naturalsmp.naturalcore.NaturalCore;
import net.milkbowl.vault.chat.Chat; // IMPORT BARU
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultManager {

    private final NaturalCore plugin;
    private Economy economy;
    private Chat chat; // VARIABLE BARU

    public VaultManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    // --- SETUP CHAT (BARU) ---
    public boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return false;
        chat = rsp.getProvider();
        return chat != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    // GETTER CHAT (BARU)
    public Chat getChat() {
        return chat;
    }
}