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

    //getters
    public String getId() { return id; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public Timestamp getCreatedAt() { return createdAt; }

}