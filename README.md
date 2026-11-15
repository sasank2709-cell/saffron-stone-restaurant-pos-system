# Saffron & Stone Restaurant POS

An IntelliJ IDEA Java Swing point-of-sale app using SQLite and JDBC. It has login, menu, customer, table, inventory, POS checkout, order history, kitchen workflow, dashboard, and sales reports.

## Open and run in IntelliJ IDEA

1. Extract the project ZIP (or open this project folder).
2. Open `pom.xml` and import the project as Maven if IntelliJ does not do so automatically. Maven needs internet access the first time to obtain the SQLite JDBC dependency and build plugins.
3. Run `com.saffronstone.pos.Main` with Java 17 or newer.
4. On a new database, sign in with `admin` / `1234`.

The database file is `restaurant_pos.db` in the working directory. The first run creates the tables and starter menu, inventory, tables, and demo admin. Use `SAFFRON_DB_PATH` in the run configuration to select another database file.

## What is implemented

- SQLite/JDBC persistence for orders, item snapshots, menu, customers, inventory, tables, and users.
- Salted PBKDF2 password storage and login validation.
- Atomic checkout that saves the order and all order lines together, records payment choice, and marks dine-in tables occupied.
- Order history with saved line-item retrieval.
- Kitchen status workflow: New, Preparing, Ready, Completed, or Cancelled. Completing an order frees its dine-in table.
- Inventory add and stock updates with low-stock thresholds.
- Customer and menu management with input validation.
- All-time and daily sales, top item, and today's order details.
- `OrderStore` interface for the POS and order-history screens.

## Review 3 demonstration

See [REVIEW3_DEMO.md](REVIEW3_DEMO.md) for a concise, step-by-step workflow. Card and UPI selections are stored as payment methods for the project demonstration; the app does not process real payments.
