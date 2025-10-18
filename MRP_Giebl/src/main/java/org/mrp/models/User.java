package org.mrp.models;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String passwordHash;
    private Timestamp createdAt;
    private String token;
    private List<MediaEntry> favourites;


    public User() {}

    public User(UUID id, String username, String passwordHash, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public Timestamp getCreatedAt() { return createdAt; }

    public String getToken() { return token; }
    public void SetToken(String token) { this.token = token; }

}