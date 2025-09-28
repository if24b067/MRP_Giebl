package org.mrp.models;

import java.sql.Timestamp;

public class User {
    private String id;
    private String username;
    private String passwordHash;
    private Timestamp createdAt;


    public User() {}

    public User(String id, String username, String passwordHash, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

}