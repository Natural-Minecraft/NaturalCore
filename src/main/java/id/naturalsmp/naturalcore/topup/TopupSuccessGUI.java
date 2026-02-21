package id.naturalsmp.naturalcore.topup;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TopupSuccessGUI implements Listener {

    private final NaturalCore plugin;

    public TopupSuccessGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, double amount, String transactionId) {
        // Aesthetic Title (Standard NaturalSMP Style)
        Inventory inv = GUIUtils.createGUI(new TopupHolder(), 27,
                "&#FFFF00❂ &#FFFF00ᴛᴏᴘᴜᴘ ʙᴇʀʜᴀꜱɪʟ &#FFFF00❂");

        // Standard Fillers
        ItemStack filler = GUIUtils.createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack accent = GUIUtils.createFiller(Material.YELLOW_STAINED_GLASS_PANE);

        // Fill border with a pattern
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, accent);
            } else {
                inv.setItem(i, filler);
            }
        }

        // Center Piece - The Reward
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String date = dtf.format(LocalDateTime.now());

        inv.setItem(13, createItem(Material.SUNFLOWER, "&#FFFF00&lTOPUP BERHASIL!",
                "&8&m------------------",
                "&7Terima kasih, &#55FF55&l" + player.getName() + "&7!",
                "&7Dukunganmu sangat berarti bagi kami.",
                "",
                "&#FFAA00&lDETAIL TRANSAKSI:",
                "&7• Item: &fNaturalCoins",
                "&7• Jumlah: &6" + amount + " NC",
                "&7• ID: &e#" + transactionId,
                "&7• Waktu: &7" + date,
                "",
                "&#55FF55&l✔ SUDAH MASUK KE DOMPET",
                "&8&m------------------"));

        setupSideInfo(inv);
        player.openInventory(inv);
        playPremiumEffects(player);
    }

    public void openRankGUI(Player player, String rank, String transactionId) {
        Inventory inv = GUIUtils.createGUI(new TopupHolder(), 27,
                "&#00AAFF❂ &#00AAFFʀᴀɴᴋ ᴜᴘɢʀᴀᴅᴇ &#00AAFF❂");

        ItemStack filler = GUIUtils.createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack accent = GUIUtils.createFiller(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, accent);
            } else {
                inv.setItem(i, filler);
            }
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String date = dtf.format(LocalDateTime.now());

        inv.setItem(13, createItem(Material.NETHER_STAR, "&#00AAFF&lRANK UPGRADE BERHASIL!",
                "&8&m------------------",
                "&7Terima kasih, &#55FF55&l" + player.getName() + "&7!",
                "&7Kini kamu berstatus &b" + rank.toUpperCase() + "&7.",
                "",
                "&#FFAA00&lDETAIL TRANSAKSI:",
                "&7• Item: &fRank &e" + rank.toUpperCase(),
                "&7• Durasi: &630 Hari (30d)",
                "&7• ID: &e#" + transactionId,
                "&7• Waktu: &7" + date,
                "",
                "&#55FF55&l✔ FITUR PREMIUM TERBUKA",
                "&8&m------------------"));

        setupSideInfo(inv);
        player.openInventory(inv);
        playPremiumEffects(player);
    }

    private void setupSideInfo(Inventory inv) {
        // Side Info - Tips
        inv.setItem(11, createItem(Material.BOOK, "&#00AAFF&lPREMIUM SUPPORT",
                "&7Nikmati fitur eksklusif",
                "&7sesuai dengan paketmu.",
                "",
                "&7Setiap dukungan membantu",
                "&7server tetap online."));

        inv.setItem(15, createItem(Material.PAPER, "&#FF55FF&lVIRTUAL RECEIPT",
                "&7Struk digital ini disimpan",
                "&7sebagai bukti transaksi sah.",
                "",
                "&7Screenshot ini sebagai",
                "&7kenang-kenangan!"));
    }

    private void playPremiumEffects(Player player) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.3f, 1.0f);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<Component> l = new ArrayList<>();
            for (String s : lore)
                l.add(ChatUtils.toComponent(s));
            meta.lore(l);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof TopupHolder) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory())
                return;
        }
    }

    public static class TopupHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
