package com.example.mp3player.utils;

import android.util.Patterns;

public class ValidationUtils {
    
    /**
     * Validate email format
     * @param email The email to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Validate password meets minimum requirements
     * @param password The password to validate
     * @return true if password is valid (at least 6 characters), false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 6;
    }
    
    /**
     * Validate username format
     * @param username The username to validate
     * @return true if username is valid (3-20 characters, alphanumeric and underscore), false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String trimmed = username.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 20 
                && trimmed.matches("^[a-zA-Z0-9_]+$");
    }
}
