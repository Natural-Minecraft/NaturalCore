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

    public ConnectionManager(NaturalCore instance, int port, String name, String password) {
        connection = new Connection("127.0.0.1", port);

        // Continuously try to connect to the server
        new BukkitRunnable() {
            
            private void unableToConnect() {
                instance.getLogger().warning("Unable to connect to the Velocity Sync instance");
            }
            
            @Override
            public void run() {
                if (connection.isConnected()) {
                    
                    isConnected = true;
                    
                    try {
                        if (connection.out == null) {
                            connection.out = new PrintWriter(connection.clientSocket.getOutputStream());
                        }
                        if (connection.in == null) {
                            connection.in = new BufferedReader(new InputStreamReader(connection.clientSocket.getInputStream()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    sendMessage("name " + name);
                    sendMessage("password " + password);
                    instance.getLogger().info("Connected to Velocity Sync server.");
                    cancel();
                } else {
                    unableToConnect();
                }
            }
        }.runTaskTimerAsynchronously(instance, 0, 40);
    }

    public void dispatchCommand(String command) {
        sendMessage("run command " + command);
    }
    
    public void shutdown() {
        sendMessage(".");
        isConnected = false;
    }
    
    private void sendMessage(String message) {
        try {       
            if (isConnected) connection.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
