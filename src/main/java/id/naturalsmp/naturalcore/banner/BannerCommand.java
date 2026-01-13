package id.naturalsmp.naturalcore.banner;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BannerCommand implements CommandExecutor {

    private final BannerManager manager;

    public BannerCommand(BannerManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigUtils.getString("messages.global.only-player"));
            return true;
        }

        if (!player.hasPermission("naturalsmp.admin.banner")) {
            player.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "wand" -> {
                player.getInventory().addItem(BannerListener.getWand());
                player.sendMessage(ChatUtils.colorize("&a&l[Banner] &fYou received the Banner Selection Wand!"));
            }
            case "create" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatUtils
                            .colorize("&cUsage: /banner create <name> <imageName> [leftAction] [rightAction]"));
                    return true;
                }
                String name = args[1];
                String image = args[2];

                Location p1 = BannerListener.getPos1(player.getUniqueId());
                Location p2 = BannerListener.getPos2(player.getUniqueId());

                if (p1 == null || p2 == null) {
                    player.sendMessage(ChatUtils.colorize("&cPlease set Pos 1 and Pos 2 using the wand first!"));
                    return true;
                }

                List<String> left = new ArrayList<>();
                List<String> right = new ArrayList<>();

                if (args.length > 3)
                    left.add(args[3]);
                if (args.length > 4)
                    right.add(args[4]);

                manager.createBanner(name, image, p1, p2, left, right);
                player.sendMessage(
                        ChatUtils.colorize("&a&l[Banner] &fBanner '&e" + name + "&f' created successfully!"));
            }
            case "delete" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.colorize("&cUsage: /banner delete <name>"));
                    return true;
                }
                manager.deleteBanner(args[1]);
                player.sendMessage(ChatUtils.colorize("&a&l[Banner] &fBanner '&e" + args[1] + "&f' deleted."));
            }
            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(ChatUtils.colorize("&6&lNaturalCore Banner System:"));
        p.sendMessage(ChatUtils.colorize("&e/banner wand &7- Get the selection tool"));
        p.sendMessage(ChatUtils.colorize("&e/banner create <name> <image> [leftAction] [rightAction]"));
        p.sendMessage(ChatUtils.colorize("&e/banner delete <name> &7- Remove a banner"));
        p.sendMessage(ChatUtils.colorize("&7Actions format: [URL]Link or [COMMAND]/cmd"));
    }
}
