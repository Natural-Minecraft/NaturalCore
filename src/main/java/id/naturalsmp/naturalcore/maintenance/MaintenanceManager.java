package id.naturalsmp.naturalcore.maintenance;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceManager {

    private final NaturalCore plugin;
    private boolean active = false;
    private BukkitRunnable currentTask;
    private final List<String> whitelistedPlayers = new ArrayList<>();

    public MaintenanceManager(NaturalCore plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void scheduleMaintenance(int seconds) {
        if (currentTask != null)
            return;

        currentTask = new BukkitRunnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    setMaintenance(true);
                    this.cancel();
                    currentTask = null;
                    return;
                }

                if (timeLeft <= 10 || timeLeft % 10 == 0) {
                    broadcastCountdown(timeLeft);
                }

                timeLeft--;
            }
        };
        currentTask.runTaskTimer(plugin, 0L, 20L);
    }

    private void broadcastCountdown(int seconds) {
        String msg = ConfigUtils.getString("messages.admin.maintenance.countdown")
                .replace("%time%", String.valueOf(seconds));
        GUIUtils.broadcast(msg);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        }
    }

    public void setMaintenance(boolean active) {
        this.active = active;
        saveData();

        // Notify Velocity
        sendProxyUpdate();

        if (active) {
            String kickReason = ConfigUtils.getString("messages.admin.maintenance.kick-reason");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("naturalsmp.maintenance.bypass")) {
                    p.kickPlayer(ChatUtils.colorize(kickReason));
                }
            }
            GUIUtils.broadcast("&6&lMaintenance &8» &aMode Maintenance telah AKTIF.");
        } else {
            GUIUtils.broadcast("&6&lMaintenance &8» &cMode Maintenance telah NONAKTIF.");
        }
    }

    public boolean isActive() {
        return active;
    }

    public void addWhitelist(String playerName) {
        if (!whitelistedPlayers.contains(playerName.toLowerCase())) {
            whitelistedPlayers.add(playerName.toLowerCase());
            saveData();
            sendProxyUpdate();
        }
    }

    public void removeWhitelist(String playerName) {
        if (whitelistedPlayers.remove(playerName.toLowerCase())) {
            saveData();
            sendProxyUpdate();
        }
    }

    public boolean isWhitelisted(String playerName) {
        return whitelistedPlayers.contains(playerName.toLowerCase());
    }

    private void loadData() {
        if (plugin.getCoreDatabase().isEnabled()) {
            active = plugin.getCoreDatabase().getMaintenanceActive();
            String whitelistJson = plugin.getCoreDatabase().getMaintenanceWhitelist();
            // Simple parsing since we don't have Gson here (or do we? check imports)
            // For now, keep local config as backup/primary if DB disabled
            if (whitelistJson.equals("[]")) {
                whitelistedPlayers.addAll(plugin.getConfig().getStringList("maintenance.whitelist"));
            } else {
                // Basic cleanup for simple string list in TEXT column
                String clean = whitelistJson.replace("[", "").replace("]", "").replace(" ", "");
                if (!clean.isEmpty()) {
                    for (String s : clean.split(",")) {
                        whitelistedPlayers.add(s.toLowerCase());
                    }
                }
            }
        } else {
            active = plugin.getConfig().getBoolean("maintenance.active", false);
            whitelistedPlayers.addAll(plugin.getConfig().getStringList("maintenance.whitelist"));
        }
    }

    private void saveData() {
        plugin.getConfig().set("maintenance.active", active);
        plugin.getConfig().set("maintenance.whitelist", whitelistedPlayers);
        plugin.saveConfig();

        if (plugin.getCoreDatabase().isEnabled()) {
            plugin.getCoreDatabase().setMaintenanceActive(active);
            plugin.getCoreDatabase().setMaintenanceWhitelist(whitelistedPlayers.toString());
        }
    }

    public void sendProxyUpdate() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Maintenance");
            out.writeBoolean(active);

            // Whitelist sync (Include Ops automatically)
            java.util.Set<String> safePlayers = new java.util.HashSet<>(whitelistedPlayers);
            // Add all OPs (Offline supported)
            for (org.bukkit.OfflinePlayer op : Bukkit.getOperators()) {
                if (op.getName() != null) {
                    safePlayers.add(op.getName().toLowerCase());
                }
            }

            out.writeInt(safePlayers.size());
            for (String pName : safePlayers) {
                out.writeUTF(pName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Player p = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (p != null) {
            p.sendPluginMessage(plugin, "natural:main", b.toByteArray());
        }
    }
}
