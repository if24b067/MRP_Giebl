package org.mrp.repositories;

import org.mrp.models.MediaEntry;
import org.mrp.models.Rating;
import org.mrp.utils.Database;
import org.mrp.utils.UUIDv7Generator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class RatingRepository implements Repository{
    private UUIDv7Generator uuidGenerator;
    private Database db;

    public RatingRepository() {
        uuidGenerator = new UUIDv7Generator();
        db = new Database();
    }

    //save information in db
    @Override
    public <T> UUID save(T t) throws SQLException {
        UUID rating_id = null;
        if(t instanceof Rating) {
            Rating rating = (Rating) t;

            rating_id = db.insert("INSERT INTO Ratings (rating_id, creator, media_entry, star_value, comment, created_at, likes, vis_flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    rating.getCreator(),
                    rating.getMediaEntry(),
                    rating.getStarValue(),
                    rating.getComment(),
                    LocalDate.now(),
                    rating.getLikes(),
                    rating.getVis()
            );
        }
        return rating_id;
    }

    @Override
    public <T> void update(T t) throws SQLException {
        if(t instanceof Rating) {
            Rating rating = (Rating) t;

            db.update("UPDATE Ratings SET star_value = ?, comment = ?, likes = ?, vis_flag = ? WHERE rating_id = ?",
                    rating.getStarValue(),
                    rating.getComment(),
                    rating.getLikes(),
                    rating.getVis(),
                    rating.getId()
            );
        }
    }

    @Override
    public void delete(UUID id) throws SQLException {
        db.update("DELETE FROM Ratings WHERE rating_id = ?", id);
    }

    @Override
    public List<Object> getAll() throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        ResultSet rs = db.query("SELECT * FROM Ratings");

        while (rs.next()) {

            Rating rating = new Rating(
                (UUID) rs.getObject("rating_id"),
                (UUID) rs.getObject("creator"),
                (UUID) rs.getObject("media_entry"),
                rs.getInt("star_value"),
                rs.getString("comment"),
                rs.getTimestamp("created_at"),
                rs.getInt("likes"),
                rs.getBoolean("vis_flag")
            );

            ratings.add(rating);
        }

        return new ArrayList<Object>(ratings);
    }

    @Override
    public Object getOne(UUID id) throws SQLException {
        ResultSet rs = db.query("SELECT * FROM Ratings WHERE rating_id = ?", id);

        if(rs.next())
        {
            return new Rating(
                (UUID) rs.getObject("rating_id"),
                (UUID) rs.getObject("creator"),
                (UUID) rs.getObject("media_entry"),
                rs.getInt("star_value"),
                rs.getString("comment"),
                rs.getTimestamp("created_at"),
                rs.getInt("likes"),
                rs.getBoolean("vis_flag")
            );

        }
        return null;
    }

    public boolean chkCreator(UUID rating_id, UUID user_id) throws SQLException {
        ResultSet rs = db.query("SELECT (creator) FROM Ratings WHERE rating_id = ?",
                rating_id);
        if(!rs.next()) {return false;}  //rating not found
        String creatorId = rs.getString("creator");
        String userId = user_id.toString();
        return Objects.equals(userId, creatorId);
    }

    public boolean chkUserAndMedia(UUID user_id, UUID media_id) throws SQLException {
        return db.exists("SELECT * FROM ratings WHERE creator = ? AND media_entry = ?",
                 user_id,
                 media_id
        );
    }

}
