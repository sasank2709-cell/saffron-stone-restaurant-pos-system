package com.saffronstone.pos;

/** Shared validation for values entered in the POS screens and repository API. */
public final class InputValidation {
    private InputValidation() { }

    public static String requiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    public static int positiveInt(int value, String fieldName) {
        if (value <= 0) throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        return value;
    }

    public static double nonNegativeFinite(double value, String fieldName) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(fieldName + " must be a finite, non-negative number.");
        }
        return value;
    }

    public static String optionalPhone(String value) {
        if (value == null || value.isBlank()) return null;
        String phone = value.trim();
        String digits = phone.replaceAll("\\D", "");
        if (!phone.matches("[0-9+(). -]+") || digits.length() < 7 || digits.length() > 15) {
            throw new IllegalArgumentException("Enter a valid phone number with 7 to 15 digits, or leave it blank.");
        }
        return phone;
    }

    public static String optionalEmail(String value) {
        if (value == null || value.isBlank()) return null;
        String email = value.trim();
        if (email.length() > 254 || !email.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$")) {
            throw new IllegalArgumentException("Enter a valid email address, or leave it blank.");
        }
        return email;
    }
}
