package id.naturalsmp.naturalcore.staff;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StaffManager {

    private final NaturalCore plugin;
    private final Set<UUID> staffModePlayers = new HashSet<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, ItemStack[]> inventoryCache = new HashMap<>();
    private final Map<UUID, ItemStack[]> armorCache = new HashMap<>();
    private final Map<UUID, GameMode> gamemodeCache = new HashMap<>();

    public StaffManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void toggleStaffMode(Player player) {
        if (staffModePlayers.contains(player.getUniqueId())) {
            disableStaffMode(player);
        } else {
            enableStaffMode(player);
        }
    }

    public void enableStaffMode(Player player) {
        staffModePlayers.add(player.getUniqueId());

        // Save state
        inventoryCache.put(player.getUniqueId(), player.getInventory().getContents());
        armorCache.put(player.getUniqueId(), player.getInventory().getArmorContents());
        gamemodeCache.put(player.getUniqueId(), player.getGameMode());

        // Prepare staff mode
        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);
        setVanished(player, true);

        giveStaffTools(player);

        player.sendMessage(ChatUtils.colorize("&6&lStaff &8» &7Staff Mode &a&lENABLED&7. Kamu sekarang menghilang."));
    }

    private void giveStaffTools(Player player) {
        player.getInventory().setItem(0,
                createTool(Material.COMPASS, "&#6CCAFE&lTeleport Tool", "&7Left-Click: &fTeleport to point"));
        player.getInventory().setItem(1,
                createTool(Material.CLOCK, "&#FFEE00&lRandom Teleport", "&7Right-Click: &fTP to random player"));
        player.getInventory().setItem(2,
                createTool(Material.BOOK, "&#FFAA00&lInspector", "&7Right-Click Player: &fView Inventory"));
        player.getInventory().setItem(8,
                createTool(Material.BARRIER, "&#CC0000&lStaff Dashboard", "&7Right-Click: &fOpen Moderator Menu"));
    }

    private ItemStack createTool(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize(name));
            List<String> loreList = new ArrayList<>();
            loreList.add(ChatUtils.colorize(lore));
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void disableStaffMode(Player player) {
        staffModePlayers.remove(player.getUniqueId());

        // Restore state
        if (inventoryCache.containsKey(player.getUniqueId())) {
            player.getInventory().setContents(inventoryCache.remove(player.getUniqueId()));
            player.getInventory().setArmorContents(armorCache.remove(player.getUniqueId()));
            player.setGameMode(gamemodeCache.getOrDefault(player.getUniqueId(), GameMode.SURVIVAL));
            gamemodeCache.remove(player.getUniqueId());
        }

        setVanished(player, false);
        player.sendMessage(ChatUtils.colorize("&6&lStaff &8» &7Staff Mode &c&lDISABLED&7. Inventori dikembalikan."));
    }

    public void toggleVanish(Player player) {
        setVanished(player, !vanishedPlayers.contains(player.getUniqueId()));
    }

    public void setVanished(Player player, boolean vanish) {
        if (vanish) {
            vanishedPlayers.add(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("naturalsmp.staff")) {
                    online.hidePlayer(plugin, player);
                }
            }
            player.sendMessage(ChatUtils.colorize("&6&lStaff &8» &7Kamu sekarang &aMenghilang&7."));
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
            player.sendMessage(ChatUtils.colorize("&6&lStaff &8» &7Kamu sekarang &cTerlihat&7."));
        }
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean isInStaffMode(Player player) {
        return staffModePlayers.contains(player.getUniqueId());
    }
}
