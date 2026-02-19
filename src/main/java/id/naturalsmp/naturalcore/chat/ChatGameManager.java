package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ChatGameManager — Automated chat mini-games with rewards.
 * Games: Math, Unscramble, Trivia, Type Race
 * Rewards: Iron Ingots, Diamonds, or Money (Rp)
 */
public class ChatGameManager {

    private final NaturalCore plugin;
    private BukkitTask gameTask;
    private ActiveGame currentGame;

    // Config
    private static final int MIN_INTERVAL = 180; // seconds
    private static final int MAX_INTERVAL = 300; // seconds
    private static final int GAME_TIMEOUT = 60; // seconds before game expires

    public ChatGameManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        scheduleNextGame();
    }

    public void stop() {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        currentGame = null;
    }

    private void scheduleNextGame() {
        int delay = ThreadLocalRandom.current().nextInt(MIN_INTERVAL, MAX_INTERVAL + 1) * 20; // ticks
        gameTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Bukkit.getOnlinePlayers().size() >= 2) {
                startNewGame();
            }
            scheduleNextGame(); // Schedule the next one regardless
        }, delay);
    }

    private void startNewGame() {
        GameType type = GameType.values()[ThreadLocalRandom.current().nextInt(GameType.values().length)];
        currentGame = generateGame(type);

        // Broadcast to all players
        String line = "&8&m                                          ";
        Bukkit.broadcast(ChatUtils.toComponent(""));
        Bukkit.broadcast(ChatUtils.toComponent(line));
        Bukkit.broadcast(ChatUtils.toComponent("  &#FFAA00&l⚡ CHAT GAME! &7" + currentGame.typeLabel));
        Bukkit.broadcast(ChatUtils.toComponent(""));
        Bukkit.broadcast(ChatUtils.toComponent("  &#55FFFF" + currentGame.question));
        Bukkit.broadcast(ChatUtils.toComponent(""));
        Bukkit.broadcast(ChatUtils.toComponent("  &7Ketik jawabanmu di chat! &8(60 detik)"));
        Bukkit.broadcast(ChatUtils.toComponent(line));
        Bukkit.broadcast(ChatUtils.toComponent(""));

        // Play sound to all
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.5f);
        }

        // Timeout — expire game after 60s
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (currentGame != null && currentGame == this.currentGame) {
                Bukkit.broadcast(ChatUtils.toComponent(""));
                Bukkit.broadcast(
                        ChatUtils.toComponent("  &#FF5555&l✘ &cWaktu habis! Jawaban: &f" + currentGame.answer));
                Bukkit.broadcast(ChatUtils.toComponent(""));
                currentGame = null;
            }
        }, GAME_TIMEOUT * 20L);
    }

    /**
     * Called from ChatListener when a player sends a chat message.
     * Returns true if the message was a correct game answer (so it can be
     * cancelled).
     */
    public boolean tryAnswer(Player player, String message) {
        if (currentGame == null)
            return false;

        String trimmed = message.trim();
        if (trimmed.equalsIgnoreCase(currentGame.answer)) {
            // Winner!
            ActiveGame won = currentGame;
            currentGame = null; // Clear game immediately

            // Give reward on main thread
            Bukkit.getScheduler().runTask(plugin, () -> giveReward(player, won));
            return true;
        }
        return false;
    }

    private void giveReward(Player player, ActiveGame game) {
        Reward reward = generateReward();

        // Apply reward
        switch (reward.type) {
            case ITEM -> {
                ItemStack item = new ItemStack(reward.material, reward.amount);
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                if (!overflow.isEmpty()) {
                    // Drop at player feet if inventory full
                    overflow.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
                }
                // Bonus money for combo rewards
                if (reward.hasBonusMoney() && reward.moneyAmount > 0) {
                    if (plugin.getVaultManager() != null && plugin.getVaultManager().getEconomy() != null) {
                        plugin.getVaultManager().getEconomy().depositPlayer(player, reward.moneyAmount);
                    }
                }
            }
            case MONEY -> {
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getEconomy() != null) {
                    plugin.getVaultManager().getEconomy().depositPlayer(player, reward.moneyAmount);
                }
            }
        }

        // Broadcast winner
        String line = "&8&m                                          ";
        Bukkit.broadcast(ChatUtils.toComponent(""));
        Bukkit.broadcast(ChatUtils.toComponent(line));
        Bukkit.broadcast(ChatUtils.toComponent("  &#55FF55&l✔ &a" + player.getName() + " &7menjawab dengan benar!"));
        Bukkit.broadcast(ChatUtils.toComponent("  &7Jawaban: &#FFFF55" + game.answer));
        Bukkit.broadcast(ChatUtils.toComponent("  &7Hadiah: " + reward.displayText));
        Bukkit.broadcast(ChatUtils.toComponent(line));
        Bukkit.broadcast(ChatUtils.toComponent(""));

        // Sound effects
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
            }
        }
    }

    private Reward generateReward() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 40) {
            // 40% — Iron Ingots (3-8)
            int amount = ThreadLocalRandom.current().nextInt(3, 9);
            return new Reward(RewardType.ITEM, Material.IRON_INGOT, amount, 0,
                    "&#AAAAAA&l" + amount + "x Iron Ingot");
        } else if (roll < 70) {
            // 30% — Money Rp500-Rp1500
            int money = ThreadLocalRandom.current().nextInt(5, 16) * 100; // 500-1500
            return new Reward(RewardType.MONEY, null, 0, money,
                    "&#55FF55&lRp" + ChatUtils.format(money));
        } else if (roll < 90) {
            // 20% — Diamond (1)
            return new Reward(RewardType.ITEM, Material.DIAMOND, 1, 0,
                    "&#55FFFF&l1x Diamond");
        } else {
            // 10% — Diamond (2) + Money Rp1000
            // Give both
            return new Reward(RewardType.ITEM, Material.DIAMOND, 2, 1000,
                    "&#55FFFF&l2x Diamond &7+ &#55FF55&lRp1.000") {
                @Override
                boolean hasBonusMoney() {
                    return true;
                }
            };
        }
    }

    // ==================== GAME GENERATION ====================

    private ActiveGame generateGame(GameType type) {
        return switch (type) {
            case MATH -> generateMathGame();
            case UNSCRAMBLE -> generateUnscrambleGame();
            case TRIVIA -> generateTriviaGame();
            case TYPE_RACE -> generateTypeRaceGame();
        };
    }

    private ActiveGame generateMathGame() {
        int op = ThreadLocalRandom.current().nextInt(4);
        int a, b, answer;
        String symbol;

        switch (op) {
            case 0 -> { // Addition
                a = ThreadLocalRandom.current().nextInt(10, 100);
                b = ThreadLocalRandom.current().nextInt(10, 100);
                answer = a + b;
                symbol = "+";
            }
            case 1 -> { // Subtraction
                a = ThreadLocalRandom.current().nextInt(20, 100);
                b = ThreadLocalRandom.current().nextInt(5, a);
                answer = a - b;
                symbol = "-";
            }
            case 2 -> { // Multiplication
                a = ThreadLocalRandom.current().nextInt(2, 13);
                b = ThreadLocalRandom.current().nextInt(2, 13);
                answer = a * b;
                symbol = "×";
            }
            default -> { // Division (ensure clean division)
                b = ThreadLocalRandom.current().nextInt(2, 13);
                answer = ThreadLocalRandom.current().nextInt(2, 13);
                a = b * answer;
                symbol = "÷";
            }
        }

        return new ActiveGame(GameType.MATH,
                "Matematika",
                "Berapa " + a + " " + symbol + " " + b + " ?",
                String.valueOf(answer));
    }

    private ActiveGame generateUnscrambleGame() {
        String[][] words = {
                { "DIAMOND", "BERLIAN" },
                { "EMERALD", "ZAMRUD" },
                { "CREEPER", "MOB HIJAU" },
                { "REDSTONE", "BATU MERAH" },
                { "OBSIDIAN", "BATU HITAM" },
                { "ENDERMAN", "MOB TINGGI" },
                { "VILLAGER", "PENDUDUK" },
                { "NETHERITE", "ORE TERKUAT" },
                { "SKELETON", "MOB TULANG" },
                { "ENCHANT", "SIHIR" },
                { "POTION", "RAMUAN" },
                { "BEACON", "SUAR" },
                { "TRIDENT", "TRISULA" },
                { "FURNACE", "TUNGKU" },
                { "PICKAXE", "BELIUNG" },
        };

        String[] pair = words[ThreadLocalRandom.current().nextInt(words.length)];
        String word = pair[0];
        String scrambled = scramble(word);

        // Make sure scrambled isn't the same as original
        int attempts = 0;
        while (scrambled.equalsIgnoreCase(word) && attempts < 10) {
            scrambled = scramble(word);
            attempts++;
        }

        return new ActiveGame(GameType.UNSCRAMBLE,
                "Susun Kata",
                "Susun huruf ini: &f&l" + scrambled + " &7(Hint: " + pair[1] + ")",
                word);
    }

    private String scramble(String word) {
        char[] chars = word.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    private ActiveGame generateTriviaGame() {
        String[][] trivia = {
                { "Berapa max level enchant di Minecraft?", "30" },
                { "Blok apa yang paling keras di Minecraft?", "BEDROCK" },
                { "Berapa HP max player tanpa armor?", "20" },
                { "Dimensi apa yang punya Ender Dragon?", "END" },
                { "Berapa jumlah slot hotbar?", "9" },
                { "Mob apa yang drop Gunpowder?", "CREEPER" },
                { "Berapa blok 1 chunk?", "16" },
                { "Tool apa untuk menambang Diamond?", "IRON PICKAXE" },
                { "Berapa banyak obsidian untuk Nether Portal?", "10" },
                { "Item apa untuk menjinakkan kucing?", "RAW COD" },
                { "Biome apa yang ada Mooshroom?", "MUSHROOM" },
                { "Berapa max stack item biasa?", "64" },
                { "Blok apa yang digunakan untuk craft Beacon?", "GLASS" },
                { "Berapa Eye of Ender untuk End Portal?", "12" },
                { "Villager bertukar pakai mata uang apa?", "EMERALD" },
        };

        String[] q = trivia[ThreadLocalRandom.current().nextInt(trivia.length)];
        return new ActiveGame(GameType.TRIVIA,
                "Trivia Minecraft",
                q[0],
                q[1]);
    }

    private ActiveGame generateTypeRaceGame() {
        String[] phrases = {
                "NaturalSMP Server Terbaik",
                "Minecraft Survival Multiplayer",
                "Diamond Pickaxe Unbreaking",
                "Ender Dragon telah dikalahkan",
                "Jangan lupa tidur malam ini",
                "Creeper oh man jangan meledak",
                "Villager Trading Iron Golem",
                "Nether Fortress Blaze Spawner",
                "Enchanting Table Bookshelf Level",
                "Redstone Repeater Comparator Piston",
        };

        String phrase = phrases[ThreadLocalRandom.current().nextInt(phrases.length)];
        return new ActiveGame(GameType.TYPE_RACE,
                "Ketik Cepat",
                "Ketik: &f&l" + phrase,
                phrase);
    }

    // ==================== DATA CLASSES ====================

    public boolean hasActiveGame() {
        return currentGame != null;
    }

    enum GameType {
        MATH, UNSCRAMBLE, TRIVIA, TYPE_RACE
    }

    static class ActiveGame {
        final GameType type;
        final String typeLabel;
        final String question;
        final String answer;

        ActiveGame(GameType type, String typeLabel, String question, String answer) {
            this.type = type;
            this.typeLabel = typeLabel;
            this.question = question;
            this.answer = answer;
        }
    }

    enum RewardType {
        ITEM, MONEY
    }

    static class Reward {
        final RewardType type;
        final Material material;
        final int amount;
        final double moneyAmount;
        final String displayText;

        Reward(RewardType type, Material material, int amount, double moneyAmount, String displayText) {
            this.type = type;
            this.material = material;
            this.amount = amount;
            this.moneyAmount = moneyAmount;
            this.displayText = displayText;
        }

        boolean hasBonusMoney() {
            return false;
        }
    }
}
