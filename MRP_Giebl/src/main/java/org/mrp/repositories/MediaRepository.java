package org.mrp.repositories;

import org.mrp.models.Fav;
import org.mrp.models.MediaEntry;
import org.mrp.utils.Database;
import org.mrp.utils.UUIDv7Generator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class MediaRepository implements Repository{
    private UUIDv7Generator uuidGenerator;
    private Database db;

    public MediaRepository() {
        uuidGenerator = new UUIDv7Generator();
        db = new Database();
    }

    public void setDb(Database db) {
        this.db = db;
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

    private List<Object> parseRS(ResultSet rs) throws SQLException {
        List<MediaEntry> mediaEntries = new ArrayList<>();

        while (rs.next()) {

            List<String> genres = parseGenresFromDB(rs.getString("genres"));

            MediaEntry mediaEntry = new  MediaEntry(
                    (UUID) rs.getObject("media_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    (UUID) rs.getObject("creator"),
                    rs.getInt("release_year"),
                    rs.getInt("age_restriction"),
                    genres,
                    rs.getString("type")
            );

            mediaEntries.add(mediaEntry);
        }

        return new ArrayList<Object>(mediaEntries);
    }

    //save information in db
    @Override
    public <T> UUID save(T t) throws SQLException {
        UUID media_id = null;
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;

            String genres = parseGenresToDB(mediaEntry.getGenres());

            media_id = db.insert("INSERT INTO MediaEntries (media_id, title, description, creator, release_year, age_restriction, genres, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    mediaEntry.getTitle(),
                    mediaEntry.getDesc(),
                    mediaEntry.getCreator(),
                    mediaEntry.getReleaseYear(),
                    mediaEntry.getAgeRestriction(),
                    genres,
                    mediaEntry.getType()

            );
        }
        return media_id;
    }

    @Override
    public <T> void update(T t) throws SQLException {
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;

            String genres = parseGenresToDB(mediaEntry.getGenres());

            db.update("UPDATE MediaEntries SET title = ?, description = ?, release_year = ?, age_restriction = ?, genres = ?, type = ? WHERE media_id = ?",
                    mediaEntry.getTitle(),
                    mediaEntry.getDesc(),
                    mediaEntry.getReleaseYear(),
                    mediaEntry.getAgeRestriction(),
                    genres,
                    mediaEntry.getType(),
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
                    genres,
                    rs.getString("type")
                    );

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
                    genres,
                    rs.getString("type"));
        }
        return null;
    }

    public boolean chkCreator(UUID media_id, UUID user_id) throws SQLException {
        ResultSet rs = db.query("SELECT (creator) FROM MediaEntries WHERE media_id = ?",
                media_id);
        if(!rs.next()) {return false;}  //media entry not found
        String creatorId = rs.getString("creator");
        String userId = user_id.toString();
        return Objects.equals(userId, creatorId);
    }

    public void saveFav(UUID user_id, UUID media_id) throws SQLException {
        UUID fav_id = db.insert("INSERT INTO favourites (fav_id, media_entry, user_id) VALUES (?, ?, ?)",
                media_id,
                user_id
        );
    }

    public void delFav(UUID user_id, UUID media_id) throws SQLException {
        db.update("DELETE FROM favourites WHERE media_entry = ? AND user_id = ?;",
                media_id,
                user_id
        );
    }

    public boolean chkEntry(UUID id) throws SQLException {
        return db.exists("SELECT * FROM mediaentries WHERE media_id = ?", id);
    }

    public boolean chkFav(UUID media_id, UUID user_id) throws SQLException {
        return db.exists("SELECT * FROM favourites WHERE media_entry = ? AND user_id = ?",
                media_id, user_id);
    }

    public List<Object> getFav(UUID user_id) throws SQLException {
        List<Fav> favourites = new ArrayList<>();
        ResultSet rs = db.query("SELECT * FROM favourites WHERE user_id = ?", user_id);

        while (rs.next()) {

            Fav fav = new Fav(
                    (UUID) rs.getObject("fav_id"),
                    (UUID) rs.getObject("user_id"),
                    (UUID) rs.getObject("media_entry")
            );

            favourites.add(fav);
        }

        return new ArrayList<Object>(favourites);
    }

    public List<List<String>> getUserPreferences(UUID user_id) throws SQLException{
        List<String> genres = new ArrayList<>();
        List<String> mediaTypes = new ArrayList<>();
        List<String> ageRestrictions = new ArrayList<>();
        ResultSet rs = db.query("SELECT m.genres, m.age_restriction, m.type FROM ratings r JOIN mediaentries m ON r.media_entry = m.media_id WHERE r.creator = ? AND r.star_value >= 4;", user_id);

        while (rs.next()) {
            genres.addAll(parseGenresFromDB(rs.getString("genres")));
            mediaTypes.add(rs.getString("type"));
            ageRestrictions.add(rs.getString("age_restriction"));
        }
        return List.of(genres, mediaTypes, ageRestrictions);
    }

    public List<Object> getByPreference(List<String> preferences) throws SQLException {
        List<MediaEntry> mediaEntries = new ArrayList<>();
        String genre = "%" + preferences.get(0) + "%";
        String mediaType = preferences.get(1);
        Integer ageRestriction = preferences.get(2) != null ? Integer.parseInt(preferences.get(2)) : null;

        // Prepare the SQL statement
        String query = "SELECT * FROM MediaEntries WHERE " +
                "(genres LIKE ? OR ? IS NULL) " +
                "AND (age_restriction = ? OR ? IS NULL) " +
                "AND (type = ? OR ? IS NULL) " +
                "LIMIT 5";

        // Create a PreparedStatement
        try (PreparedStatement stmt = db.getConnection().prepareStatement(query)) {
            // Set the parameters
            stmt.setString(1, genre);
            stmt.setString(2, genre); // For NULL check
            if (ageRestriction != null) {
                stmt.setInt(3, ageRestriction);
            } else {
                stmt.setNull(3, Types.INTEGER); // Type for age_restriction
            }
            stmt.setObject(4, ageRestriction, Types.INTEGER); // For NULL check
            if (mediaType != null) {
                stmt.setString(5, mediaType);
            } else {
                stmt.setNull(5, Types.VARCHAR); // Type for media type
            }
            stmt.setObject(6, mediaType, Types.VARCHAR); // For NULL check

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                List<String> genres = parseGenresFromDB(rs.getString("genres"));

                MediaEntry mediaEntry = new MediaEntry(
                        (UUID) rs.getObject("media_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        (UUID) rs.getObject("creator"),
                        rs.getInt("release_year"),
                        rs.getInt("age_restriction"),
                        genres,
                        rs.getString("type")
                );

                mediaEntries.add(mediaEntry);
            }
        }

        return new ArrayList<>(mediaEntries);
    }


//    public List<Object> getByGenre(String genre) throws SQLException {
//        List<MediaEntry> mediaEntries = new ArrayList<>();
//        genre = "%"+genre+"%";
//        ResultSet rs = db.query("SELECT * FROM MediaEntries WHERE genres LIKE ? LIMIT 5", genre);
//
//        while (rs.next()) {
//
//            List<String> genres = parseGenresFromDB(rs.getString("genres"));
//
//            MediaEntry mediaEntry = new  MediaEntry(
//                    (UUID) rs.getObject("media_id"),
//                    rs.getString("title"),
//                    rs.getString("description"),
//                    (UUID) rs.getObject("creator"),
//                    rs.getInt("release_year"),
//                    rs.getInt("age_restriction"),
//                    genres,
//                    rs.getString("type"));
//
//            mediaEntries.add(mediaEntry);
//        }
//
//        return new ArrayList<Object>(mediaEntries);
//    }

    public List<Object> getByTitle(String title) throws SQLException {
        List<MediaEntry> mediaEntries = new ArrayList<>();
        title = "%"+title+"%";
        ResultSet rs = db.query("SELECT * FROM MediaEntries WHERE title LIKE ?", title);

        while (rs.next()) {

            List<String> genres = parseGenresFromDB(rs.getString("genres"));

            MediaEntry mediaEntry = new  MediaEntry(
                    (UUID) rs.getObject("media_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    (UUID) rs.getObject("creator"),
                    rs.getInt("release_year"),
                    rs.getInt("age_restriction"),
                    genres,
                    rs.getString("type"));

            mediaEntries.add(mediaEntry);
        }

        return new ArrayList<Object>(mediaEntries);
    }

    public List<Object> sorted(char flag) throws SQLException {
        List<MediaEntry> mediaEntries = new ArrayList<>();
        //ResultSet rs = db.query("SELECT * FROM MediaEntries WHERE title LIKE ?", title);

        if(flag == 't') return parseRS(db.query("SELECT * FROM MediaEntries ORDER BY title"));
        else if(flag == 'y') return parseRS(db.query("SELECT * FROM MediaEntries ORDER BY release_year"));
        else return new ArrayList<>();
        //return new ArrayList<Object>(mediaEntries);
    }

    public List<Object> sortedByScore() throws SQLException {
        List<MediaEntry> mediaEntries = new ArrayList<>();
        return new ArrayList<Object>(mediaEntries);
    }
}

