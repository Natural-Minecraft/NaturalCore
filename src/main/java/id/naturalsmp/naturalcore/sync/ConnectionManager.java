package id.naturalsmp.naturalcore.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.bukkit.scheduler.BukkitRunnable;

import id.naturalsmp.naturalcore.NaturalCore;

public class ConnectionManager {

    private final Connection connection;
    private volatile boolean isConnected = false;

    public ConnectionManager(NaturalCore instance, String host, int port, String name, String password) {
        connection = new Connection(host, port);

        // Continuously run background tasks to maintain connection
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    try {
                        connection.connect();
                        isConnected = true;
                        
                        // Authorize and identify
                        connection.sendMessage("name " + name);
                        connection.sendMessage("password " + password);
                        instance.getLogger().info("Connected to Velocity Sync server on " + host + ":" + port);
                    } catch (IOException e) {
                        // Silent fail or occasional log to avoid spam
                        // We do not want to spam "Unable to connect" every 2 seconds
                        return;
                    }
                } else {
                    // Send Keep Alive to check and maintain the connection
                    try {
                        connection.sendMessage("keep alive packet");
                    } catch (IOException e) {
                        instance.getLogger().warning("Connection to Velocity Sync server lost. Reconnecting...");
                        isConnected = false; // Trigger reconnect next tick
                    }
                }
            }
        }.runTaskTimerAsynchronously(instance, 40, 40); // Run every 2 seconds
    }

    public void dispatchCommand(String command) {
        sendMessage("run command " + command);
    }
    
    public void shutdown() {
        sendMessage(".");
        isConnected = false;
    }
    
    private void sendMessage(String message) {
        if (!isConnected) return;
        try {
            connection.sendMessage(message);
        } catch (IOException e) {
            isConnected = false;
        }
    }

}
