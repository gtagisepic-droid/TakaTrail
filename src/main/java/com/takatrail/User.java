package com.takatrail;

/** Represents an authenticated application user without exposing credentials. */
public class User {
    private final int id;
    private final String fullName;
    private final String username;
    private final String passwordHash;
    private final String salt;

    public User(int id, String fullName, String username, String passwordHash, String salt) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }
}
