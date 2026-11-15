package com.saffronstone.pos;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/** Database-backed login with salted PBKDF2 password hashes. */
public final class AuthRepository {
    private static final int ITERATIONS = 120_000;
    private static final int HASH_BITS = 256;
    private AuthRepository() { }

    static void ensureDefaultAdmin(Connection connection) throws SQLException {
        try (PreparedStatement check = connection.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
            check.setString(1, "admin");
            try (ResultSet row = check.executeQuery()) {
                if (row.next()) return;
            }
        }
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = hash("1234".toCharArray(), salt);
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT OR IGNORE INTO users(username,password_salt,password_hash,role) VALUES(?,?,?,'Admin')")) {
            insert.setString(1, "admin"); insert.setBytes(2, salt); insert.setBytes(3, hash); insert.executeUpdate();
        }
        Arrays.fill(hash, (byte) 0);
    }

    public static boolean authenticate(String username, char[] password) throws SQLException {
        if (username == null || password == null) return false;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT password_salt,password_hash FROM users WHERE username=?")) {
            query.setString(1, username.trim());
            try (ResultSet row = query.executeQuery()) {
                if (!row.next()) return false;
                byte[] salt = row.getBytes("password_salt");
                byte[] expected = row.getBytes("password_hash");
                byte[] actual = hash(password, salt);
                boolean matches = MessageDigest.isEqual(expected, actual);
                Arrays.fill(actual, (byte) 0);
                return matches;
            }
        }
    }

    private static byte[] hash(char[] password, byte[] salt) throws SQLException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException error) {
            throw new SQLException("Could not verify the login password.", error);
        } finally {
            spec.clearPassword();
        }
    }
}
