package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilderCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public ItemBuilderCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        String cmd = label.toLowerCase();

        // 1. /give <player> <item> [amount] [meta]
        if (cmd.equals("give") || cmd.equals("i")) {
            return handleGive(sender, args);
        }

        // Must be player for editing item in hand
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigUtils.getString("messages.global.only-player"));
            return true;
        }
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            p.sendMessage(ChatUtils.colorize("&cPegang item di tangan terlebih dahulu!"));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return true; // Should not happen usually

        // 2. /itemname <name>
        if (cmd.equals("itemname") || cmd.equals("iname") || cmd.equals("name")) {
            if (args.length == 0) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /itemname <nama baru>"));
                return true;
            }
            String name = String.join(" ", args);
            meta.setDisplayName(ChatUtils.colorize(name));
            item.setItemMeta(meta);
            p.sendMessage(ChatUtils
                    .colorize(ConfigUtils.getString("messages.item-edit.name-changed").replace("%name%", name)));
            return true;
        }

        // 3. /lore <add/set/clear> [args]
        if (cmd.equals("lore") || cmd.equals("ilore")) {
            if (args.length == 0) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /lore <add/set/clear> [text]"));
                return true;
            }

            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            String action = args[0].toLowerCase();

            switch (action) {
                case "add" -> {
                    if (args.length < 2) {
                        p.sendMessage(ChatUtils.colorize("&cUsage: /lore add <text>"));
                        return true;
                    }
                    String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    lore.add(ChatUtils.colorize(text));
                    p.sendMessage(ConfigUtils.getString("messages.item-edit.lore-added"));
                }
                case "set" -> {
                    if (args.length < 3) {
                        p.sendMessage(ChatUtils.colorize("&cUsage: /lore set <line> <text>"));
                        return true;
                    }
                    try {
                        int line = Integer.parseInt(args[1]) - 1;
                        if (line < 0 || line >= lore.size()) {
                            p.sendMessage(ChatUtils.colorize("&cBaris tidak valid (Total: " + lore.size() + ")"));
                            return true;
                        }
                        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        lore.set(line, ChatUtils.colorize(text));
                        p.sendMessage(ConfigUtils.getString("messages.item-edit.lore-set").replace("%line%",
                                String.valueOf(line + 1)));
                    } catch (NumberFormatException e) {
                        p.sendMessage(ChatUtils.colorize("&cNomor baris harus angka!"));
                        return true;
                    }
                }
                case "clear", "removeall" -> {
                    lore.clear();
                    p.sendMessage(ConfigUtils.getString("messages.item-edit.lore-cleared"));
                }
                case "remove", "delete" -> {
                    if (args.length < 2) {
                        p.sendMessage(ChatUtils.colorize("&cUsage: /lore remove <line>"));
                        return true;
                    }
                    try {
                        int line = Integer.parseInt(args[1]) - 1;
                        if (line < 0 || line >= lore.size())
                            return true;
                        lore.remove(line);
                        p.sendMessage(ChatUtils.colorize("&aLore baris " + (line + 1) + " dihapus."));
                    } catch (NumberFormatException e) {
                    }
                }
                default -> p.sendMessage(ChatUtils.colorize("&cAction: add, set, remove, clear"));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            return true;
        }

        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("naturalsmp.give")) {
            sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /give <player> <item> [amount]"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ConfigUtils.getString("messages.global.player-not-found").replace("%player%", args[0]));
            return true;
        }

        Material mat = Material.matchMaterial(args[1]);
        if (mat == null) {
            sender.sendMessage(ChatUtils.colorize("&cItem tidak valid."));
            return true;
        }

        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        ItemStack item = new ItemStack(mat, amount);
        target.getInventory().addItem(item);
        sender.sendMessage(ChatUtils.colorize(ConfigUtils.getString("messages.item-edit.give-success")
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", mat.name())
                .replace("%player%", target.getName())));
        return true;
    }
}
