package com.saffronstone.pos;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Persistence contract used by the POS and order-history screens. */
public interface OrderStore {
    long saveOrder(String customer, String orderType, Integer tableNumber, String paymentMethod,
                   Map<String, Integer> items, Map<String, Integer> prices) throws SQLException;

    List<Object[]> getRecentOrders() throws SQLException;

    List<Object[]> getOrderItems(long orderId) throws SQLException;
}
