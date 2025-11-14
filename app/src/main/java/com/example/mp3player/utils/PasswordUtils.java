package com.example.mp3player.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {
    
    /**
     * Hash a password using SHA-256
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes());
            
            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            android.util.Log.e("PasswordUtils", "Error hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Check if a plain password matches a hashed password
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password to check against
     * @return true if passwords match, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            String hashedInput = hashPassword(plainPassword);
            return hashedInput.equals(hashedPassword);
        } catch (Exception e) {
            android.util.Log.e("PasswordUtils", "Error checking password", e);
            return false;
        }
    }
}
