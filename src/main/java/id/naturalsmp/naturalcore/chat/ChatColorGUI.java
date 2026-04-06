package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ChatColorGUI implements Listener {

    private final NaturalCore plugin;
    private final String GUI_TITLE = ConfigUtils.getString("messages.gui.chatcolor.title");

    public ChatColorGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        Inventory inv = GUIUtils.createGUI(new ChatColorHolder(), 54, GUI_TITLE);

        // Filler
        ItemStack filler = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, filler);
        }

        // --- ROW 1: DEFAULT ---
        inv.setItem(1, createRankHeader("default"));
        inv.setItem(3, createColorItem(p, Material.WHITE_WOOL, "&fWhite (Default)", "&f", "default"));

        // --- ROW 2: LEVEL 1 ---
        // Permissions: naturalsmp.color.level1
        inv.setItem(10, createRankHeader("level1"));
        inv.setItem(12, createColorItem(p, Material.PINK_WOOL, "&dPink", "&d", "naturalsmp.color.level1"));
        inv.setItem(13, createColorItem(p, Material.MAGENTA_WOOL, "&5Magenta", "&5", "naturalsmp.color.level1"));
        inv.setItem(14, createColorItem(p, Material.PURPLE_WOOL, "&5Purple", "&#8A2BE2", "naturalsmp.color.level1"));

        // --- ROW 3: LEVEL 2 ---
        // Permissions: naturalsmp.color.level2
        inv.setItem(19, createRankHeader("level2"));
        inv.setItem(21, createColorItem(p, Material.LIME_WOOL, "&aLime", "&a", "naturalsmp.color.level2"));
        inv.setItem(22, createColorItem(p, Material.GREEN_WOOL, "&2Green", "&2", "naturalsmp.color.level2"));
        inv.setItem(23, createColorItem(p, Material.YELLOW_WOOL, "&eYellow", "&e", "naturalsmp.color.level2"));

        // --- ROW 4: LEVEL 3 ---
        // Permissions: naturalsmp.color.level3
        inv.setItem(28, createRankHeader("level3"));
        inv.setItem(30, createColorItem(p, Material.LIGHT_BLUE_WOOL, "&bAqua", "&b", "naturalsmp.color.level3"));
        inv.setItem(31, createColorItem(p, Material.BLUE_WOOL, "&9Blue", "&9", "naturalsmp.color.level3"));
        inv.setItem(32, createColorItem(p, Material.CYAN_WOOL, "&3Cyan", "&3", "naturalsmp.color.level3"));

        // --- ROW 5: LEVEL 4 (Font & Styles) ---
        inv.setItem(37, createRankHeader("level4"));
        inv.setItem(39, createFontItem(p, Material.PAPER, "&fDefault Font", "default"));
        inv.setItem(40, createFontItem(p, Material.MAP, "&dSmall Caps", "SmallCaps"));
        inv.setItem(41, createFontItem(p, Material.WRITABLE_BOOK, "&eMath Sans", "MathSans"));

        // --- ROW 6: STYLES ---
        inv.setItem(48, createStyleItem(p, Material.BOOK, "&lBOLD", "bold"));
        inv.setItem(49, createStyleItem(p, Material.FEATHER, "&oItalic", "italic"));
        inv.setItem(50, createItem(Material.BARRIER, "&cReset Style", "&7Klik untuk reset style"));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
    }

    private ItemStack createRankHeader(String rank) {
        String name = ConfigUtils.getString("messages.gui.chatcolor.category." + rank);
        Material mat = Material.valueOf(ConfigUtils.getString("messages.gui.chatcolor.icons." + rank, "PAPER"));
        int cmd = plugin.getConfig().getInt("messages.gui.chatcolor.icons." + rank + "-cmd", 0);
        // Fallback to messages.yml if not in config.yml (messages.yml has them now)
        if (cmd == 0)
            cmd = ConfigUtils.getInt("messages.gui.chatcolor.icons." + rank + "-cmd");

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            if (cmd != 0)
                meta.setCustomModelData(cmd);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createColorItem(Player p, Material mat, String name, String code, String perm) {
        ChatColorManager manager = plugin.getChatColorManager();
        boolean hasPerm = perm.equals("default") || p.hasPermission(perm) || p.hasPermission("naturalsmp.color.level4");

        if (!hasPerm) {
            ItemStack item = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(ChatUtils.toComponent("&7Locked: " + name));
            List<Component> lore = new ArrayList<>();
            lore.add(ChatUtils.toComponent(ConfigUtils.getString("messages.gui.chatcolor.locked-lore")));
            meta.lore(lore);
            item.setItemMeta(meta);
            return item;
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.toComponent(name));
        List<Component> lore = new ArrayList<>();

        if (manager.getPlayerColor(p).equals(code)) {
            lore.add(ChatUtils.toComponent(ConfigUtils.getString("messages.gui.chatcolor.selected-lore")));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add(ChatUtils.toComponent(ConfigUtils.getString("messages.gui.chatcolor.click-select")));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createStyleItem(Player p, Material mat, String name, String type) {
        ChatColorManager manager = plugin.getChatColorManager();
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.toComponent(name));

        boolean isActive = false;
        if (type.equals("bold"))
            isActive = manager.isBold(p);
        if (type.equals("italic"))
            isActive = manager.isItalic(p);

        List<Component> lore = new ArrayList<>();
        if (!p.hasPermission("naturalsmp.color.level4")) {
            lore.add(ChatUtils.toComponent(ConfigUtils.getString("messages.utils.chatcolor-style-locked")));
        } else {
            lore.add(isActive ? ChatUtils.toComponent("&a&lENABLED") : ChatUtils.toComponent("&cDISABLED"));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFontItem(Player p, Material mat, String name, String fontName) {
        ChatColorManager manager = plugin.getChatColorManager();
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.toComponent(name));

        List<Component> lore = new ArrayList<>();
        if (!p.hasPermission("naturalsmp.color.level4")) {
            lore.add(ChatUtils.toComponent(ConfigUtils.getString("messages.utils.chatcolor-style-locked")));
        } else {
            if (manager.getPlayerFont(p).equals(fontName)) {
                lore.add(ChatUtils.toComponent(ConfigUtils.getString("messages.gui.chatcolor.selected-lore")));
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            } else {
                lore.add(ChatUtils.toComponent(ConfigUtils.getString("messages.gui.chatcolor.click-select")));
            }
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<Component> loreList = new ArrayList<>();
            for (String l : lore) {
                loreList.add(ChatUtils.toComponent(l));
            }
            meta.lore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ChatColorHolder))
            return;

        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory())
            return;
        if (!(e.getWhoClicked() instanceof Player))
            return;

        Player p = (Player) e.getWhoClicked();
        ItemStack current = e.getCurrentItem();
        if (current == null || current.getType() == Material.AIR)
            return;

        ChatColorManager manager = plugin.getChatColorManager();
        Material mat = current.getType();

        // COLORS (WOOL)
        if (mat.name().contains("WOOL")) {
            if (current.getType() == Material.GRAY_DYE) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                p.sendMessage(ChatUtils.toComponent(ConfigUtils.getString("prefix.player")
                        + ConfigUtils.getString("messages.utils.chatcolor-locked")));
                return;
            }

            @SuppressWarnings("deprecation")
            String displayName = current.getItemMeta().getDisplayName();
            String name = ChatUtils.stripColor(displayName);
            String colorCode = "&f";

            if (name.contains("Pink"))
                colorCode = "&d";
            else if (name.contains("Magenta"))
                colorCode = "&5";
            else if (name.equals("Purple"))
                colorCode = "&#8A2BE2"; // Midi Purple
            else if (name.contains("Lime"))
                colorCode = "&a";
            else if (name.contains("Green"))
                colorCode = "&2";
            else if (name.contains("Yellow"))
                colorCode = "&e";
            else if (name.contains("Aqua"))
                colorCode = "&b";
            else if (name.contains("Blue"))
                colorCode = "&9";
            else if (name.contains("Cyan"))
                colorCode = "&3";
            else if (name.contains("White"))
                colorCode = "&f";

            manager.setPlayerColor(p, colorCode);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            openGUI(p);
        }

        // STYLES
        if (mat == Material.BOOK || mat == Material.FEATHER) {
            if (!p.hasPermission("naturalsmp.color.level4")) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return;
            }
            if (mat == Material.BOOK)
                manager.setBold(p, !manager.isBold(p));
            if (mat == Material.FEATHER)
                manager.setItalic(p, !manager.isItalic(p));

            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            openGUI(p);
        }

        if (mat == Material.BARRIER) {
            manager.setBold(p, false);
            manager.setItalic(p, false);
            manager.setPlayerFont(p, "default");
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            openGUI(p);
        }

        // FONTS
        if (mat == Material.PAPER || mat == Material.MAP || mat == Material.WRITABLE_BOOK) {
            // Rank headers are also PAPER, so check for CustomModelData or Name
            if (current.getItemMeta().hasCustomModelData())
                return;

            if (!p.hasPermission("naturalsmp.color.level4")) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                return;
            }

            String font = "default";
            if (mat == Material.MAP)
                font = "SmallCaps";
            else if (mat == Material.WRITABLE_BOOK)
                font = "MathSans";

            manager.setPlayerFont(p, font);
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, 1f);
            openGUI(p);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof ChatColorHolder) {
            e.setCancelled(true);
        }
    }

    public static class ChatColorHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
