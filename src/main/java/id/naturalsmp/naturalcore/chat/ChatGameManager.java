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

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

/**
 * ChatGameManager — Automated chat mini-games with rewards.
 * Games loaded from chat-games.yml. Non-repeating randomized system.
 */
public class ChatGameManager {

    private final NaturalCore plugin;
    private BukkitTask gameTask;
    private ActiveGame currentGame;

    // Config
    private int MIN_INTERVAL = 180; // seconds
    private int MAX_INTERVAL = 300; // seconds
    private int GAME_TIMEOUT = 60; // seconds before game expires

    private final List<ActiveGame> allGames = new ArrayList<>();
    private final List<ActiveGame> availableGames = new ArrayList<>();

    public ChatGameManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        loadGames();
        scheduleNextGame();
    }

    public void stop() {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        currentGame = null;
    }

    private void loadGames() {
        allGames.clear();
        availableGames.clear();

        File file = new File(plugin.getDataFolder(), "chat-games.yml");
        if (!file.exists()) {
            plugin.saveResource("chat-games.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        MIN_INTERVAL = config.getInt("settings.min-interval", 180);
        MAX_INTERVAL = config.getInt("settings.max-interval", 300);
        GAME_TIMEOUT = config.getInt("settings.game-timeout", 60);

        if (config.contains("games")) {
            for (String key : config.getConfigurationSection("games").getKeys(false)) {
                String path = "games." + key;
                String typeStr = config.getString(path + ".type", "TRIVIA");
                String title = config.getString(path + ".title", "Mini Game");
                String question = config.getString(path + ".question", "");
                String answer = config.getString(path + ".answer", "");

                GameType type;
                try {
                    type = GameType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    type = GameType.TRIVIA;
                }

                Reward reward = null;
                if (config.contains(path + ".reward")) {
                    String reqPath = path + ".reward";
                    String rewardTypeStr = config.getString(reqPath + ".type", "MONEY");
                    String matStr = config.getString(reqPath + ".material", "DIAMOND");
                    int amt = config.getInt(reqPath + ".amount", 1);
                    int itemAmt = config.getInt(reqPath + ".itemAmount", 1);
                    double moneyAmt = config.getDouble(reqPath + ".moneyAmount", 0);
                    if (config.contains(reqPath + ".amount") && rewardTypeStr.equals("MONEY")) {
                        moneyAmt = config.getDouble(reqPath + ".amount", 0);
                    }
                    String display = config.getString(reqPath + ".displayText", "Reward");

                    RewardType reqType;
                    try {
                        reqType = RewardType.valueOf(rewardTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        reqType = RewardType.MONEY;
                    }

                    Material mat = Material.matchMaterial(matStr);
                    if (mat == null)
                        mat = Material.DIAMOND;

                    if (reqType == RewardType.ITEM) {
                        reward = new Reward(reqType, mat, amt, 0, display);
                    } else if (reqType == RewardType.MONEY) {
                        reward = new Reward(reqType, null, 0, moneyAmt, display);
                    } else if (reqType == RewardType.MONEY_AND_ITEM) {
                        reward = new Reward(reqType, mat, itemAmt, moneyAmt, display);
                    }
                }

                if (!question.isEmpty() && !answer.isEmpty()) {
                    allGames.add(new ActiveGame(type, title, question, answer, reward));
                }
            }
        }

        plugin.getLogger().info("[ChatGames] Loaded " + allGames.size() + " custom games from chat-games.yml.");
        refillAndShuffleGames();
    }

    private void refillAndShuffleGames() {
        availableGames.addAll(allGames);
        Collections.shuffle(availableGames, ThreadLocalRandom.current());
        plugin.getLogger().info("[ChatGames] Shuffled " + availableGames.size() + " games in the pool.");
    }

    private void scheduleNextGame() {
        int delay = ThreadLocalRandom.current().nextInt(MIN_INTERVAL, MAX_INTERVAL + 1) * 20; // ticks
        gameTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Bukkit.getOnlinePlayers().size() >= 2) {
                startNewGame();
            } else {
                // Skip but schedule next
            }
            scheduleNextGame(); // Schedule the next one regardless
        }, delay);
    }

    private void startNewGame() {
        if (allGames.isEmpty()) {
            plugin.getLogger().warning("[ChatGames] No games loaded!");
            return;
        }

        if (availableGames.isEmpty()) {
            refillAndShuffleGames();
        }

        // Pick and remove
        currentGame = availableGames.removeFirst();

        // Broadcast to all players
        String line = "&8&m                                          ";
        Bukkit.broadcast(ChatUtils.toComponent(""));
        Bukkit.broadcast(ChatUtils.toComponent(line));
        Bukkit.broadcast(ChatUtils.toComponent("  &#FFAA00&l⚡ CHAT GAME! &7" + currentGame.typeLabel));
        Bukkit.broadcast(ChatUtils.toComponent(""));
        Bukkit.broadcast(ChatUtils.toComponent("  &#55FFFF" + currentGame.question));
        Bukkit.broadcast(ChatUtils.toComponent(""));
        Bukkit.broadcast(ChatUtils.toComponent("  &7Ketik jawabanmu di chat! &8(" + GAME_TIMEOUT + " detik)"));
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
                // --- LOGGING ---
                id.naturalsmp.naturalcore.utility.NaturalLogger.getInstance()
                        .logChatGame("no one get right answer of \"" + currentGame.question + "\"");
                // ---------------
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
        Reward reward = game.reward;
        if (reward == null) {
            reward = generateFallbackReward();
        }

        // Apply reward
        switch (reward.type) {
            case ITEM -> {
                ItemStack item = new ItemStack(reward.material, reward.amount);
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                if (!overflow.isEmpty()) {
                    // Drop at player feet if inventory full
                    overflow.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
                }
            }
            case MONEY -> {
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getEconomy() != null) {
                    plugin.getVaultManager().getEconomy().depositPlayer(player, reward.moneyAmount);
                }
            }
            case MONEY_AND_ITEM -> {
                ItemStack item = new ItemStack(reward.material, reward.amount);
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                if (!overflow.isEmpty()) {
                    overflow.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
                }
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getEconomy() != null) {
                    plugin.getVaultManager().getEconomy().depositPlayer(player, reward.moneyAmount);
                }
            }
        }

        // --- LOGGING ---
        id.naturalsmp.naturalcore.utility.NaturalLogger.getInstance().logChatGame(
                player.getName() + " get right answer of \"" + game.question + "\", answer: \"" + game.answer + "\"");
        // ---------------

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

    private Reward generateFallbackReward() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 40) {
            int amount = ThreadLocalRandom.current().nextInt(3, 9);
            return new Reward(RewardType.ITEM, Material.IRON_INGOT, amount, 0,
                    "&#AAAAAA&l" + amount + "x Iron Ingot");
        } else if (roll < 70) {
            int money = ThreadLocalRandom.current().nextInt(5, 16) * 100; // 500-1500
            return new Reward(RewardType.MONEY, null, 0, money,
                    "&#55FF55&lRp" + ChatUtils.format(money));
        } else if (roll < 90) {
            return new Reward(RewardType.ITEM, Material.DIAMOND, 1, 0,
                    "&#55FFFF&l1x Diamond");
        } else {
            return new Reward(RewardType.MONEY_AND_ITEM, Material.DIAMOND, 2, 1000,
                    "&#55FFFF&l2x Diamond &7+ &#55FF55&lRp1.000");
        }
    }

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
        final Reward reward;

        ActiveGame(GameType type, String typeLabel, String question, String answer, Reward reward) {
            this.type = type;
            this.typeLabel = typeLabel;
            this.question = question;
            this.answer = answer;
            this.reward = reward;
        }
    }

    enum RewardType {
        ITEM, MONEY, MONEY_AND_ITEM
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
    }
}
