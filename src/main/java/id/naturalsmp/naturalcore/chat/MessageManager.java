package id.naturalsmp.naturalcore.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class MessageManager {

    // Menyimpan UUID Pengirim -> UUID Penerima (untuk /reply)
    private final HashMap<UUID, UUID> lastMessageSender = new HashMap<>();

    public void setReplyTarget(Player sender, Player target) {
        lastMessageSender.put(sender.getUniqueId(), target.getUniqueId());
        lastMessageSender.put(target.getUniqueId(), sender.getUniqueId());
    }

    public Player getReplyTarget(Player sender) {
        if (!lastMessageSender.containsKey(sender.getUniqueId())) return null;

        UUID targetUUID = lastMessageSender.get(sender.getUniqueId());
        return Bukkit.getPlayer(targetUUID);
    }
}