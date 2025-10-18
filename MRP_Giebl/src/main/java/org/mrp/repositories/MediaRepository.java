package org.mrp.repositories;

import org.mrp.models.MediaEntry;
import org.mrp.utils.UUIDv7Generator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MediaRepository implements Repository{

    public MediaRepository() {
    }

    //save information in db
    @Override
    public <T> void save(T t) {
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;
            //save to db
        }
    }

    @Override
    public <T> void update(T t) {
        if(t instanceof MediaEntry) {
            MediaEntry mediaEntry = (MediaEntry) t;
            //update in db
        }
    }

    @Override
    public void delete(UUID id) {
        //delete in db
    }

    @Override
    public <T> List<T> get() {
        //get from db  String title, String desc, UUID creator, int releaseYear, int ageRestriction, List<String> genres) {
        UUIDv7Generator uuidv7Generator = new UUIDv7Generator();
        List<String> genres = new ArrayList<>();
        genres.add("comedy");
        genres.add("crocodile");
        genres.add("alligator");

        MediaEntry mediaEntry = new MediaEntry("name", "movie", uuidv7Generator.randomUUID(), 2000, 12, genres);
        MediaEntry mediaEntry1 = new MediaEntry("another", "film", uuidv7Generator.randomUUID(), 2003, 16, genres);

        List<MediaEntry> mediaEntries = new ArrayList<>();
        mediaEntries.add(mediaEntry);
        mediaEntries.add(mediaEntry1);
        return (List<T>) mediaEntries;
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

    public boolean chkCreator(UUID media_id, UUID user_id) {
        //chk with db
        return true;
    }
}
