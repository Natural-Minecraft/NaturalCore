package id.naturalsmp.naturalcore.guide;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuideCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.guide")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            player.sendMessage(ChatUtils.color("&e&lNaturalSMP Guide System"));
            player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            player.sendMessage(ChatUtils.color(""));
            player.sendMessage(ChatUtils.color("&7/guide altar &f- Guide sistem altar"));
            player.sendMessage(ChatUtils.color("&7/guide reforge &f- Guide sistem reforge"));
            player.sendMessage(ChatUtils.color("&7/guide trader &f- Guide travelling trader"));
            player.sendMessage(ChatUtils.color(""));
            player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            return true;
        }
        
        String topic = args[0].toLowerCase();
        
        switch (topic) {
            case "altar":
                sendAltarGuide(player);
                break;
            case "reforge":
                sendReforgeGuide(player);
                break;
            case "trader":
                sendTraderGuide(player);
                break;
            default:
                player.sendMessage(ChatUtils.color("&cTopic tidak valid!"));
                break;
        }
        
        return true;
    }
    
    private void sendAltarGuide(Player player) {
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(ChatUtils.color("&e&lAltar System Guide"));
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&7Altar adalah sistem event dimana player"));
        player.sendMessage(ChatUtils.color("&7harus defend sebuah area dari gelombang monster!"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&e&lCara Bermain:"));
        player.sendMessage(ChatUtils.color("&71. Admin akan mengaktifkan altar"));
        player.sendMessage(ChatUtils.color("&72. Player berkumpul di lokasi altar"));
        player.sendMessage(ChatUtils.color("&73. Defend altar dari monster waves"));
        player.sendMessage(ChatUtils.color("&74. Dapatkan reward jika berhasil!"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
    
    private void sendReforgeGuide(Player player) {
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(ChatUtils.color("&e&lReforge System Guide"));
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&7Reforge memungkinkan kamu untuk meningkatkan"));
        player.sendMessage(ChatUtils.color("&7stats dari item/weapon yang kamu punya!"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&e&lCara Menggunakan:"));
        player.sendMessage(ChatUtils.color("&71. Gunakan command /reforge"));
        player.sendMessage(ChatUtils.color("&72. Place item di slot yang tersedia"));
        player.sendMessage(ChatUtils.color("&73. Klik tombol reforge"));
        player.sendMessage(ChatUtils.color("&74. Item akan mendapat stats baru!"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&c&lPeringatan: &7Stats bisa naik atau turun!"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
    
    private void sendTraderGuide(Player player) {
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(ChatUtils.color("&e&lTravelling Trader Guide"));
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&7Travelling Trader adalah NPC yang muncul"));
        player.sendMessage(ChatUtils.color("&7di waktu-waktu tertentu untuk menjual item rare!"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&e&lJadwal Kedatangan:"));
        player.sendMessage(ChatUtils.color("&7• Siang: 12:00 - 14:00"));
        player.sendMessage(ChatUtils.color("&7• Malam: 20:00 - 22:00"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&e&lCara Beli:"));
        player.sendMessage(ChatUtils.color("&71. Tunggu trader datang"));
        player.sendMessage(ChatUtils.color("&72. Gunakan /trader open"));
        player.sendMessage(ChatUtils.color("&73. Klik item yang ingin dibeli"));
        player.sendMessage(ChatUtils.color(""));
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}
