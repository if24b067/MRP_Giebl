package org.mrp.repositories;

import org.mrp.models.Rating;
import org.mrp.utils.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class RatingRepository implements Repository{
    private Database db;

    public RatingRepository() {
        db = new Database();
    }

    private List<Object> parseRS(ResultSet rs) throws SQLException {
        //method to avoid redundancies
        List<Rating> ratings = new ArrayList<>();

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

    //save rating in db
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
        ResultSet rs = db.query("SELECT * FROM Ratings");

        return parseRS(rs);
    }

    @Override
    public Object getOne(UUID id) throws SQLException {
//        db.update("ALTER TABLE favourites DROP CONSTRAINT favourites_user_id_fkey;");
//        db.update("ALTER TABLE favourites ADD CONSTRAINT favourites_user_id_fkey FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE;");
//        db.update("ALTER TABLE favourites DROP CONSTRAINT favourites_media_entry_fkey;");
//        db.update("ALTER TABLE favourites ADD CONSTRAINT favourites_media_entry_fkey FOREIGN KEY (media_entry) REFERENCES MediaEntries(media_id) ON DELETE CASCADE;");
//
//        db.update("ALTER TABLE ratings DROP CONSTRAINT ratings_creator_fkey;");
//        db.update("ALTER TABLE ratings ADD CONSTRAINT ratings_creator_fkey FOREIGN KEY (creator) REFERENCES Users(user_id) ON DELETE CASCADE;");
//        db.update("ALTER TABLE ratings DROP CONSTRAINT ratings_media_entry_fkey;");
//        db.update("ALTER TABLE ratings ADD CONSTRAINT ratings_media_entry_fkey FOREIGN KEY (media_entry) REFERENCES MediaEntries(media_id) ON DELETE CASCADE;");
//
//        db.update("ALTER TABLE mediaentries DROP CONSTRAINT mediaentries_creator_fkey;");
//        db.update("ALTER TABLE mediaentries ADD CONSTRAINT mediaentries_creator_fkey FOREIGN KEY (creator) REFERENCES Users(user_id) ON DELETE CASCADE;");
//
//
//        ResultSet test2 = db.query("SELECT\n" +
//                "    conname AS constraint_name,\n" +
//                "    conrelid::regclass AS table_name,\n" +
//                "    a.attname AS column_name,\n" +
//                "    confdeltype AS on_delete_action\n" +
//                "FROM\n" +
//                "    pg_constraint c\n" +
//                "JOIN\n" +
//                "    pg_attribute a ON a.attnum = ANY(c.conkey) AND a.attrelid = c.conrelid\n" +
//                "WHERE\n" +
//                "    c.contype = 'f';");
//        int columnCount = test2.getMetaData().getColumnCount();
//        while (test2.next()) {
//            for (int i = 1; i <= columnCount; i++) {
//                System.out.print(test2.getString(i) + "\t"); // Print each column's value
//            }
//            System.out.println(); // New line for the next row
//        }

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

    public List<Object> getAllOfMedia(UUID mediaId) throws SQLException {
        //get all ratings from specific media entry
        ResultSet rs = db.query("SELECT * FROM Ratings WHERE media_entry = ?", mediaId);

        return parseRS(rs);
    }

    public List<Object> getOwn(UUID id) throws SQLException {
        ResultSet rs = db.query("SELECT * FROM Ratings WHERE creator = ?", id);

        return parseRS(rs);
    }


    public boolean chkCreator(UUID rating_id, UUID user_id) throws SQLException {
        ResultSet rs = db.query("SELECT (creator) FROM Ratings WHERE rating_id = ?",
                rating_id);
        if(!rs.next()) {return false;}  //rating not found
        String creatorId = rs.getString("creator");
        String userId = user_id.toString();
        return Objects.equals(userId, creatorId);   //chk whether creatorId is provided userId
    }

    public boolean chkUserAndMedia(UUID user_id, UUID media_id) throws SQLException {
        //chk whether user has alredy rated mediaentry
        return db.exists("SELECT * FROM ratings WHERE creator = ? AND media_entry = ?",
                 user_id,
                 media_id
        );
    }

    public boolean chkUserAndRating(UUID user_id, UUID rating_id) throws SQLException {
        //chk whether user has already liked rating
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
