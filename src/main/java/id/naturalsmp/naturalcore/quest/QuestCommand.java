package id.naturalsmp.naturalcore.quest;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class QuestCommand implements CommandExecutor {

    private final QuestManager questManager;
    
    public QuestCommand(QuestManager questManager) {
        this.questManager = questManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.quest.admin")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(ChatUtils.color("&c&lQuest System Commands:"));
            player.sendMessage(ChatUtils.color("&7/questnpc penagih - Set NPC yang kamu lihat sebagai Penagih"));
            player.sendMessage(ChatUtils.color("&7/questnpc petani - Set NPC yang kamu lihat sebagai Petani"));
            player.sendMessage(ChatUtils.color("&7/questnpc info - Lihat info NPC quest"));
            player.sendMessage(ChatUtils.color("&7/questnpc reset <player> - Reset quest progress player"));
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "penagih":
                handleSetPenagih(player);
                break;
            case "petani":
                handleSetPetani(player);
                break;
            case "info":
                handleInfo(player);
                break;
            case "reset":
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.color("&cUsage: /questnpc reset <player>"));
                    return true;
                }
                handleReset(player, args[1]);
                break;
            default:
                player.sendMessage(ChatUtils.color("&cAction tidak valid!"));
                break;
        }
        
        return true;
    }
    
    private void handleSetPenagih(Player player) {
        Entity target = getTargetNPC(player);
        if (target == null) {
            player.sendMessage(ChatUtils.color("&cLihat ke NPC Citizens terlebih dahulu!"));
            return;
        }
        
        questManager.setNPCPenagih(target.getUniqueId());
        player.sendMessage(ChatUtils.color("&a&l✔ &aNPC Penagih (HaikalMabrur) telah diset!"));
        player.sendMessage(ChatUtils.color("&7UUID: &f" + target.getUniqueId()));
    }
    
    private void handleSetPetani(Player player) {
        Entity target = getTargetNPC(player);
        if (target == null) {
            player.sendMessage(ChatUtils.color("&cLihat ke NPC Citizens terlebih dahulu!"));
            return;
        }
        
        questManager.setNPCPetani(target.getUniqueId());
        player.sendMessage(ChatUtils.color("&a&l✔ &aNPC Petani (Farmer) telah diset!"));
        player.sendMessage(ChatUtils.color("&7UUID: &f" + target.getUniqueId()));
    }
    
    private void handleInfo(Player player) {
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(ChatUtils.color("&e&lQuest System Info"));
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage("");
        
        if (questManager.getNPCPenagihUUID() != null) {
            player.sendMessage(ChatUtils.color("&7Penagih UUID: &f" + questManager.getNPCPenagihUUID()));
        } else {
            player.sendMessage(ChatUtils.color("&7Penagih: &cBelum diset"));
        }
        
        if (questManager.getNPCPetaniUUID() != null) {
            player.sendMessage(ChatUtils.color("&7Petani UUID: &f" + questManager.getNPCPetaniUUID()));
        } else {
            player.sendMessage(ChatUtils.color("&7Petani: &cBelum diset"));
        }
        
        player.sendMessage("");
        player.sendMessage(ChatUtils.color("&7Quest Stage Kamu: &f" + questManager.getQuestStage(player)));
        player.sendMessage(ChatUtils.color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
    
    private void handleReset(Player player, String targetName) {
        Player target = player.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatUtils.color("&cPlayer tidak ditemukan!"));
            return;
        }
        
        questManager.setQuestStage(target, "none");
        player.sendMessage(ChatUtils.color("&a&l✔ &aQuest &f" + target.getName() + " &atelah direset!"));
        target.sendMessage(ChatUtils.color("&6&l📜 &x&F&F&C&E&4&7&lS&x&F&F&B&C&4&2&lT&x&F&F&A&B&3&D&lO&x&F&F&9&9&3&8&lR&x&F&F&8&8&3&3&lY &8» &eQuest kamu telah direset oleh admin."));
    }
    
    private Entity getTargetNPC(Player player) {
        // Get entity player is looking at (within 5 blocks)
        Entity target = null;
        double minDistance = 5.0;
        
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
                double distance = entity.getLocation().distance(player.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    target = entity;
                }
            }
        }
        
        return target;
    }
}
