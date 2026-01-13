package id.naturalsmp.naturalcore.banner;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BannerCommand implements CommandExecutor, TabCompleter {

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
                    player.sendMessage(ChatUtils.colorize("&cUsage: /banner create <name> <imageName> [action]"));
                    return true;
                }
                String name = args[1];
                String image = args[2];

                Location p1 = BannerListener.getPos1(player.getUniqueId());
                Location p2 = BannerListener.getPos2(player.getUniqueId());
                org.bukkit.block.BlockFace face = BannerListener.getFace(player.getUniqueId());

                if (p1 == null || p2 == null || face == null) {
                    player.sendMessage(ChatUtils.colorize("&cPlease set Pos 1 and Pos 2 using the wand first!"));
                    return true;
                }

                List<String> left = new ArrayList<>();

                // PERBAIKAN: Gabungkan semua argumen dari index ke-3 sampai habis menjadi satu string
                // Ini memungkinkan command seperti "[COMMAND] /spawn" dianggap satu kesatuan
                if (args.length > 3) {
                    StringBuilder actionBuilder = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        actionBuilder.append(args[i]).append(" ");
                    }
                    left.add(actionBuilder.toString().trim());
                }

                // Untuk "create", kita set Right Action kosong dulu. User bisa pakai /banner edit nanti.
                // Ini lebih aman daripada parsing spasi yang membingungkan.
                List<String> right = new ArrayList<>();

                manager.createBanner(name, image, p1, p2, face, left, right);
                player.sendMessage(ChatUtils.colorize("&a&l[Banner] &fBanner '&e" + name + "&f' created successfully!"));
            }
            case "edit" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.colorize(
                            "&cUsage: /banner edit <name> [--newPict <image>] [--newcmd <leftAction>] [--newcmdRight <rightAction>]"));
                    return true;
                }
                String name = args[1];
                String newPict = null;
                String newLeft = null;
                String newRight = null;

                for (int i = 2; i < args.length; i++) {
                    if (args[i].equalsIgnoreCase("--newPict") && i + 1 < args.length) {
                        newPict = args[i + 1];
                        i++;
                    } else if (args[i].equalsIgnoreCase("--newcmd") && i + 1 < args.length) {
                        newLeft = args[i + 1];
                        i++;
                    } else if (args[i].equalsIgnoreCase("--newcmdRight") && i + 1 < args.length) {
                        newRight = args[i + 1];
                        i++;
                    }
                }

                manager.editBanner(name, newPict,
                        newLeft != null ? List.of(newLeft) : null,
                        newRight != null ? List.of(newRight) : null);
                player.sendMessage(ChatUtils.colorize("&a&l[Banner] &fBanner '&e" + name + "&f' updated!"));
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

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("wand", "create", "edit", "delete"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("delete")) {
                suggestions.addAll(manager.getActiveBanners().keySet());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                suggestions.addAll(getImageFiles());
            } else if (args[0].equalsIgnoreCase("edit")) {
                suggestions.addAll(Arrays.asList("--newPict", "--newcmd", "--newcmdRight"));
            }
        } else {
            String lastArg = args[args.length - 1];
            String prevArg = args[args.length - 2];

            if (prevArg.equalsIgnoreCase("--newPict")) {
                suggestions.addAll(getImageFiles());
            } else if (prevArg.equalsIgnoreCase("--newcmd") || prevArg.equalsIgnoreCase("--newcmdRight")
                    || args[0].equalsIgnoreCase("create")) {
                if (lastArg.isEmpty() || lastArg.startsWith("[")) {
                    suggestions.add("[URL]");
                    suggestions.add("[COMMAND]");
                    suggestions.add("[CONSOLE]");
                }
            } else if (args[0].equalsIgnoreCase("edit")) {
                suggestions.addAll(Arrays.asList("--newPict", "--newcmd", "--newcmdRight"));
            }
        }
        return suggestions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }

    private List<String> getImageFiles() {
        File folder = new File(manager.plugin().getDataFolder(), "banners/images");
        if (!folder.exists())
            return new ArrayList<>();
        File[] files = folder.listFiles();
        if (files == null)
            return new ArrayList<>();
        return Arrays.stream(files).map(File::getName).collect(Collectors.toList());
    }

    private void sendHelp(Player p) {
        p.sendMessage(ChatUtils.colorize("&6&lNaturalCore Banner System:"));
        p.sendMessage(ChatUtils.colorize("&e/banner wand &7- Get selection tool"));
        p.sendMessage(ChatUtils.colorize("&e/banner create <name> <image> [leftAction] [rightAction]"));
        p.sendMessage(ChatUtils.colorize("&e/banner edit <name> <flags>"));
        p.sendMessage(ChatUtils.colorize("&e/banner delete <name>"));
        p.sendMessage(ChatUtils.colorize("&7Actions: [URL], [COMMAND], [CONSOLE]"));
    }
}
