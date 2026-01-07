package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class TeleportManager {

    private final NaturalCore plugin;

    // Key: Penerima Request, Value: Pengirim Request
    private final HashMap<UUID, UUID> tpaRequests = new HashMap<>();

    // Key: Penerima Request, Value: Tipe (true = tpa, false = tpahere)
    private final HashMap<UUID, Boolean> requestType = new HashMap<>();

    public TeleportManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    // --- LOGIC REQUEST ---

    public void sendTpaRequest(Player sender, Player target, boolean isTpaHere) {
        // Simpan request
        tpaRequests.put(target.getUniqueId(), sender.getUniqueId());
        requestType.put(target.getUniqueId(), isTpaHere);

        // Pesan ke Pengirim
        String prefix = ConfigUtils.getString("prefix.teleport");
        sender.sendMessage(prefix + ConfigUtils.getString("messages.tpa-sent").replace("%target%", target.getName()));

        // Pesan ke Penerima (DENGAN TOMBOL KLIK)
        String msgKey = isTpaHere ? "messages.tpahere-received" : "messages.tpa-received";
        target.sendMessage(prefix + ConfigUtils.getString(msgKey).replace("%player%", sender.getName()));

        sendClickableButtons(target);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);

        // Hapus request otomatis setelah 60 detik
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (tpaRequests.containsKey(target.getUniqueId()) && tpaRequests.get(target.getUniqueId()).equals(sender.getUniqueId())) {
                tpaRequests.remove(target.getUniqueId());
                requestType.remove(target.getUniqueId());
                // Opsional: Beritahu timeout
            }
        }, 20 * 60); // 60 Detik
    }

    public void acceptRequest(Player receiver) {
        String prefix = ConfigUtils.getString("prefix.teleport");

        if (!tpaRequests.containsKey(receiver.getUniqueId())) {
            receiver.sendMessage(prefix + ConfigUtils.getString("messages.tpa-no-request"));
            return;
        }

        UUID senderUUID = tpaRequests.get(receiver.getUniqueId());
        Player sender = Bukkit.getPlayer(senderUUID);
        boolean isTpaHere = requestType.get(receiver.getUniqueId());

        // Hapus request dulu biar gak bisa di-spam
        tpaRequests.remove(receiver.getUniqueId());
        requestType.remove(receiver.getUniqueId());

        if (sender == null || !sender.isOnline()) {
            receiver.sendMessage(prefix + ChatUtils.colorize("&cPemain tersebut sudah offline."));
            return;
        }

        // Logic Teleport
        if (isTpaHere) {
            // TPAHERE: Receiver ditarik ke Sender
            receiver.teleport(sender.getLocation());
            receiver.sendMessage(prefix + ConfigUtils.getString("messages.tpa-accept-target").replace("%player%", sender.getName()));
            sender.sendMessage(prefix + ConfigUtils.getString("messages.tpa-accept-sender"));
        } else {
            // TPA: Sender pergi ke Receiver
            sender.teleport(receiver.getLocation());
            sender.sendMessage(prefix + ConfigUtils.getString("messages.tpa-accept-sender"));
            receiver.sendMessage(prefix + ConfigUtils.getString("messages.tpa-accept-target").replace("%player%", sender.getName()));
        }

        sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        receiver.playSound(receiver.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    public void denyRequest(Player receiver) {
        String prefix = ConfigUtils.getString("prefix.teleport");

        if (!tpaRequests.containsKey(receiver.getUniqueId())) {
            receiver.sendMessage(prefix + ConfigUtils.getString("messages.tpa-no-request"));
            return;
        }

        UUID senderUUID = tpaRequests.get(receiver.getUniqueId());
        Player sender = Bukkit.getPlayer(senderUUID);

        tpaRequests.remove(receiver.getUniqueId());
        requestType.remove(receiver.getUniqueId());

        receiver.sendMessage(prefix + ConfigUtils.getString("messages.tpa-deny-target").replace("%player%", (sender != null ? sender.getName() : "Player")));

        if (sender != null && sender.isOnline()) {
            sender.sendMessage(prefix + ConfigUtils.getString("messages.tpa-deny-sender").replace("%target%", receiver.getName()));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }

    // --- HELPER CLICKABLE MESSAGE ---
    private void sendClickableButtons(Player p) {
        TextComponent accept = new TextComponent(ChatUtils.colorize("   &a&l[ACCEPT]"));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));

        TextComponent space = new TextComponent("  ");

        TextComponent deny = new TextComponent(ChatUtils.colorize("&c&l[DENY]"));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"));

        accept.addExtra(space);
        accept.addExtra(deny);

        p.spigot().sendMessage(accept);
    }
}