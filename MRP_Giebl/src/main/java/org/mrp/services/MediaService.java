package org.mrp.services;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.MediaEntry;
import org.mrp.models.Rating;
import org.mrp.repositories.MediaRepository;
import org.mrp.repositories.RatingRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class MediaService {
    private MediaRepository mediaRepository;
    private AuthService authService;

    public MediaService() {
        mediaRepository = new MediaRepository();
        authService = new AuthService();
    }


    public void createMedia(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        //chk if response has body
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            JsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = JsonHelper.parseRequest(exchange, Map.class);
        String title = request.get("title");
        String desc = request.get("desc");
        String type = request.get("type");

        Integer releaseYear = null;
        String releaseYearStr = request.get("releaseYear");
        if (releaseYearStr != null) {
            try {
                releaseYear = Integer.parseInt(releaseYearStr);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        Integer ageRestriction = null;
        String ageRestrictionStr = request.get("ageRestriction");
        if (ageRestrictionStr != null) {
            try {
                ageRestriction = Integer.parseInt(ageRestrictionStr);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        List<String> genres = null;
        String genresStr = request.get("genres");
        if (genresStr != null && !genresStr.trim().isEmpty()) {
            genres = Arrays.asList(genresStr.split(","));
        } else {
            JsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }


        UUID creator = user_id;

        //validate input
        if (title == null || title.trim().isEmpty() ||
                desc == null || desc.trim().isEmpty() ||
                type == null || type.trim().isEmpty() ||
                (!type.equals("MOVIE") && !type.equals("SERIES") && !type.equals("GAME"))) {
            JsonHelper.sendError(exchange, 400, "Correct input required");
            return;
        }

        if (title.length() > 30 || desc.length() > 100 || genres.size() > 10) {
            JsonHelper.sendError(exchange, 400, "input too long");
            return;
        }


        MediaEntry mediaEntry = new MediaEntry(title, desc, user_id, releaseYear, ageRestriction, genres, type);

        //call repo function
        UUID mediaId = mediaRepository.save(mediaEntry);
        MediaEntry entry = (MediaEntry) mediaRepository.getOne(mediaId);

        JsonHelper.sendResponse(exchange, 201, entry);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {

        if(authService.validateToken(exchange) == null) {return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID media_id = UUID.fromString(tmpValues[tmpValues.length-1]);
            MediaEntry mediaEntry = (MediaEntry) mediaRepository.getOne(media_id);

            if(mediaEntry == null){
                JsonHelper.sendError(exchange, 404, "Media not found");
                return;
            }

            JsonHelper.sendResponse(exchange, 200, mediaEntry);

        } catch (IllegalArgumentException exception){
            List<Object> response = mediaRepository.getAll();

            JsonHelper.sendResponse(exchange, 200, response);
        }
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        //chk if response has body
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            JsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = JsonHelper.parseRequest(exchange, Map.class);
        UUID media_id = UUID.fromString(request.get("media_id"));

        //chk whether User is creator
        boolean isCreator = mediaRepository.chkCreator(media_id, user_id);

        if(!isCreator){
            JsonHelper.sendError(exchange, 401, "unauthorized to edit post");
            return;
        }

        //get rest of info if authorized
        String title = request.get("title");
        String desc = request.get("desc");
        String type = request.get("type");

        Integer releaseYear = null;
        String releaseYearStr = request.get("releaseYear");
        if (releaseYearStr != null) {
            try {
                releaseYear = Integer.parseInt(releaseYearStr);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        Integer ageRestriction = null;
        String ageRestrictionStr = request.get("ageRestriction");
        if (ageRestrictionStr != null) {
            try {
                ageRestriction = Integer.parseInt(ageRestrictionStr);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        List<String> genres = null;
        String genresStr = request.get("genres");
        if (genresStr != null && !genresStr.trim().isEmpty()) {
            genres = Arrays.asList(genresStr.split(","));
        } else {
            JsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        if (title == null || title.trim().isEmpty() ||
                desc == null || desc.trim().isEmpty() ||
                type == null || type.trim().isEmpty() ||
                (!type.equals("MOVIE") && !type.equals("SERIES") && !type.equals("GAME"))) {
            JsonHelper.sendError(exchange, 400, "Correct input required");
            return;
        }
        if (title.length() > 30 || desc.length() > 100 || genres.size() > 10) {
            JsonHelper.sendError(exchange, 400, "input too long");
            return;
        }

        MediaEntry mediaEntry = new MediaEntry(media_id, title, desc, user_id, releaseYear, ageRestriction, genres, type);

        //call repo function
        mediaRepository.update(mediaEntry);

        JsonHelper.sendResponse(exchange, 201, mediaEntry);
    }

    public void delete(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        //get info from exchange
        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");
        UUID media_id = null;
        try{
            media_id = UUID.fromString(tmpValues[tmpValues.length-1]);
        } catch (IllegalArgumentException exception){
            JsonHelper.sendError(exchange, 400, "media Id required");
            return;
        }

        //chk whether entry exists
        MediaEntry mediaEntry = (MediaEntry) mediaRepository.getOne(media_id);

        if(mediaEntry == null) {
            JsonHelper.sendError(exchange, 404, "Media entry not found");
            return;
        }

        //chk whether User is creator
        boolean isCreator = mediaRepository.chkCreator(media_id, user_id);

        if(!isCreator){
            JsonHelper.sendError(exchange, 401, "unauthorized to delete post");
            return;
        }

        mediaRepository.delete(media_id);
        JsonHelper.sendResponse(exchange, 200, mediaEntry);
    }

    public void getAvgRating(HttpExchange exchange) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID media_id = UUID.fromString(tmpValues[tmpValues.length-1]);
            RatingRepository ratingRepository = new RatingRepository();
            List<Object> response = ratingRepository.getAll();

            float sum = 0;
            for(Object o : response){
                if(o instanceof Rating){
                    Rating rating = (Rating) o;
                    sum += rating.getStarValue();
                }
            }
            float score = sum/response.size();
            //TODO: correct Json response
            String avgScore = JsonHelper.toJson(score);
            JsonHelper.sendResponse(exchange, 200, avgScore);

        } catch (IllegalArgumentException exception){
            JsonHelper.sendError(exchange, 404, "Media not found");
        }
    }

    public void getAllRatings(HttpExchange exchange) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID media_id = UUID.fromString(tmpValues[tmpValues.length-1]);
            RatingRepository ratingRepository = new RatingRepository();
            List<Object> ratings = ratingRepository.getAll();

            //chk flag for visibility of comment
            for(Object r : ratings){
                if(r instanceof Rating rating){
                    if(!rating.getVis()) rating.setComment(null);
                }
            }

            JsonHelper.sendResponse(exchange, 200, ratings);

        } catch (IllegalArgumentException exception){
            JsonHelper.sendError(exchange, 404, "Media not found");
        }
    }

    public void addFavourite(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        //chk if response has body
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            JsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = JsonHelper.parseRequest(exchange, Map.class);

        UUID media_id = null;
        String id = request.get("media_id");
        if (id != null) {
            try{
                media_id = UUID.fromString(id);
            }catch (IllegalArgumentException e){
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        if(!mediaRepository.chkEntry(media_id)) {
            JsonHelper.sendError(exchange, 404, "media entry not found");
            return;
        }

        if(mediaRepository.chkFav(media_id, user_id)) {
            JsonHelper.sendError(exchange, 404, "entry already favourited");
            return;
        }

        //call repo function
        mediaRepository.saveFav(user_id, media_id);
        List<Object> favourites = mediaRepository.getFav(user_id);

        JsonHelper.sendResponse(exchange, 201, favourites);
    }

    public void remFavourite(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID media_id = UUID.fromString(tmpValues[tmpValues.length-1]);
            if(!mediaRepository.chkFav(media_id, user_id)) {
                JsonHelper.sendError(exchange, 404, "media entry not found");
                return;
            }

            mediaRepository.delFav(user_id, media_id);
            List<Object> favourites = mediaRepository.getFav(user_id);

            JsonHelper.sendResponse(exchange, 200, favourites);

        } catch (IllegalArgumentException exception){
            JsonHelper.sendError(exchange, 404, "correct input required");
        }
    }

    public void readFav(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        List<Object> favourites = mediaRepository.getFav(user_id);

        JsonHelper.sendResponse(exchange, 200, favourites);

    }

//    public void getRecommendations(HttpExchange exchange) throws IOException, SQLException {
//        UUID user_id = authService.validateToken(exchange);
//        if(user_id==null){return;}
//
//        List<Object> favourites = mediaRepository.getFav(user_id);
//        List<String> genres = new ArrayList<>();
//        for( Object f : favourites){
//            if(f instanceof Fav fav){
//                //UUID id = fav.getEntry_id();
//                MediaEntry entry = (MediaEntry) mediaRepository.getOne(fav.getEntry_id());
//                genres.addAll(entry.getGenres());
//            }
//        }
//
//        //determine most frequent genre
//        Map<String, Integer> genreCount = new HashMap<>();
//
//        for (String genre : genres) {
//            genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
//        }
//
//        String mostFrequentGenre = null;
//        int maxCount = 0;
//
//        for (Map.Entry<String, Integer> entry : genreCount.entrySet()) {
//            if (entry.getValue() > maxCount) {
//                maxCount = entry.getValue();
//                mostFrequentGenre = entry.getKey();
//            }
//        }
//
//        List<Object> recommendations = mediaRepository.getByGenre(mostFrequentGenre);
//        JsonHelper.sendResponse(exchange, 200, recommendations);
//    }

    private String getMostFrequent(List<String> attribute) {
        Map<String, Integer> genreCount = new HashMap<>();
        for (String a : attribute) {
            genreCount.put(a, genreCount.getOrDefault(a, 0) + 1);
        }

        String mostFrequent = "";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : genreCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }

        return mostFrequent;
    }

    public void getRecommendations(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        List<List<String>> preferences = mediaRepository.getUserPreferences(user_id);
        List<String> genres = preferences.get(0);
        List<String> mediaTypes = preferences.get(1);
        List<String> ageRestrictions = preferences.get(2);

        if(mediaTypes.isEmpty() || genres.isEmpty() || ageRestrictions.isEmpty()){
            JsonHelper.sendResponse(exchange, 200, genres);
            return;
        }

        //determine most frequent genre
        String mostFrequentGenre = getMostFrequent(genres);

        //determine most frequent mediaType
        String mostFrequentMediaType = getMostFrequent(mediaTypes);

        //determine most frequent ageRestriction
        String mostFrequentAgeRestriction = getMostFrequent(ageRestrictions);

        List<String> userPref = new ArrayList<>();
        userPref.add(mostFrequentGenre);
        userPref.add(mostFrequentMediaType);
        userPref.add(mostFrequentAgeRestriction);

        List<Object> recommendations = mediaRepository.getByPreference(userPref);
        JsonHelper.sendResponse(exchange, 200, recommendations);
    }


    public void searchByTitle(HttpExchange exchange) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        String title = tmpValues[tmpValues.length-1];
        List<Object> mediaEntries = mediaRepository.getByTitle(title);

        if(mediaEntries.isEmpty()){
            JsonHelper.sendResponse(exchange, 404, mediaEntries);
            return;
        }

        JsonHelper.sendResponse(exchange, 200, mediaEntries);
    }

    public void sort(HttpExchange exchange, char TitleOrYear) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}

//        String path = exchange.getRequestURI().getPath();
//        String[] tmpValues = path.split("/");
//
//        String title = tmpValues[tmpValues.length-1];
        List<Object> mediaEntries = mediaRepository.sorted(TitleOrYear);

        if(mediaEntries.isEmpty()){
            JsonHelper.sendResponse(exchange, 404, mediaEntries);
            return;
        }

        JsonHelper.sendResponse(exchange, 200, mediaEntries);
    }

//    public void sortByYear(HttpExchange exchange) throws IOException, SQLException {
//        if(authService.validateToken(exchange) == null) {return;}
//
//        String path = exchange.getRequestURI().getPath();
//        String[] tmpValues = path.split("/");
//
//        try{
//            int title = Integer.parseInt(tmpValues[tmpValues.length-1]);
//            List<Object> mediaEntries = mediaRepository.sortedByYear(title);
//
//            if(mediaEntries.isEmpty()){
//                JsonHelper.sendResponse(exchange, 404, mediaEntries);
//                return;
//            }
//
//            JsonHelper.sendResponse(exchange, 200, mediaEntries);
//
//        } catch (IllegalArgumentException exception){
//            JsonHelper.sendError(exchange, 400, "correct input required");
//        }
//    }

    public void sortByScore(HttpExchange exchange) throws IOException, SQLException {
        //TODO
    }
}
