package com.takatrail;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Handles registration, secure password verification, and login state. */
public class AuthManager {
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;

    private final DatabaseManager databaseManager;
    private final SecureRandom secureRandom = new SecureRandom();
    private User currentUser;

    public AuthManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public User register(String fullName, String username, char[] password, char[] confirmPassword)
            throws SQLException {
        String cleanName = fullName == null ? "" : fullName.trim();
        String cleanUsername = username == null ? "" : username.trim();
        validateRegistration(cleanName, cleanUsername, password, confirmPassword);

        if (databaseManager.usernameExists(cleanUsername)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        String passwordHash = hashPassword(password, salt);
        return databaseManager.createUser(
                cleanName,
                cleanUsername,
                passwordHash,
                Base64.getEncoder().encodeToString(salt));
    }

    private void validateRegistration(String fullName, String username, char[] password, char[] confirmPassword) {
        if (fullName.isBlank() || username.isBlank() || password == null || password.length == 0
                || confirmPassword == null || confirmPassword.length == 0) {
            throw new IllegalArgumentException("All registration fields are required.");
        }
        if (password.length < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!MessageDigest.isEqual(toBytes(password), toBytes(confirmPassword))) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
    }

    public User authenticate(String username, char[] password) throws SQLException {
        // Every login attempt starts unauthenticated so failed credentials cannot retain an older session.
        currentUser = null;
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.isBlank() || password == null || password.length == 0) {
            return null;
        }

        User user = databaseManager.findUserByUsername(cleanUsername);
        if (user == null) {
            return null;
        }
        byte[] salt;
        byte[] expectedHash;
        try {
            salt = Base64.getDecoder().decode(user.getSalt());
            expectedHash = Base64.getDecoder().decode(user.getPasswordHash());
        } catch (IllegalArgumentException exception) {
            System.err.println("Stored credentials could not be decoded: " + exception.getMessage());
            return null;
        }

        byte[] actualHash = hashPasswordBytes(password, salt);
        if (MessageDigest.isEqual(expectedHash, actualHash)) {
            currentUser = user;
            return user;
        }
        return null;
    }

    // PBKDF2 deliberately combines a random salt with many iterations.
    private String hashPassword(char[] password, byte[] salt) {
        return Base64.getEncoder().encodeToString(hashPasswordBytes(password, salt));
    }

    private byte[] hashPasswordBytes(char[] password, byte[] salt) {
        PBEKeySpec specification = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(specification).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Secure password hashing is unavailable.", exception);
        } finally {
            specification.clearPassword();
        }
    }

    private byte[] toBytes(char[] characters) {
        byte[] bytes = new byte[characters.length * 2];
        for (int i = 0; i < characters.length; i++) {
            bytes[i * 2] = (byte) (characters[i] >> 8);
            bytes[i * 2 + 1] = (byte) characters[i];
        }
        return bytes;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }
}
