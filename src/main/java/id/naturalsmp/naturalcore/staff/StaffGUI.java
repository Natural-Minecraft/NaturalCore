package id.naturalsmp.naturalcore.staff;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.ArrayList;
import java.util.List;

public class StaffGUI implements Listener {

    private final NaturalCore plugin;

    public StaffGUI(NaturalCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainGUI(Player staff) {
        Inventory inv = Bukkit.createInventory(new StaffHolder(), 54,
                ChatUtils.colorize("&#6CCAFE&lＳＴＡＦＦ &8| &#FFFFFF&lＤＡＳＨＢＯＡＲＤ"));

        // Decoration
        ItemStack blueGlass = createItem(Material.BLUE_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blueGlass);
            inv.setItem(45 + i, blueGlass);
        }

        int slot = 9;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (slot >= 45)
                break;
            inv.setItem(slot++, createPlayerHead(p));
        }

        staff.openInventory(inv);
        staff.playSound(staff.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
    }

    private ItemStack createPlayerHead(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(target);
            meta.setDisplayName(ChatUtils.colorize("&#6CCAFE&l" + target.getName()));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatUtils.colorize("&8» &fWorld: &e" + target.getWorld().getName()));
            lore.add(ChatUtils.colorize("&8» &fGamemode: &e" + target.getGameMode().name()));
            lore.add("");
            lore.add(ChatUtils.colorize("&b&lLEFT-CLICK &7to Teleport"));
            lore.add(ChatUtils.colorize("&e&lRIGHT-CLICK &7to View Info"));
            lore.add(ChatUtils.colorize("&c&lSHIFT-CLICK &7to Moderation Menu"));
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize(name));
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String l : lore) {
                    loreList.add(ChatUtils.colorize(l));
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof StaffHolder))
            return;
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR)
            return;

        Player staff = (Player) e.getWhoClicked();
        String title = ChatUtils.colorize(e.getView().getTitle());

        if (title.contains("ＤＡＳＨＢＯＡＲＤ")) {
            if (item.getType() != Material.PLAYER_HEAD)
                return;
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null)
                return;

            Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
            if (target == null) {
                staff.sendMessage(ChatUtils.colorize("&cPlayer tidak ditemukan lagi."));
                return;
            }

            if (e.isShiftClick()) {
                openModerationGUI(staff, target);
            } else if (e.isLeftClick()) {
                staff.teleport(target.getLocation());
                staff.sendMessage(ChatUtils.colorize("&6&lStaff &8» &7Teleport ke &e" + target.getName()));
                staff.playSound(staff.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            } else if (e.isRightClick()) {
                staff.performCommand("whois " + target.getName());
                staff.closeInventory();
            }
        } else if (title.contains("ＭＯＤＥＲＡＴＩＯＮ")) {
            String targetName = title.split("»")[1].trim().replace("§f", "");
            Player target = Bukkit.getPlayer(targetName);
            if (target == null)
                return;

            switch (item.getType()) {
                case ICE -> {
                    staff.performCommand("freeze " + target.getName());
                }
                case CHEST -> {
                    staff.performCommand("invsee " + target.getName());
                }
                case BARRIER -> {
                    staff.closeInventory();
                    staff.sendMessage(ChatUtils
                            .colorize("&6&lStaff &8» &7Gunakan &e/kick " + target.getName() + " <alasan> &7di chat."));
                }
                case BLAZE_ROD -> {
                    staff.closeInventory();
                    staff.sendMessage(ChatUtils
                            .colorize("&6&lStaff &8» &7Gunakan &e/ban " + target.getName() + " <alasan> &7di chat."));
                }
                case ENDER_PEARL -> {
                    staff.teleport(target.getLocation());
                    staff.playSound(staff.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                }
                default -> {
                }
            }
        }
    }

    public void openModerationGUI(Player staff, Player target) {
        Inventory inv = Bukkit.createInventory(new StaffHolder(), 27,
                ChatUtils.colorize("&#CC0000&lＭＯＤＥＲＡＴＩＯＮ &8» &f" + target.getName()));

        // Options
        inv.setItem(11, createItem(Material.ICE, "&#00D4FF&lFREEZE PLAYER", "&7Klik untuk bekukan player."));
        inv.setItem(12, createItem(Material.CHEST, "&#FFAA00&lINSPECT INVENTORY", "&7Intip isi tas player."));
        inv.setItem(13, createItem(Material.BARRIER, "&#CC0000&lKICK PLAYER", "&7Keluarkan player dari server."));
        inv.setItem(14, createItem(Material.BLAZE_ROD, "&#FF4400&lBAN PLAYER", "&7Blokir player dari server."));
        inv.setItem(15, createItem(Material.ENDER_PEARL, "&#55FF55&lTELEPORT TO", "&7Teleport ke target."));

        staff.openInventory(inv);
        staff.playSound(staff.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 1.5f);
    }
}
