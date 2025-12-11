package org.mrp.repositories;

import org.mrp.models.MediaEntry;
import org.mrp.models.Rating;
import org.mrp.utils.Database;
import org.mrp.utils.UUIDv7Generator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MediaRepository implements Repository{
    private UUIDv7Generator uuidGenerator;
    private Database db;

    public MediaRepository() {
        uuidGenerator = new UUIDv7Generator();
        db = new Database();
    }

    private String parseGenresToDB(List<String> genresList){
        //get genre List from object, convert to csl for db storage
        StringBuilder str = new StringBuilder();
        for (String genre : genresList) { str.append(genre).append(","); }

        String genres = str.toString();

        if (!genres.isEmpty()) genres = genres.substring(0, genres.length() - 1);

        return genres;
    }

    private List<String> parseGenresFromDB(String genresString){
        List<String> genres = new ArrayList<>();
        if(genresString != null && !genresString.isEmpty())
        {
            genres = Arrays.asList(genresString.split(","));
        }
        return genres;
    }

    //save information in db
    @Override
    public <T> UUID save(T t) throws SQLException {
        UUID media_id = null;
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;

            String genres = parseGenresToDB(mediaEntry.getGenres());

            media_id = db.insert("INSERT INTO MediaEntries (media_id, title, description, creator, release_year, age_restriction, genres) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    mediaEntry.getTitle(),
                    mediaEntry.getDesc(),
                    mediaEntry.getCreator(),
                    mediaEntry.getReleaseYear(),
                    mediaEntry.getAgeRestriction(),
                    genres

            );
        }
        return media_id;
    }

    @Override
    public <T> void update(T t) throws SQLException {
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;

            String genres = parseGenresToDB(mediaEntry.getGenres());

            db.update("UPDATE MediaEntries SET title = ?, description = ?, release_year = ?, age_restriction = ?, genres = ? WHERE media_id = ?",
                    mediaEntry.getTitle(),
                    mediaEntry.getDesc(),
                    mediaEntry.getReleaseYear(),
                    mediaEntry.getAgeRestriction(),
                    genres,
                    mediaEntry.getId()
            );
        }
    }

    @Override
    public void delete(UUID id) throws SQLException {
        db.update("DELETE FROM MediaEntries WHERE media_id = ?", id);
    }

    @Override
    public List<Object> getAll() throws SQLException {
        List<MediaEntry> mediaEntries = new ArrayList<>();
        ResultSet rs = db.query("SELECT * FROM MediaEntries");

        while (rs.next()) {

            List<String> genres = parseGenresFromDB(rs.getString("genres"));

            MediaEntry mediaEntry = new  MediaEntry(
                    (UUID) rs.getObject("media_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    (UUID) rs.getObject("creator"),
                    rs.getInt("release_year"),
                    rs.getInt("age_restriction"),
                    genres);

            mediaEntries.add(mediaEntry);
        }

        return new ArrayList<Object>(mediaEntries);
    }

    @Override
    public Object getOne(UUID id) throws SQLException {
        ResultSet rs = db.query("SELECT * FROM MediaEntries WHERE media_id = ?", id);

        if(rs.next())
        {
            List<String> genres = parseGenresFromDB(rs.getString("genres"));

            return new  MediaEntry(id,
                    rs.getString("title"),
                    rs.getString("description"),
                    (UUID) rs.getObject("creator"),
                    rs.getInt("release_year"),
                    rs.getInt("age_restriction"),
                    genres);
        }
        return null;
    }


    public int calcAvgScore(UUID media_id) {
//        int sum = 0;
//        for(Rating r : ratings) {
//            sum += r.getStarValue();
//        }
//        return sum / ratings.size();
        return -1;
    };

    public boolean chkCreator(UUID media_id, UUID user_id) throws SQLException {
        ResultSet rs = db.query("SELECT (creator) FROM MediaEntries WHERE media_id = ?",
                media_id);
        if(!rs.next()) {return false;}  //media entry not found
        String creatorId = rs.getString("creator");
        String userId = user_id.toString();
        return Objects.equals(userId, creatorId);
    }
}

