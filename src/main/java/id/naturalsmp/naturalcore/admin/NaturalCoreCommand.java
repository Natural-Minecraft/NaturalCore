package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NaturalCoreCommand implements CommandExecutor {

    private final Map<String, CommandExecutor> subCommands = new HashMap<>();

    /**
     * Register a subcommand that can be accessed via /nacore <subcommand>
     * 
     * @param name     The subcommand name (e.g., "setwarp", "kickall")
     * @param executor The CommandExecutor to handle this subcommand
     */
    public void registerSubCommand(String name, CommandExecutor executor) {
        subCommands.put(name.toLowerCase(), executor);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        // Jika cuma ketik /nacore (tanpa argumen) -> Tampilkan Help Menu
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Built-in sub-commands (tidak perlu permission check untuk help/version)
        switch (subCommand) {
            case "reload":
            case "rl":
                if (!sender.hasPermission("naturalsmp.admin")) {
                    sender.sendMessage(ConfigUtils.getString("messages.no-permission"));
                    return true;
                }
                ConfigUtils.reload();
                sender.sendMessage(
                        ConfigUtils.getString("prefix.admin") + ConfigUtils.getString("messages.reload-success"));
                return true;

            case "version":
            case "ver":
                sender.sendMessage(ChatUtils
                        .colorize("&bNaturalCore &7v" + NaturalCore.getInstance().getDescription().getVersion()));
                return true;

            case "help":
                sendHelp(sender);
                return true;
        }

        // Cek apakah ada registered subcommand
        if (subCommands.containsKey(subCommand)) {
            CommandExecutor executor = subCommands.get(subCommand);

            // Buat args baru tanpa subcommand pertama
            String[] newArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

            // Buat fake command dengan nama subcommand untuk onCommand
            FakeCommand fakeCmd = new FakeCommand(subCommand);

            return executor.onCommand(sender, fakeCmd, subCommand, newArgs);
        }

        // Tidak dikenali
        sender.sendMessage(ChatUtils.colorize("&cSubcommand tidak dikenali. Ketik &e/nacore help &cuntuk bantuan."));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        String ver = NaturalCore.getInstance().getDescription().getVersion();
        String header = "&8&m     &6&l NATURAL CORE &7v" + ver + " &8&m     ";

        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize(header));
        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize(" &6&lCore Commands:"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore reload     &7- &fReload config.yml"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore version    &7- &fCek versi plugin"));
        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize(" &6&lAdmin Commands:"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore kickall    &7- &fKick semua player"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore restartalert &7- &fMulai hitungan restart"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore bc         &7- &fBroadcast pesan"));
        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize(" &6&lWarp Commands:"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore warp       &7- &fTeleport ke warp"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore warps      &7- &fBuka menu warp"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore setwarp    &7- &fBuat warp baru"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore delwarp    &7- &fHapus warp"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore setwarpicon &7- &fSet icon warp"));
        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize(" &6&lTrader Commands:"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore trader     &7- &fKelola wandering trader"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore givecurrency &7- &fBeri Quest Paper"));
        sender.sendMessage(ChatUtils.colorize(" &e/nacore tradeeditor &7- &fBuka trade editor"));
        sender.sendMessage(ChatUtils.colorize(""));
        sender.sendMessage(ChatUtils.colorize("&8&m                                     "));
    }

    /**
     * Inner class untuk membuat fake Command object
     * Digunakan untuk meneruskan subcommand ke executor lain
     */
    private static class FakeCommand extends Command {
        protected FakeCommand(String name) {
            super(name);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            return false;
        }
    }
}