package org.mrp.repositories;

import org.mrp.models.Fav;
import org.mrp.models.MediaEntry;
import org.mrp.utils.Database;
import org.mrp.utils.UUIDv7Generator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MediaRepository implements Repository{
    private Database db;

    public MediaRepository() {
        db = new Database();
    }

    public void setDb(Database db) {
        this.db = db;
    }

    private String parseGenresToDB(List<String> genresList){
        //get genre List from object, convert to csv for db storage
        StringBuilder str = new StringBuilder();
        for (String genre : genresList) { str.append(genre).append(","); }

        String genres = str.toString();

        //remove last ,
        if (!genres.isEmpty()) genres = genres.substring(0, genres.length() - 1);

        return genres;
    }

    private List<String> parseGenresFromDB(String genresString){
        //convert csv to list
        List<String> genres = new ArrayList<>();
        if(genresString != null && !genresString.isEmpty())
        {
            genres = Arrays.asList(genresString.split(","));
        }
        return genres;
    }

    private List<Object> parseRS(ResultSet rs) throws SQLException {
        //method to parse rs to avoid redundancies
        List<MediaEntry> mediaEntries = new ArrayList<>();

        while (rs.next()) {

            List<String> genres = parseGenresFromDB(rs.getString("genres"));    //csv to list for easier handling

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

    //save mediaentry in db
    @Override
    public <T> UUID save(T t) throws SQLException {
        UUID media_id = null;
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;

            String genres = parseGenresToDB(mediaEntry.getGenres());    //list to csv for db storage

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

            String genres = parseGenresToDB(mediaEntry.getGenres());    //list to csv for db storage

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
        ResultSet rs = db.query("SELECT * FROM MediaEntries");
        return parseRS(rs); //get mediaentries from rs
    }

    @Override
    public Object getOne(UUID id) throws SQLException {
        ResultSet rs = db.query("SELECT * FROM MediaEntries WHERE media_id = ?", id);

        if(rs.next())   //only one result
        {
            List<String> genres = parseGenresFromDB(rs.getString("genres"));    //csv to list for easier handling

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
        return Objects.equals(userId, creatorId);   //chk whether found creatorId matches userId
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
        //chk whether entry has already be marked as favourite by this user
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
        //get lists of users most rated genres, types and age restrictions
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
        //get entries that match users most rated genre, type and age restriction
        String genre = "%" + preferences.get(0) + "%";
        String mediaType = preferences.get(1);
        Integer ageRestriction = preferences.get(2) != null ? Integer.parseInt(preferences.get(2)) : null;

        ResultSet rs = db.query("SELECT * FROM MediaEntries WHERE genres LIKE ?  AND age_restriction = ? AND type = ? LIMIT 5",
                genre,
                ageRestriction,
                mediaType);

        return parseRS(rs);
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
}

