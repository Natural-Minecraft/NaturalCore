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

        // maintenance whitelist & state table
        String whitelistTable = "CREATE TABLE IF NOT EXISTS nvelo_mt (" +
                "id INT PRIMARY KEY, " +
                "username VARCHAR(64), " +
                "uuid VARCHAR(64), " +
                "value VARCHAR(255))";

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
                PreparedStatement s1_5 = connection.prepareStatement(whitelistTable);
                PreparedStatement s2 = connection.prepareStatement(homesTable);
                PreparedStatement s3 = connection.prepareStatement(spawnTable);
                PreparedStatement s4 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS vanished_players (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "vanished_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")) {
            s1.execute();
            s1_5.execute();
            s2.execute();
            s3.execute();
            s4.execute();
        }

        // Initialize row id = 0 for maintenance state if not exists
        try (PreparedStatement sInit = connection.prepareStatement("INSERT IGNORE INTO nvelo_mt (id, username, uuid, value) VALUES (0, NULL, NULL, 'false')")) {
            sInit.execute();
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
        if (!connect())
            return false;
        String query = "SELECT value FROM nvelo_mt WHERE id = 0";
        try (PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String val = rs.getString("value");
                return val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("global") || val.equalsIgnoreCase("active"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to get maintenance active from nvelo_mt, falling back to core_state", e);
        }
        return Boolean.parseBoolean(getState("maintenance_active", "false"));
    }

    public void setMaintenanceActive(boolean active) {
        setState("maintenance_active", String.valueOf(active));

        if (!connect())
            return;
        String query = "UPDATE nvelo_mt SET value = ? WHERE id = 0";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, active ? "global" : "false");
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to update maintenance active in nvelo_mt", e);
        }
    }

    public String getMaintenanceWhitelist() {
        if (!connect())
            return "[]";

        java.util.List<String> whitelist = new java.util.ArrayList<>();
        String query = "SELECT username FROM nvelo_mt WHERE id >= 1";
        try (PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                if (username != null && !username.trim().isEmpty()) {
                    whitelist.add(username.trim().toLowerCase());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to get maintenance whitelist from nvelo_mt", e);
            return getState("maintenance_whitelist", "[]");
        }

        // Migration logic: if the new table is empty but the old core_state has whitelist data, migrate it!
        if (whitelist.isEmpty()) {
            String oldVal = getState("maintenance_whitelist", "[]");
            if (oldVal != null && !oldVal.equals("[]") && !oldVal.trim().isEmpty()) {
                plugin.getLogger().info("[CoreDB] Migrating maintenance whitelist from core_state to nvelo_mt table...");
                setMaintenanceWhitelist(oldVal);
                // Re-query the table after migration to ensure it works
                try (PreparedStatement stmt = connection.prepareStatement(query);
                        ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String username = rs.getString("username");
                        if (username != null && !username.trim().isEmpty()) {
                            whitelist.add(username.trim().toLowerCase());
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to query maintenance whitelist after migration", e);
                }
            }
        }

        return whitelist.toString();
    }

    public void setMaintenanceWhitelist(String listStr) {
        // Also save to core_state for compatibility/fallback/migration purposes
        setState("maintenance_whitelist", listStr);

        if (!connect())
            return;

        String clean = listStr.replace("[", "").replace("]", "").replace(" ", "");
        java.util.List<String> newUsers = new java.util.ArrayList<>();
        if (!clean.isEmpty()) {
            for (String s : clean.split(",")) {
                newUsers.add(s.trim().toLowerCase());
            }
        }

        try {
            connection.setAutoCommit(false);

            // 1. Delete players who are no longer whitelisted (id >= 1)
            if (newUsers.isEmpty()) {
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM nvelo_mt WHERE id >= 1")) {
                    stmt.executeUpdate();
                }
            } else {
                StringBuilder deleteQuery = new StringBuilder("DELETE FROM nvelo_mt WHERE id >= 1 AND LOWER(username) NOT IN (");
                for (int i = 0; i < newUsers.size(); i++) {
                    deleteQuery.append("?");
                    if (i < newUsers.size() - 1) {
                        deleteQuery.append(",");
                    }
                }
                deleteQuery.append(")");
                try (PreparedStatement stmt = connection.prepareStatement(deleteQuery.toString())) {
                    for (int i = 0; i < newUsers.size(); i++) {
                        stmt.setString(i + 1, newUsers.get(i));
                    }
                    stmt.executeUpdate();
                }
            }

            // 2. Identify which players are already in the database to avoid duplicate inserts
            java.util.Set<String> existingUsers = new java.util.HashSet<>();
            try (PreparedStatement stmt = connection.prepareStatement("SELECT LOWER(username) FROM nvelo_mt WHERE id >= 1");
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String u = rs.getString(1);
                    if (u != null) {
                        existingUsers.add(u.trim().toLowerCase());
                    }
                }
            }

            // 3. Find players that need to be inserted
            java.util.List<String> usersToInsert = new java.util.ArrayList<>();
            for (String user : newUsers) {
                if (!existingUsers.contains(user)) {
                    usersToInsert.add(user);
                }
            }

            // 4. Insert new players with sequential IDs (starting from 1)
            if (!usersToInsert.isEmpty()) {
                int nextId = 1;
                try (PreparedStatement stmt = connection.prepareStatement("SELECT MAX(id) FROM nvelo_mt");
                        ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int max = rs.getInt(1);
                        if (max >= 0) {
                            nextId = max + 1;
                        }
                    }
                }

                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO nvelo_mt (id, username, uuid, value) VALUES (?, ?, NULL, NULL)")) {
                    for (String user : usersToInsert) {
                        stmt.setInt(1, nextId++);
                        stmt.setString(2, user);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to rollback transaction", rollbackEx);
            }
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to set maintenance whitelist in nvelo_mt", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to restore autoCommit", e);
            }
        }
    }

    public void addVanished(java.util.UUID uuid) {
        if (!connect())
            return;
        String query = "INSERT IGNORE INTO vanished_players (uuid) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to add vanished player " + uuid, e);
        }
    }

    public void removeVanished(java.util.UUID uuid) {
        if (!connect())
            return;
        String query = "DELETE FROM vanished_players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to remove vanished player " + uuid, e);
        }
    }

    public java.util.Set<java.util.UUID> getVanishedPlayers() {
        java.util.Set<java.util.UUID> vanished = new java.util.HashSet<>();
        if (!connect())
            return vanished;
        String query = "SELECT uuid FROM vanished_players";
        try (PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                vanished.add(java.util.UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CoreDB] Failed to get vanished players", e);
        }
        return vanished;
    }
}
