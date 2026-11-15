package com.saffronstone.pos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens the local SQLite database and prepares the POS tables. */
public final class DBConnection {
    private static final String DB_PATH = setting("SAFFRON_DB_PATH", "restaurant_pos.db");
    private DBConnection() { }

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) value = System.getProperty(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Path path = Path.of(DB_PATH).toAbsolutePath().normalize();
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + path.toString().replace((char) 92, (char) 47));
            try {
                initialize(connection);
                return connection;
            } catch (SQLException error) {
                try {
                    connection.close();
                } catch (SQLException closeError) {
                    error.addSuppressed(closeError);
                }
                throw error;
            }
        } catch (java.io.IOException error) {
            throw new SQLException("Could not create the SQLite database folder.", error);
        }
    }

    private static void initialize(Connection connection) throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS orders ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "customer_name TEXT NOT NULL DEFAULT 'Walk-in Guest', "
                    + "order_type TEXT NOT NULL CHECK (order_type IN ('Dine-in','Takeaway','Delivery')), "
                    + "table_number INTEGER NULL, "
                    + "payment_method TEXT NOT NULL CHECK (payment_method IN ('Cash','Card','UPI')), "
                    + "subtotal NUMERIC NOT NULL, tax NUMERIC NOT NULL, total NUMERIC NOT NULL, "
                    + "status TEXT NOT NULL DEFAULT 'New', "
                    + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            if (!hasColumn(connection, "orders", "status")) {
                s.executeUpdate("ALTER TABLE orders ADD COLUMN status TEXT NOT NULL DEFAULT 'New'");
            }
            s.executeUpdate("CREATE TABLE IF NOT EXISTS order_items ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE, "
                    + "item_name TEXT NOT NULL, unit_price NUMERIC NOT NULL, "
                    + "quantity INTEGER NOT NULL CHECK (quantity > 0), line_total NUMERIC NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS menu_items ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, "
                    + "category TEXT NOT NULL, price INTEGER NOT NULL CHECK (price > 0), "
                    + "available INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0,1)))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS customers ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, "
                    + "phone TEXT, email TEXT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS inventory_items ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, "
                    + "quantity REAL NOT NULL DEFAULT 0, unit TEXT NOT NULL, "
                    + "low_stock_at REAL NOT NULL DEFAULT 5)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS restaurant_tables ("
                    + "table_number INTEGER PRIMARY KEY, "
                    + "status TEXT NOT NULL DEFAULT 'Available' "
                    + "CHECK (status IN ('Available','Occupied','Reserved')))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, "
                    + "password_salt BLOB NOT NULL, password_hash BLOB NOT NULL, role TEXT NOT NULL DEFAULT 'Staff')");
        }
        seedInitialData(connection);
        AuthRepository.ensureDefaultAdmin(connection);
    }

    private static boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        try (Statement s = connection.createStatement(); ResultSet columns = s.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) if (column.equalsIgnoreCase(columns.getString("name"))) return true;
        }
        return false;
    }

    private static void seedInitialData(Connection connection) throws SQLException {
        if (count(connection, "menu_items") == 0) {
            String sql = "INSERT INTO menu_items(name,category,price,available) VALUES(?,?,?,1)";
            Object[][] items = {
                    {"Paneer Tikka", "Starter", 320}, {"Butter Chicken", "Main Course", 460},
                    {"Dal Makhani", "Main Course", 340}, {"Veg Biryani", "Biryani", 310},
                    {"Hyderabadi Biryani", "Biryani", 390}, {"Margherita Pizza", "Main Course", 360},
                    {"Classic Burger", "Main Course", 420}, {"Mango Lassi", "Beverage", 140},
                    {"Masala Chai", "Beverage", 90}, {"Gulab Jamun", "Dessert", 160}
            };
            try (var p = connection.prepareStatement(sql)) {
                for (Object[] item : items) {
                    p.setString(1, (String) item[0]); p.setString(2, (String) item[1]);
                    p.setInt(3, (Integer) item[2]); p.addBatch();
                }
                p.executeBatch();
            }
        }
        if (count(connection, "inventory_items") == 0) {
            try (Statement s = connection.createStatement()) {
                s.executeUpdate("INSERT INTO inventory_items(name,quantity,unit,low_stock_at) VALUES "
                        + "('Paneer',3,'kg',5),('Rice',12,'kg',5),('Chicken',4,'kg',5),('Tomatoes',9,'kg',5)");
            }
        }
        if (count(connection, "restaurant_tables") == 0) {
            try (Statement s = connection.createStatement()) {
                s.executeUpdate("WITH RECURSIVE numbers(x) AS (SELECT 1 UNION ALL SELECT x+1 FROM numbers WHERE x<12) "
                        + "INSERT INTO restaurant_tables(table_number,status) SELECT x,'Available' FROM numbers");
            }
        }
    }

    private static int count(Connection connection, String table) throws SQLException {
        try (Statement s = connection.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }
}
