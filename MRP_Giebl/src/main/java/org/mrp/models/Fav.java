package org.mrp.models;

import java.util.UUID;

public class Fav {
    private UUID id;
    private UUID user;
    private UUID entry_id;

    public Fav(UUID id, UUID user, UUID entry_id) {
        this.id = id;
        this.user = user;
        this.entry_id = entry_id;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) {this.id = id; }

    public UUID getUser() {return user;}
    public void setUser(UUID user) {this.user = user;}

    public UUID getEntry_id() {return entry_id;}
    public void setEntry_id(UUID entry_id) {this.entry_id = entry_id;}
}
