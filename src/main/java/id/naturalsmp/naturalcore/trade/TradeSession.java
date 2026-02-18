package id.naturalsmp.naturalcore.trade;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * TradeSession - Menyimpan seluruh state dari satu sesi trade antara 2 player.
 * Menggunakan SHARED inventory (kedua player buka inventory yang sama).
 */
public class TradeSession {

    private final Player player1;
    private final Player player2;
    private Inventory sharedInventory;

    // Slot assignments
    public static final int[] P1_SLOTS = { 0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30 };
    public static final int[] P2_SLOTS = { 5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35 };

    // Separator column
    public static final int[] SEPARATOR_SLOTS = { 4, 13, 22, 31 };

    // Bottom row (tools) - Row 4
    public static final int SLOT_P1_MONEY = 37;
    public static final int SLOT_P1_LOCK = 38;
    public static final int SLOT_INFO = 40;
    public static final int SLOT_P2_LOCK = 42;
    public static final int SLOT_P2_MONEY = 43;

    // Bottom row (actions) - Row 5
    public static final int SLOT_P1_CONFIRM = 46;
    public static final int SLOT_P1_TRUST = 47;
    public static final int SLOT_COUNTDOWN = 49;
    public static final int SLOT_P2_TRUST = 51;
    public static final int SLOT_P2_CONFIRM = 52;

    // State
    private double money1 = 0;
    private double money2 = 0;
    private boolean confirmed1 = false;
    private boolean confirmed2 = false;
    private boolean locked1 = false;
    private boolean locked2 = false;

    // Countdown
    private boolean countdownActive = false;
    private int countdownTicks = 5;
    private BukkitTask countdownTask;

    // Flag: player sedang input money (jangan cancel trade saat close)
    private boolean p1InputtingMoney = false;
    private boolean p2InputtingMoney = false;

    public TradeSession(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    // --- Player Getters ---
    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Player getOther(Player p) {
        return p.getUniqueId().equals(player1.getUniqueId()) ? player2 : player1;
    }

    public boolean isPlayer1(Player p) {
        return p.getUniqueId().equals(player1.getUniqueId());
    }

    // --- Inventory ---
    public Inventory getSharedInventory() {
        return sharedInventory;
    }

    public void setSharedInventory(Inventory inv) {
        this.sharedInventory = inv;
    }

    // --- Slot Helpers ---
    public int[] getMySlots(Player p) {
        return isPlayer1(p) ? P1_SLOTS : P2_SLOTS;
    }

    public boolean isMySlot(Player p, int slot) {
        int[] mySlots = getMySlots(p);
        for (int s : mySlots) {
            if (s == slot)
                return true;
        }
        return false;
    }

    public boolean isItemSlot(int slot) {
        for (int s : P1_SLOTS)
            if (s == slot)
                return true;
        for (int s : P2_SLOTS)
            if (s == slot)
                return true;
        return false;
    }

    // --- Money ---
    public double getMoney(Player p) {
        return isPlayer1(p) ? money1 : money2;
    }

    public void setMoney(Player p, double amount) {
        if (isPlayer1(p))
            money1 = amount;
        else
            money2 = amount;
    }

    // --- Confirmation ---
    public boolean isConfirmed(Player p) {
        return isPlayer1(p) ? confirmed1 : confirmed2;
    }

    public void setConfirmed(Player p, boolean confirmed) {
        if (isPlayer1(p))
            confirmed1 = confirmed;
        else
            confirmed2 = confirmed;
    }

    public boolean bothConfirmed() {
        return confirmed1 && confirmed2;
    }

    public void resetBothConfirmations() {
        confirmed1 = false;
        confirmed2 = false;
    }

    // --- Lock ---
    public boolean isLocked(Player p) {
        return isPlayer1(p) ? locked1 : locked2;
    }

    public void setLocked(Player p, boolean locked) {
        if (isPlayer1(p))
            locked1 = locked;
        else
            locked2 = locked;
    }

    // --- Countdown ---
    public boolean isCountdownActive() {
        return countdownActive;
    }

    public void setCountdownActive(boolean active) {
        this.countdownActive = active;
    }

    public int getCountdownTicks() {
        return countdownTicks;
    }

    public void setCountdownTicks(int ticks) {
        this.countdownTicks = ticks;
    }

    public BukkitTask getCountdownTask() {
        return countdownTask;
    }

    public void setCountdownTask(BukkitTask task) {
        this.countdownTask = task;
    }

    public void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        countdownActive = false;
        countdownTicks = 5;
    }

    // --- Money Input Flag ---
    public boolean isInputtingMoney(Player p) {
        return isPlayer1(p) ? p1InputtingMoney : p2InputtingMoney;
    }

    public void setInputtingMoney(Player p, boolean inputting) {
        if (isPlayer1(p))
            p1InputtingMoney = inputting;
        else
            p2InputtingMoney = inputting;
    }

    // --- Confirm Slot Helpers ---
    public int getConfirmSlot(Player p) {
        return isPlayer1(p) ? SLOT_P1_CONFIRM : SLOT_P2_CONFIRM;
    }

    public int getMoneySlot(Player p) {
        return isPlayer1(p) ? SLOT_P1_MONEY : SLOT_P2_MONEY;
    }

    public int getLockSlot(Player p) {
        return isPlayer1(p) ? SLOT_P1_LOCK : SLOT_P2_LOCK;
    }

    public int getTrustSlot(Player p) {
        return isPlayer1(p) ? SLOT_P1_TRUST : SLOT_P2_TRUST;
    }
}
