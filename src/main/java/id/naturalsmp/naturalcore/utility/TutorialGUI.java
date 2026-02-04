package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TutorialGUI implements Listener {

    private final NaturalCore plugin;

    public TutorialGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, String filter) {
        String title = filter == null ? "&#00AAFF&l📖 BASIC COMMANDS" : "&#00AAFF&l📖 SEARCH: " + filter;
        Inventory inv = GUIUtils.createGUI(new TutorialHolder(), 54, title);

        // Border & Info
        GUIUtils.fillBorder(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Define Items
        List<CommandItem> items = new ArrayList<>();
        items.add(new CommandItem("gg", Material.GOLD_INGOT, "&#FFFF55&l/gg", "&7Bilang 'Good Game' ke semua orang!"));
        items.add(new CommandItem("noob", Material.WOODEN_SWORD, "&#FF5555&l/noob", "&7Teriak 'NOOB' ke semua orang!"));
        items.add(new CommandItem("ranks", Material.DIAMOND, "&#55FFFF&l/ranks", "&7Lihat daftar rank server."));
        items.add(new CommandItem("tiers", Material.EMERALD, "&#55FF55&l/tiers", "&7Cek progress tier kamu."));
        items.add(new CommandItem("warp", Material.ENDER_PEARL, "&#9955FF&l/warp", "&7Menu teleportasi cepat."));
        items.add(new CommandItem("start", Material.NETHER_STAR, "&#FFAA00&l/start", "&7Buka menu awal server."));
        items.add(new CommandItem("tutorial", Material.BOOK, "&#AAAAAA&l/tutorial", "&7Teleport ke area tutorial."));
        items.add(new CommandItem("firework", Material.FIREWORK_ROCKET, "&#FF55FF&l/firework",
                "&7Luncurkan kembang api!"));
        items.add(new CommandItem("profile", Material.PLAYER_HEAD, "&#55FF55&l/profile",
                "&7Lihat statistik karaktermu."));
        items.add(new CommandItem("shop", Material.GOLD_BLOCK, "&#FFFF55&l/shop", "&7Buka toko server."));

        // Layout Logic
        int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };
        int currentSlot = 0;

        for (CommandItem item : items) {
            // Filter Logic
            if (filter != null && !item.command.contains(filter.toLowerCase())
                    && !item.name.toLowerCase().contains(filter.toLowerCase())) {
                continue;
            }

            if (currentSlot >= slots.length)
                break;

            ItemStack is = new ItemStack(item.mat);
            ItemMeta meta = is.getItemMeta();
            meta.displayName(ChatUtils.toComponent(item.name));
            List<Component> lore = new ArrayList<>();
            lore.add(ChatUtils.toComponent(item.desc));
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("&#FFAA00&l➥ KLIK UNTUK EKSEKUSI"));
            meta.lore(lore);
            is.setItemMeta(meta);

            inv.setItem(slots[currentSlot], is);
            currentSlot++;
        }

        // Search Button (Slot 49)
        ItemStack search = new ItemStack(Material.OAK_SIGN);
        ItemMeta sMeta = search.getItemMeta();
        sMeta.displayName(ChatUtils.toComponent("&#55FF55&l🔎 CARI COMMAND"));
        List<Component> sLore = new ArrayList<>();
        sLore.add(ChatUtils.toComponent("&7Klik untuk mencari command"));
        sLore.add(ChatUtils.toComponent("&7dengan mengetik di chat."));
        sMeta.lore(sLore);
        search.setItemMeta(sMeta);
        inv.setItem(49, search);

        // Back Button (Slot 45)
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.displayName(ChatUtils.toComponent("&c&l⬅ KEMBALI"));
        back.setItemMeta(bMeta);
        inv.setItem(45, back);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TutorialHolder))
            return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE)
            return;

        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

        if (clicked.getType() == Material.ARROW) {
            p.performCommand("menu"); // Back to main menu
            return;
        }

        if (clicked.getType() == Material.OAK_SIGN) {
            p.closeInventory();
            p.sendMessage(ChatUtils.toComponent("&#55FF55&l🔎 &aSilahkan ketik kata kunci pencarian di chat..."));
            p.sendMessage(ChatUtils.toComponent("&7(Ketik 'cancel' untuk membatalkan)"));

            // Set Search Mode
            id.naturalsmp.naturalcore.chat.ChatListener.setSearchMode(p.getUniqueId());
            return;
        }

        // Execute Command from Name logic (Strip colors first)
        String name = ChatUtils.stripColor(ChatUtils.serialize(clicked.getItemMeta().displayName()));
        if (name.startsWith("/")) {
            p.performCommand(name.substring(1)); // Remove '/'
            p.closeInventory();
        }
    }

    private static class CommandItem {
        String command;
        Material mat;
        String name;
        String desc;

        CommandItem(String cmd, Material m, String n, String d) {
            this.command = cmd;
            this.mat = m;
            this.name = n;
            this.desc = d;
        }
    }

    public static class TutorialHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
