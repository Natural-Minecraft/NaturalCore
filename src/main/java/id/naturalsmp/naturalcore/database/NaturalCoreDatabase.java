package id.naturalsmp.naturalcore.database;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Handles centralized MySQL state for NaturalCore and sync with
 * NaturalVelocity.
 */
public class NaturalCoreDatabase {

    private final NaturalCore plugin;
    private Connection connection;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public NaturalCoreDatabase(NaturalCore plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        var config = plugin.getConfig();
        // Use a new section for core database to avoid confusion with RankPriceDatabase
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.database", "naturalsmp_core");
        this.username = config.getString("database.username", "root");
        this.password = config.getString("database.password", "");
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("database.enabled", false);
    }

    public boolean connect() {
        if (!isEnabled())
            return false;

        try {
            if (connection != null && !connection.isClosed() && connection.isValid(1)) {
                return true;
            }

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);
            setupTables();
            plugin.getLogger().info("[CoreDB] Connected to MySQL database.");
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[CoreDB] Failed to connect to MySQL:", e);
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Error closing connection:", e);
        }
    }

    private void setupTables() throws SQLException {
        // key-value table for general state (maintenance, etc)
        String coreState = "CREATE TABLE IF NOT EXISTS core_state (" +
                "`key` VARCHAR(64) PRIMARY KEY, " +
                "`value` TEXT NOT NULL, " +
                "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";

        // homes table
        String homesTable = "CREATE TABLE IF NOT EXISTS player_homes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "uuid VARCHAR(36) NOT NULL, " +
                "home_name VARCHAR(64) NOT NULL, " +
                "world VARCHAR(64) NOT NULL, " +
                "x DOUBLE NOT NULL, " +
                "y DOUBLE NOT NULL, " +
                "z DOUBLE NOT NULL, " +
                "yaw FLOAT NOT NULL, " +
                "pitch FLOAT NOT NULL, " +
                "UNIQUE KEY `player_home` (uuid, home_name))";

        // spawn table
        String spawnTable = "CREATE TABLE IF NOT EXISTS server_spawn (" +
                "id VARCHAR(32) PRIMARY KEY, " +
                "world VARCHAR(64) NOT NULL, " +
                "x DOUBLE NOT NULL, " +
                "y DOUBLE NOT NULL, " +
                "z DOUBLE NOT NULL, " +
                "yaw FLOAT NOT NULL, " +
                "pitch FLOAT NOT NULL)";

        try (PreparedStatement s1 = connection.prepareStatement(coreState);
                PreparedStatement s2 = connection.prepareStatement(homesTable);
                PreparedStatement s3 = connection.prepareStatement(spawnTable)) {
            s1.execute();
            s2.execute();
            s3.execute();
        }
    }

    public String getState(String key, String defaultValue) {
        if (!connect())
            return defaultValue;

        String query = "SELECT `value` FROM core_state WHERE `key` = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to get state for " + key, e);
        }
        return defaultValue;
    }

    public void setState(String key, String value) {
        if (!connect())
            return;

        String query = "INSERT INTO core_state (`key`, `value`) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE `value` = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to set state for " + key, e);
        }
    }

    public boolean getMaintenanceActive() {
        return Boolean.parseBoolean(getState("maintenance_active", "false"));
    }

    public void setMaintenanceActive(boolean active) {
        setState("maintenance_active", String.valueOf(active));
    }

    public String getMaintenanceWhitelist() {
        return getState("maintenance_whitelist", "[]");
    }

    public void setMaintenanceWhitelist(String jsonArray) {
        setState("maintenance_whitelist", jsonArray);
    }
}
