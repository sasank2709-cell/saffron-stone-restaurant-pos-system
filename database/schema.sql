-- Complete SQLite schema for Saffron & Stone Restaurant POS.
-- DBConnection also applies these tables and the legacy status migration automatically.
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS orders (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  customer_name TEXT NOT NULL DEFAULT 'Walk-in Guest',
  order_type TEXT NOT NULL CHECK (order_type IN ('Dine-in','Takeaway','Delivery')),
  table_number INTEGER NULL,
  payment_method TEXT NOT NULL CHECK (payment_method IN ('Cash','Card','UPI')),
  subtotal NUMERIC NOT NULL CHECK (subtotal >= 0),
  tax NUMERIC NOT NULL CHECK (tax >= 0),
  total NUMERIC NOT NULL CHECK (total >= 0),
  status TEXT NOT NULL DEFAULT 'New'
    CHECK (status IN ('New','Preparing','Ready','Completed','Cancelled')),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  item_name TEXT NOT NULL,
  unit_price NUMERIC NOT NULL CHECK (unit_price > 0),
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  line_total NUMERIC NOT NULL CHECK (line_total >= 0)
);

CREATE TABLE IF NOT EXISTS menu_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  category TEXT NOT NULL,
  price INTEGER NOT NULL CHECK (price > 0),
  available INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0,1))
);

CREATE TABLE IF NOT EXISTS customers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  phone TEXT,
  email TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  quantity REAL NOT NULL DEFAULT 0 CHECK (quantity >= 0),
  unit TEXT NOT NULL,
  low_stock_at REAL NOT NULL DEFAULT 5 CHECK (low_stock_at >= 0)
);

CREATE TABLE IF NOT EXISTS restaurant_tables (
  table_number INTEGER PRIMARY KEY,
  status TEXT NOT NULL DEFAULT 'Available'
    CHECK (status IN ('Available','Occupied','Reserved'))
);

CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password_salt BLOB NOT NULL,
  password_hash BLOB NOT NULL,
  role TEXT NOT NULL DEFAULT 'Staff'
);
