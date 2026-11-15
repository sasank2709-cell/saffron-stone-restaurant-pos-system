# Saffron POS: SQLite setup

The application creates `restaurant_pos.db` and its tables automatically in the project working directory. Menu items, customers, inventory, table statuses, orders, kitchen statuses, and sales summaries use this database.

## Run in IntelliJ

1. Open this project and let IntelliJ load `pom.xml` as a Maven project. The first load needs an internet connection to download SQLite JDBC.
2. Run `com.saffronstone.pos.Main`.
3. The first run creates starter menu, inventory, and table records, and a demo login: username `admin`, password `1234`.
4. Use **Point of Sale** to add menu items, remove any accidental item with the **−** button, select order type/table/payment, and check out. The order header and every item/price/quantity are saved together in one SQLite transaction.
5. Open **Orders**, select a row, and choose **View Order Items** to retrieve the saved line items. Open **Kitchen** to move an order through New → Preparing → Ready → Completed. Completing a dine-in order releases its table.
6. Use **Inventory** to add ingredients or update stock. Low-stock status is based on the threshold entered for each ingredient. Use **Customers** and **Menu Management** to add customers and menu items or toggle item availability.
7. **Reports** and the dashboard read saved data from SQLite. Reports show all-time sales, today's sales and order count, top item, and the day's order details. Cancelled orders are excluded from sales totals.
8. Close and reopen the app to confirm that orders, customers, menu, inventory, and table status persist.

The login password is stored as a salted PBKDF2 hash. The demo account is `admin` / `1234`. To use a different database file, set `SAFFRON_DB_PATH` in IntelliJ's run configuration to the full path of a `.db` file. Keep the database file safe; it contains customer and order information.

Cash/Card/UPI is saved as the selected payment method. The app does not currently process card or UPI transactions through a payment provider.

The login screen displays the database connection state before you sign in. The first-run demo account is `admin` / `1234`.
