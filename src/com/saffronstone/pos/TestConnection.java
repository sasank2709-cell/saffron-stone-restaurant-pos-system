package com.saffronstone.pos;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("SQLite connected successfully: " + connection.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println("SQLite connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
