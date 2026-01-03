package id.naturalsmp.naturalcore.altar;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AltarCommand implements CommandExecutor {

    private final AltarManager altarManager;
    
    public AltarCommand(AltarManager altarManager) {
        this.altarManager = altarManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.altar.admin")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        switch (label.toLowerCase()) {
            case "altarwand":
                handleAltarWand(player);
                break;
            case "altarsetpos1":
                handleSetPos1(player);
                break;
            case "altarsetpos2":
                handleSetPos2(player);
                break;
            case "altarsettrigger":
                handleSetTrigger(player);
                break;
            case "altarsetwarp":
                handleSetWarp(player);
                break;
            case "altarsetworld":
                if (args.length < 1) {
                    player.sendMessage(ChatUtils.color("&cUsage: /altarsetworld <world_name>"));
                    return true;
                }
                handleSetWorld(player, args[0]);
                break;
            case "altarstart":
                handleAltarStart(player, args);
                break;
            case "altardelete":
                handleAltarDelete(player);
                break;
            default:
                player.sendMessage(ChatUtils.color("&cCommand tidak valid!"));
                break;
        }
        
        return true;
    }
    
    private void handleAltarWand(Player player) {
        ItemStack wand = new ItemBuilder(Material.GOLDEN_HOE)
                .setName("&e&lAltar Wand")
                .setLore(
                    "&7Klik Kiri (Pos 1) & Klik Kanan (Pos 2)",
                    "&7untuk membuat &bZona Teleportasi&7."
                )
                .build();
        
        player.getInventory().addItem(wand);
        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &aDapat Wand! Klik Kiri & Kanan untuk set zona."));
    }
    
    private void handleSetPos1(Player player) {
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cLihat blok terlebih dahulu!"));
            return;
        }
        altarManager.setPos1(targetBlock.getLocation());
        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &bPosisi 1 &7(Manual) disimpan!"));
        player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", 1f, 1f);
    }
    
    private void handleSetPos2(Player player) {
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cLihat blok terlebih dahulu!"));
            return;
        }
        altarManager.setPos2(targetBlock.getLocation());
        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &bPosisi 2 &7(Manual) disimpan!"));
        player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", 1f, 1f);
    }
    
    private void handleSetTrigger(Player player) {
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cLihat blok donasinya dulu!"));
            return;
        }
        altarManager.setTriggerLocation(targetBlock.getLocation());
        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &aLokasi Donasi & Hologram diset!"));
        altarManager.updateHologram();
    }
    
    private void handleSetWarp(Player player) {
        altarManager.setWarpLocation(player.getLocation());
        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &aTitik Warp Altar diset!"));
    }
    
    private void handleSetWorld(Player player, String worldName) {
        altarManager.setTargetWorld(worldName);
        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &aTujuan Altar diubah ke world: &e" + worldName));
    }
    
    private void handleAltarStart(Player player, String[] args) {
        if (altarManager.getTargetWorld() == null) {
            player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cWorld Tujuan belum diset! Gunakan /altarsetworld <nama_world>"));
            return;
        }
        
        if (args.length < 1) {
            player.sendMessage(ChatUtils.color("&cUsage: /altarstart <amount1> [amount2] [amount3]"));
            player.sendMessage(ChatUtils.color("&7Pegang item di hotbar 1, 2, 3 sesuai jumlah yang diperlukan"));
            return;
        }
        
        List<ItemStack> items = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();
        
        // Get items from hotbar slots 0, 1, 2
        for (int i = 0; i < Math.min(3, args.length); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cERROR: Hotbar " + (i + 1) + " Kosong!"));
                return;
            }
            
            try {
                int amount = Integer.parseInt(args[i]);
                items.add(item.clone());
                amounts.add(amount);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cJumlah harus berupa angka!"));
                return;
            }
        }
        
        altarManager.startAltar(items, amounts);
        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &aAltar berhasil dimulai!"));
    }
    
    private void handleAltarDelete(Player player) {
        altarManager.deleteAltar();
        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &c&lRESET! &7Data altar telah dihapus bersih."));
    }
}
