package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand implements CommandExecutor {

    private final TeleportManager teleportManager;
    
    public TPACommand(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.tpa")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(ChatUtils.color("&cUsage: /tpa <player>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cPlayer &f" + args[0] + " &ctidak aktif."));
            player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
            return true;
        }
        
        if (target.equals(player)) {
            player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cError: Tidak bisa TPA ke diri sendiri."));
            player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
            return true;
        }
        
        if (teleportManager.hasCooldown(player)) {
            int remaining = teleportManager.getRemainingCooldown(player);
            player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cCooldown! &7Tunggu &f" + remaining + " detik &7lagi."));
            player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
            return true;
        }
        
        // Create request
        teleportManager.createRequest(player, target);
        teleportManager.setCooldown(player);
        
        // Message to requester
        player.sendActionBar(ChatUtils.color("&x&0&E&B&8&C&0&lTPA &8» &7Mengirim request ke &f" + target.getName() + "..."));
        player.playSound(player.getLocation(), "UI_BUTTON_CLICK", 1f, 1f);
        
        // Message to target (with clickable buttons)
        target.sendMessage("");
        target.sendMessage(ChatUtils.color("&8&m                                                "));
        target.sendMessage(ChatUtils.color("           &x&2&6&D&0&7&C&lT&x&2&2&C&8&8&9&lP&x&1&D&C&1&9&5&lA &x&1&9&B&9&A&2&lR&x&1&5&B&1&A&F&lE&x&1&0&A&A&B&C&lQ&x&0&C&A&2&C&8&lU&x&0&7&9&B&D&5&lE&x&0&3&9&3&E&2&lS&x&0&0&8&C&E&E&lT"));
        target.sendMessage("");
        target.sendMessage(ChatUtils.color("  &f" + player.getName() + " &7ingin teleport ke lokasi kamu."));
        target.sendMessage(ChatUtils.color("  &7Ketik &a/tpyes &7atau &c/tpno &7untuk merespon."));
        target.sendMessage("");
        
        // Clickable buttons
        TextComponent acceptButton = new TextComponent(ChatUtils.color("&a&l[✔ TERIMA]"));
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpyes"));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(ChatUtils.color("&aKlik untuk menerima (atau ketik /tpyes)")).create()));
        
        TextComponent denyButton = new TextComponent(ChatUtils.color("&c&l[✖ TOLAK]"));
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpno"));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(ChatUtils.color("&cKlik untuk menolak (atau ketik /tpno)")).create()));
        
        TextComponent spacer = new TextComponent("          ");
        
        TextComponent message = new TextComponent("     ");
        message.addExtra(acceptButton);
        message.addExtra(spacer);
        message.addExtra(denyButton);
        
        target.spigot().sendMessage(message);
        target.sendMessage("");
        target.sendMessage(ChatUtils.color("&8&m                                                "));
        
        target.playSound(target.getLocation(), "ENTITY_EXPERIENCE_ORB_PICKUP", 1f, 1f);
        
        return true;
    }
}
