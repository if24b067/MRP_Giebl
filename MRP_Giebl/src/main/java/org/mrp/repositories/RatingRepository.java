package org.mrp.repositories;

import org.mrp.models.LeaderBoard;
import org.mrp.models.MediaEntry;
import org.mrp.models.Rating;
import org.mrp.models.User;
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

            rating_id = db.insert("INSERT INTO Ratings (rating_id, creator, media_entry, star_value, comment, created_at, vis_flag) VALUES ( ?, ?, ?, ?, ?, ?, ?)",
                    rating.getCreator(),
                    rating.getMediaEntry(),
                    rating.getStarValue(),
                    rating.getComment(),
                    LocalDate.now(),
                    rating.getVis()
            );
        }
        return rating_id;
    }

    @Override
    public <T> void update(T t) throws SQLException {
        if(t instanceof Rating) {
            Rating rating = (Rating) t;

            db.update("UPDATE Ratings SET star_value = ?, comment = ?, vis_flag = ? WHERE rating_id = ?",
                    rating.getStarValue(),
                    rating.getComment(),
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
                rs.getBoolean("vis_flag")
            );

            ratings.add(rating);
        }

        return new ArrayList<Object>(ratings);
    }

    @Override
    public Object getOne(UUID id) throws SQLException {
        //db.update("CREATE TABLE Likes ( like_id UUID PRIMARY KEY, rating_id UUID, user_id UUID, FOREIGN KEY (rating_id) REFERENCES Ratings(rating_id), FOREIGN KEY (user_id) REFERENCES Users(user_id));");

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
                rs.getBoolean("vis_flag")
            );

        }
        return null;
    }

    public List<Object> getOwn(UUID id) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        ResultSet rs = db.query("SELECT * FROM Ratings WHERE creator = ?", id);

        while (rs.next()) {

            Rating rating = new Rating(
                    (UUID) rs.getObject("rating_id"),
                    (UUID) rs.getObject("creator"),
                    (UUID) rs.getObject("media_entry"),
                    rs.getInt("star_value"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at"),
                    rs.getBoolean("vis_flag")
            );

            ratings.add(rating);
        }

        return new ArrayList<Object>(ratings);
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

    public boolean chkUserAndRating(UUID user_id, UUID rating_id) throws SQLException {
        return db.exists("SELECT * FROM likes WHERE user_id = ? AND rating_id = ?",
                user_id,
                rating_id
        );
    }

    public int getCntOfLikes(UUID id) throws SQLException {
        ResultSet rs = db.query("SELECT COUNT(*) FROM likes WHERE rating_id = ?;", id);
        int cnt = 0;
        if (rs.next()) {
            cnt = rs.getInt(1);
        }
        return cnt;
    }

    public void like(UUID user_id, UUID rating_id) throws SQLException {
        UUID like_id = db.insert("INSERT INTO likes (like_id, user_id, rating_id) VALUES ( ?, ?, ?)",
                user_id,
                rating_id
        );
    }
}
