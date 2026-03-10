package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SocialCommand implements CommandExecutor {

    private final Map<String, String> links = new HashMap<>();

    public SocialCommand() {
        links.put("links", "https://www.naturalsmp.net/links");
        links.put("vote", "https://vote.naturalsmp.net/");
        links.put("discord", "https://discord.naturalsmp.net/");
        links.put("instagram", "https://instagram.naturalsmp.net/");
        links.put("whatsapp", "https://whatsapp.naturalsmp.net/");
        links.put("appeal", "https://appeal.naturalsmp.net/");
        links.put("wiki", "https://wiki.naturalsmp.net/");
        links.put("tiktok", "https://tiktok.naturalsmp.net/");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.colorize("&cOnly players can use this command."));
            return true;
        }

        String cmdName = command.getName().toLowerCase();
        String url = links.get(cmdName);

        if (url == null) {
            return false;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

        TextComponent header = new TextComponent(
                ChatUtils.colorize("\n&8&m      &f &b&lNATURAL SMP &f &8&m      "));
        TextComponent body = new TextComponent(
                ChatUtils.colorize("\n&7Silahkan klik teks di bawah ini untuk membuka halaman:"));

        TextComponent link = new TextComponent(ChatUtils.colorize("\n&b&l▶ &n" + url + "\n"));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatUtils.colorize("&fKlik untuk menuju:\n&b" + url)).create()));

        TextComponent footer = new TextComponent(
                ChatUtils.colorize("&8&m                                     \n"));

        player.spigot().sendMessage(header, body, link, footer);

        return true;
    }
}
