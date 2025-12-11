package org.mrp.models;

import java.sql.Timestamp;
import java.util.UUID;

public class Rating {
    private UUID id;
    private UUID creator;
    private UUID mediaEntry;
    private int starValue;
    private String comment;
    private boolean visFlag;
    private Timestamp createdAt;
    private int likes;

    public Rating() {}

    public Rating(UUID creator, UUID mediaEntry, int starValue, String comment, Timestamp createdAt) {
        this.creator = creator;
        this.mediaEntry = mediaEntry;
        this.starValue = starValue;
        this.comment = comment;
        this.createdAt = createdAt;
        visFlag = false;
    }

    public UUID getCreator() {
        //possibly call function to get user from db to pass object
        return creator;
    }

    public UUID getMediaEntry() {
        //possibly call function to get mediaEntry from db to pass object
        return mediaEntry;
    }

    public int getStarValue() { return starValue; }
    public void setStarValue(int starValue) { this.starValue = starValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public boolean getVis() { return visFlag; }
    public void setVis(boolean vis) { visFlag = vis; }

}
