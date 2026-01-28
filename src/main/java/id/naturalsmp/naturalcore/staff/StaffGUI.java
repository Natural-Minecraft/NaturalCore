package id.naturalsmp.naturalcore.staff;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class StaffGUI implements Listener {

    private final NaturalCore plugin;

    public StaffGUI(NaturalCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainGUI(Player staff) {
        Inventory inv = GUIUtils.createGUI(new StaffHolder(), 54,
                "&#6CCAFE&lＳＴＡＦＦ &8| &#FFFFFF&lＤＡＳＨＢＯＡＲＤ");

        // Decoration
        ItemStack blueGlass = GUIUtils.createFiller(Material.BLUE_STAINED_GLASS_PANE);

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
            meta.displayName(ChatUtils.toComponent("&#6CCAFE&l" + target.getName()));
            List<Component> lore = Arrays.asList(
                    Component.empty(),
                    ChatUtils.toComponent("&8» &fWorld: &e" + target.getWorld().getName()),
                    ChatUtils.toComponent("&8» &fGamemode: &e" + target.getGameMode().name()),
                    Component.empty(),
                    ChatUtils.toComponent("&b&lLEFT-CLICK &7to Teleport"),
                    ChatUtils.toComponent("&e&lRIGHT-CLICK &7to View Info"),
                    ChatUtils.toComponent("&c&lSHIFT-CLICK &7to Moderation Menu"));
            meta.lore(lore);
            head.setItemMeta(meta);
        }
        return head;
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
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(e.getView().title());

        if (title.contains("ＤＡＳＨＢＯＡＲＤ")) {
            if (item.getType() != Material.PLAYER_HEAD)
                return;
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null)
                return;

            Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
            if (target == null) {
                staff.sendMessage(ChatUtils.toComponent("&cPlayer tidak ditemukan lagi."));
                return;
            }

            if (e.isShiftClick()) {
                openModerationGUI(staff, target);
            } else if (e.isLeftClick()) {
                staff.teleport(target.getLocation());
                staff.sendMessage(ChatUtils.toComponent("&6&lStaff &8» &7Teleport ke &e" + target.getName()));
                staff.playSound(staff.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            } else if (e.isRightClick()) {
                staff.performCommand("whois " + target.getName());
                staff.closeInventory();
            }
        } else if (title.contains("ＭＯＤＥＲＡＴＩＯＮ")) {
            String targetName = title.split("»")[1].trim();
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
                            .toComponent(
                                    "&6&lStaff &8» &7Gunakan &e/kick " + target.getName() + " <alasan> &7di chat."));
                }
                case BLAZE_ROD -> {
                    staff.closeInventory();
                    staff.sendMessage(ChatUtils
                            .toComponent(
                                    "&6&lStaff &8» &7Gunakan &e/ban " + target.getName() + " <alasan> &7di chat."));
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
        Inventory inv = GUIUtils.createGUI(new StaffHolder(), 27,
                "&#CC0000&lＭＯＤＥＲＡＴＩＯＮ &8» &f" + target.getName());

        // Options
        inv.setItem(11, GUIUtils.createItem(Material.ICE, "&#00D4FF&lFREEZE PLAYER",
                Arrays.asList("&7Klik untuk bekukan player.")));
        inv.setItem(12, GUIUtils.createItem(Material.CHEST, "&#FFAA00&lINSPECT INVENTORY",
                Arrays.asList("&7Intip isi tas player.")));
        inv.setItem(13, GUIUtils.createItem(Material.BARRIER, "&#CC0000&lKICK PLAYER",
                Arrays.asList("&7Keluarkan player dari server.")));
        inv.setItem(14, GUIUtils.createItem(Material.BLAZE_ROD, "&#FF4400&lBAN PLAYER",
                Arrays.asList("&7Blokir player dari server.")));
        inv.setItem(15, GUIUtils.createItem(Material.ENDER_PEARL, "&#55FF55&lTELEPORT TO",
                Arrays.asList("&7Teleport ke target.")));

        staff.openInventory(inv);
        staff.playSound(staff.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 1.5f);
    }
}
