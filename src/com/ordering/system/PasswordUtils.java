package com.ordering.system;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// A utility class for handling password hashing.
public class PasswordUtils {

	// Hashes a plain text password using the SHA-256 algorithm.
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        try {
            // Get an instance of the SHA-256 message digest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Hash the password
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Encode the hash in Base64 to get a string representation
            return Base64.getEncoder().encodeToString(hash);
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: SHA-256 algorithm not found.");
            e.printStackTrace();
            throw new RuntimeException("Could not hash password", e);
        }
    }


    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        // Hash the plain text password to see if it matches the stored hash
        String newHash = hashPassword(plainPassword);
        
        return newHash.equals(hashedPassword);
    }
}
