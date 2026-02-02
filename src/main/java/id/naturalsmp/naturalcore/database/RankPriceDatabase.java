package id.naturalsmp.naturalcore.database;

import id.naturalsmp.naturalcore.NaturalCore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Handles MySQL queries for rank prices.
 * Fetches prices from the existing 'products' table used by the store website.
 * 
 * Products Table Schema:
 * - id: VARCHAR(255) e.g. "rank-vip", "rank-mvp"
 * - name: VARCHAR(255) e.g. "VIP RANK"
 * - price: DECIMAL(10,2) in Rupiah
 * - type: ENUM('rank', 'currency', 'item')
 * - discount: INT (percentage)
 */
public class RankPriceDatabase {

    private final NaturalCore plugin;
    private Connection connection;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String table;

    // Cached prices (rank_id -> price in RP/NC)
    // Format: "midi" -> 15000.0, "vip" -> 25000.0
    private final Map<String, Double> pricesRP = new HashMap<>();
    private final Map<String, Double> pricesNC = new HashMap<>();
    private final Map<String, String> rankNames = new HashMap<>();
    private final Map<String, Integer> discounts = new HashMap<>();

    public RankPriceDatabase(NaturalCore plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        var config = plugin.getConfig();
        this.host = config.getString("rank_price_database.host", "localhost");
        this.port = config.getInt("rank_price_database.port", 3306);
        this.database = config.getString("rank_price_database.database", "naturalsmp");
        this.username = config.getString("rank_price_database.username", "root");
        this.password = config.getString("rank_price_database.password", "password");
        this.table = config.getString("rank_price_database.table", "products");
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("rank_price_database.enabled", false);
    }

    public boolean connect() {
        if (!isEnabled())
            return false;

        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("[RankPriceDB] Connected to MySQL database.");
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[RankPriceDB] Failed to connect to MySQL:", e);
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("[RankPriceDB] Disconnected from MySQL database.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[RankPriceDB] Error closing connection:", e);
        }
    }

    /**
     * Fetch all rank prices from database and cache them.
     * Filters by type='rank' to only get rank products.
     */
    public void fetchPrices() {
        if (!connect()) {
            // Use fallback prices
            setFallbackPrices();
            return;
        }

        try {
            // Query products table for ranks only
            // id format: "rank-vip", "rank-mvp", etc.
            String query = "SELECT id, name, price, price_virtual, discount FROM " + table + " WHERE type = 'rank'";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            pricesRP.clear();
            pricesNC.clear();
            rankNames.clear();
            discounts.clear();

            while (rs.next()) {
                String productId = rs.getString("id").toLowerCase(); // e.g. "rank-vip"
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                double priceNCValue = rs.getDouble("price_virtual");
                int discount = rs.getInt("discount");

                // Extract rank ID from product ID (e.g., "rank-vip" -> "vip")
                String rankId = productId.replace("rank-", "").replace("-plus", "");

                pricesRP.put(rankId, price);
                pricesNC.put(rankId, priceNCValue);
                rankNames.put(rankId, name);
                discounts.put(rankId, discount);
            }

            rs.close();
            stmt.close();
            plugin.getLogger().info("[RankPriceDB] Loaded " + pricesRP.size() + " rank prices from database.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[RankPriceDB] Failed to fetch prices, using fallback:", e);
            setFallbackPrices();
        }
    }

    private void setFallbackPrices() {
        pricesRP.clear();
        pricesNC.clear();
        rankNames.clear();
        discounts.clear();

        // Fallback prices (Rupiah) - synced with store
        pricesRP.put("midi", 15000.0);
        pricesRP.put("vip", 25000.0);
        pricesRP.put("mvp", 50000.0);
        pricesRP.put("nature", 100000.0);

        // Fallback prices (NC) - estimated
        pricesNC.put("midi", 50.0);
        pricesNC.put("vip", 100.0);
        pricesNC.put("mvp", 250.0);
        pricesNC.put("nature", 500.0);

        rankNames.put("midi", "MIDI RANK");
        rankNames.put("vip", "VIP RANK");
        rankNames.put("mvp", "MVP RANK");
        rankNames.put("nature", "NATURE RANK");

        discounts.put("midi", 0);
        discounts.put("vip", 0);
        discounts.put("mvp", 15);
        discounts.put("nature", 0);
    }

    public double getPriceRP(String rankId) {
        return pricesRP.getOrDefault(rankId.toLowerCase(), 0.0);
    }

    public double getPriceNC(String rankId) {
        return pricesNC.getOrDefault(rankId.toLowerCase(), 0.0);
    }

    /**
     * Get discounted price (RP) if discount is available.
     */
    public double getDiscountedPriceRP(String rankId) {
        double price = getPriceRP(rankId);
        int discount = discounts.getOrDefault(rankId.toLowerCase(), 0);
        if (discount > 0) {
            return price * (100 - discount) / 100.0;
        }
        return price;
    }

    /**
     * Get discounted price (NC) if discount is available.
     */
    public double getDiscountedPriceNC(String rankId) {
        double price = getPriceNC(rankId);
        int discount = discounts.getOrDefault(rankId.toLowerCase(), 0);
        if (discount > 0) {
            return price * (100 - discount) / 100.0;
        }
        return price;
    }

    public int getDiscount(String rankId) {
        return discounts.getOrDefault(rankId.toLowerCase(), 0);
    }

    public String getRankName(String rankId) {
        return rankNames.getOrDefault(rankId.toLowerCase(), rankId.toUpperCase() + " RANK");
    }

    public Map<String, Double> getAllPricesRP() {
        return new HashMap<>(pricesRP);
    }
}
