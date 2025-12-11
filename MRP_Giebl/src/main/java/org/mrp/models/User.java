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
        favourites = null;
    }

    public User(String username, String passwordHash, Timestamp createdAt) {
        id = null;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        favourites = null;
    }

    public User(UUID id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        favourites = null;
    }

    public UUID getId() { return id; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public Timestamp getCreatedAt() { return createdAt; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public List<MediaEntry> getFavourites() { return favourites; }
    public void setFavourites(MediaEntry media) { favourites.add(media); }

}