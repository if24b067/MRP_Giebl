package org.mrp.repositories;

import org.mrp.models.MediaEntry;
import org.mrp.utils.Database;
import org.mrp.utils.UUIDv7Generator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class MediaRepository implements Repository{
    private UUIDv7Generator uuidGenerator;
    private Database db;

    public MediaRepository() {
        uuidGenerator = new UUIDv7Generator();
        db = new Database();
    }

    //save information in db
    @Override
    public <T> void save(T t) throws SQLException {
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;
            UUID user_id = db.insert("INSERT INTO MediaEntries (media_id, title, description, creator, release_year, age_restriction) VALUES (?, ?, ?, ?, ?, ?)",
                    mediaEntry.getTitle(),
                    mediaEntry.getDesc(),
                    mediaEntry.getCreator(),
                    mediaEntry.getReleaseYear(),
                    mediaEntry.getAgeRestriction()
            );
        }
    }

    @Override
    public <T> void update(T t) throws SQLException {
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;
            db.update("UPDATE MediaEntries SET title = ?, description = ?, release_year = ?, age_restriction = ? WHERE media_id = ?",
                    mediaEntry.getTitle(),
                    mediaEntry.getDesc(),
                    mediaEntry.getReleaseYear(),
                    mediaEntry.getAgeRestriction(),
                    mediaEntry.getId()
            );
        }
    }

    @Override
    public void delete(UUID id) throws SQLException {
        db.update("DELETE FROM MediaEntries WHERE media_id = ?", id);
        //delete in db
    }

    @Override
    public List<Map<String, Object>> get() throws SQLException {
        List<Map<String, Object>> response = new ArrayList<>();
        ResultSet rs = db.query("SELECT * FROM MediaEntries");

        while (rs.next()) {
            Map<String, Object> mediaEntry = new HashMap<>();
            mediaEntry.put("media_id", rs.getObject("media_id"));
            mediaEntry.put("title", rs.getString("title"));
            mediaEntry.put("description", rs.getString("description"));
            mediaEntry.put("creator", rs.getObject("creator"));
            mediaEntry.put("release_year", rs.getInt("release_year"));
            mediaEntry.put("age_restriction", rs.getInt("age_restriction"));

            response.add(mediaEntry);
        }

        return response;
    }

    public int calcAvgScore(UUID media_id) {
        int sum = 0;
        int cnt = 0;
        //get rating score for media_id from db
        /*for (Rating rating : ratings) {
            sum += rating.getStarValue();
            cnt++;
        }*/
        return sum/cnt;
    }

    public boolean chkCreator(UUID media_id, UUID user_id) throws SQLException {
        ResultSet rs = db.query("SELECT (creator) FROM MediaEntries WHERE media_id = ?",
                media_id);
        if(!rs.next()) {return false;}  //media entry not found
        String creatorId = rs.getString("creator");
        String userId = user_id.toString();
        return Objects.equals(userId, creatorId);
    }
}
/*
ResultSet rs = db.query("SELECT (user_id) FROM Users WHERE token = ?", token);
        if(!rs.next()) {return null;}  //token not found
        return UUID.fromString(rs.getString("user_id"));*/
