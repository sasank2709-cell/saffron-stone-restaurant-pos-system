# Review 3 demonstration flow

## Start

1. Open this folder in IntelliJ IDEA and let IntelliJ import `pom.xml` as a Maven project.
2. Run `com.saffronstone.pos.Main` (Java 17 or newer).
3. Sign in with username `admin` and password `1234` on the first run.

## Demonstrate the app

1. **Menu:** Open Menu and show available items. Add a dish or toggle its availability.
2. **Customer:** Add a customer with a valid name; phone and email are optional and validated when entered.
3. **POS:** Open Point of Sale, add two menu items, remove one by mistake with the **−** button, then choose Dine-in, a table, and a payment method. Check out and note the order number.
4. **Orders:** Open Orders, refresh, select the new order, and choose **View Order Items** to show saved item names, prices, and quantities.
5. **Kitchen:** Open Kitchen and update the order through Preparing, Ready, and Completed. Refresh between screens if needed. A completed dine-in order frees its table.
6. **Inventory:** Add an ingredient with a low-stock threshold, update the quantity, and show the saved stock level and low-stock indicator.
7. **Reports:** Refresh Reports and show all-time sales, today's sales, order count, top item, and today's order details.
8. **Persistence:** Close and reopen the app, then refresh Orders, Kitchen, Inventory, and Reports to show the database-backed records remain.

## Database and login notes

- SQLite file: `restaurant_pos.db` in the project working directory.
- First-run demo login: `admin` / `1234`. The stored password is a salted PBKDF2 hash.
- Set `SAFFRON_DB_PATH` in the IntelliJ run configuration to use a different SQLite file.
- Card and UPI choices are recorded for demonstration; the app does not contact a payment processor.
