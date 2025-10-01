package org.mrp.models;

import java.sql.Timestamp;

public class Rating {
    private String id;
    private User creator;
    private MediaEntry mediaEntry;
    private int starValue;
    private String comment;
    private Timestamp createdAt;
    private int likes;

    public Rating() {}

    public Rating(User creator, MediaEntry mediaEntry, int starValue, String comment, Timestamp createdAt) {
        this.creator = creator;
        this.mediaEntry = mediaEntry;
        this.starValue = starValue;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public User getCreator() { return creator; }

    public MediaEntry getMediaEntry() { return mediaEntry; }

    public int getStarValue() { return starValue; }
    public void setStarValue(int starValue) { this.starValue = starValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

}
