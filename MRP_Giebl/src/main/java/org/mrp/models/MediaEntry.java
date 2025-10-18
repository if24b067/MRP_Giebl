package org.mrp.models;


import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class MediaEntry {
    private UUID id;
    private String title;
    private String desc;
    private UUID creator;
    private int releaseYear;
    private int ageRestriction;
    private List<String> genres;
    //private List<Rating> ratings;

    public MediaEntry() {}

    public MediaEntry(String title, String desc, UUID creator, int releaseYear, int ageRestriction, List<String> genres) {
        this.title = title;
        this.desc = desc;
        this.creator = creator;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
        this.genres = genres;
        id =  UUID.randomUUID();
    }

    /*public int calcAvgScore() {
        int sum = 0;
        int cnt = 0;
        for (Rating rating : ratings) {
            sum += rating.getStarValue();
            cnt++;
        }
        return sum/cnt;
    }*/

    public UUID getId() { return id; }
    public void setId(UUID id) {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public UUID getCreator() { return creator; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public int getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(int ageRestriction) { this.ageRestriction = ageRestriction; }

    public List<String> getGenres() { return genres; }
    public void setGenres(String genre) { genres.add(genre); }

    /*public List<Rating> getRatings() { return ratings; }
    public void setRating(Rating rating) { ratings.add(rating); }*/
}
