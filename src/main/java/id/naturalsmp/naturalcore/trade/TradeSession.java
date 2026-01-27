package id.naturalsmp.naturalcore.trade;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TradeSession {

    private final Player player1;
    private final Player player2;
    private final Map<Integer, ItemStack> items1 = new HashMap<>(); // slots 0-3 for P1
    private final Map<Integer, ItemStack> items2 = new HashMap<>(); // slots 0-3 for P2
    private double money1 = 0;
    private double money2 = 0;
    private boolean confirmed1 = false;
    private boolean confirmed2 = false;

    public TradeSession(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Player getOther(Player p) {
        return p.equals(player1) ? player2 : player1;
    }

    public void setConfirmed(Player p, boolean confirmed) {
        if (p.equals(player1))
            confirmed1 = confirmed;
        else
            confirmed2 = confirmed;
    }

    public boolean isConfirmed(Player p) {
        return p.equals(player1) ? confirmed1 : confirmed2;
    }

    public boolean bothConfirmed() {
        return confirmed1 && confirmed2;
    }

    public Map<Integer, ItemStack> getItems(Player p) {
        return p.equals(player1) ? items1 : items2;
    }

    public double getMoney(Player p) {
        return p.equals(player1) ? money1 : money2;
    }

    public void setMoney(Player p, double amount) {
        if (p.equals(player1))
            money1 = amount;
        else
            money2 = amount;
    }
}
