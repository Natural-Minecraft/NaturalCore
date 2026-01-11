package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PrivateMessageCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public PrivateMessageCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /" + label + " <player> <message>"));
            return true;
        }
        Player p = (Player) sender;
        String cmd = label.toLowerCase();

        // --- COMMAND: /msg <player> <pesan> ---
        if (cmd.equals("msg") || cmd.equals("tell") || cmd.equals("w")) {
            if (args.length < 2) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /" + cmd + " <player> <message>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ConfigUtils.getString("messages.player-not-found"));
                return true;
            }

            // Gabungkan argumen menjadi kalimat pesan
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            sendPrivateMessage(p, target, message);
            return true;
        }

        // --- COMMAND: /reply atau /r ---
        if (cmd.equals("reply") || cmd.equals("r")) {
            if (args.length < 1) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /r <message>"));
                return true;
            }

            Player target = plugin.getMessageManager().getReplyTarget(p);
            if (target == null) {
                p.sendMessage(ConfigUtils.getString("messages.msg-no-reply"));
                return true;
            }

            String message = String.join(" ", args);
            sendPrivateMessage(p, target, message);
            return true;
        }

        return true;
    }

    private void sendPrivateMessage(Player sender, Player receiver, String message) {
        // Update Reply Target
        plugin.getMessageManager().setReplyTarget(sender, receiver);

        // Parse Emojis dalam pesan
        if (EmojiManager.getInstance() != null) {
            message = EmojiManager.getInstance().parseEmojis(sender, message);
        }

        // Format Pesan
        String formatSender = ConfigUtils.getString("chat.msg-format-sender")
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{message}", message);

        String formatReceiver = ConfigUtils.getString("chat.msg-format-receiver")
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{message}", message);

        // Kirim
        sender.sendMessage(ChatUtils.colorize(formatSender));
        receiver.sendMessage(ChatUtils.colorize(formatReceiver));

        // (Optional) SocialSpy logic bisa ditaruh disini nanti
    }
}