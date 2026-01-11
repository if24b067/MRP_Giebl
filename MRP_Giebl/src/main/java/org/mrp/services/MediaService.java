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
    private JsonHelper jsonHelper;

    public MediaService() {
        mediaRepository = new MediaRepository();
        authService = new AuthService();
        jsonHelper = new JsonHelper();
    }


    public void createMedia(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get creator id
        if(user_id==null){return;}

        //chk if response has body
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);
        String title = request.get("title");
        String desc = request.get("desc");
        String type = request.get("type");

        //get year from exchange and parse to int, chk for null
        Integer releaseYear = null;
        String releaseYearStr = request.get("releaseYear");
        if (releaseYearStr != null) {
            try {
                releaseYear = Integer.parseInt(releaseYearStr);
            } catch (NumberFormatException e) {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        //get age res from exchange and parse to int, chk for null
        Integer ageRestriction = null;
        String ageRestrictionStr = request.get("ageRestriction");
        if (ageRestrictionStr != null) {
            try {
                ageRestriction = Integer.parseInt(ageRestrictionStr);
            } catch (NumberFormatException e) {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        //get genres from exchange and parse to list csv, chk for nul
        List<String> genres = null;
        String genresStr = request.get("genres");
        if (genresStr != null && !genresStr.trim().isEmpty()) {
            genres = Arrays.asList(genresStr.split(","));
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }


        UUID creator = user_id;

        //validate input
        if (title == null || title.trim().isEmpty() ||
                desc == null || desc.trim().isEmpty() ||
                type == null || type.trim().isEmpty() ||
                (!type.equals("MOVIE") && !type.equals("SERIES") && !type.equals("GAME"))) {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        if (title.length() > 30 || desc.length() > 100 || genres.size() > 10) {
            jsonHelper.sendError(exchange, 400, "input too long");
            return;
        }


        MediaEntry mediaEntry = new MediaEntry(title, desc, user_id, releaseYear, ageRestriction, genres, type);

        //call repo function
        UUID mediaId = mediaRepository.save(mediaEntry);
        MediaEntry entry = (MediaEntry) mediaRepository.getOne(mediaId);

        jsonHelper.sendResponse(exchange, 201, entry);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}   //chk token validity

        //get values from uri
        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID media_id = UUID.fromString(tmpValues[tmpValues.length-1]); //chk for mediaId in uri
            MediaEntry mediaEntry = (MediaEntry) mediaRepository.getOne(media_id);  //if found get specific entry

            if(mediaEntry == null){
                jsonHelper.sendError(exchange, 404, "Media not found");
                return;
            }

            jsonHelper.sendResponse(exchange, 200, mediaEntry);

        } catch (IllegalArgumentException exception){   //if no media id in uri get all entries
            List<Object> response = mediaRepository.getAll();

            jsonHelper.sendResponse(exchange, 200, response);
        }
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        //chk if response has body
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);
        UUID media_id = UUID.fromString(request.get("media_id"));

        //chk whether User is creator
        boolean isCreator = mediaRepository.chkCreator(media_id, user_id);

        if(!isCreator){
            jsonHelper.sendError(exchange, 401, "unauthorized to edit post");
            return;
        }

        //get rest of info if authorized
        String title = request.get("title");
        String desc = request.get("desc");
        String type = request.get("type");


        Integer releaseYear = null; //get year and parse to int, chk for null
        String releaseYearStr = request.get("releaseYear");
        if (releaseYearStr != null) {
            try {
                releaseYear = Integer.parseInt(releaseYearStr);
            } catch (NumberFormatException e) {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        Integer ageRestriction = null;  //get age restriction and parse to int, chk for null
        String ageRestrictionStr = request.get("ageRestriction");
        if (ageRestrictionStr != null) {
            try {
                ageRestriction = Integer.parseInt(ageRestrictionStr);
            } catch (NumberFormatException e) {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        List<String> genres = null; //get genres and parse to list, chk for null
        String genresStr = request.get("genres");
        if (genresStr != null && !genresStr.trim().isEmpty()) {
            genres = Arrays.asList(genresStr.split(","));
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        //chk for null and empty values
        if (title == null || title.trim().isEmpty() ||
                desc == null || desc.trim().isEmpty() ||
                type == null || type.trim().isEmpty() ||
                (!type.equals("MOVIE") && !type.equals("SERIES") && !type.equals("GAME"))) {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }
        if (title.length() > 30 || desc.length() > 100 || genres.size() > 10) {
            jsonHelper.sendError(exchange, 400, "input too long");
            return;
        }

        MediaEntry mediaEntry = new MediaEntry(media_id, title, desc, user_id, releaseYear, ageRestriction, genres, type);

        //call repo function
        mediaRepository.update(mediaEntry);

        jsonHelper.sendResponse(exchange, 201, mediaEntry);
    }

    public void delete(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        //get info from exchange
        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");
        UUID media_id = null;
        try{
            media_id = UUID.fromString(tmpValues[tmpValues.length-1]);  //chk for media id in uri
        } catch (IllegalArgumentException exception){
            jsonHelper.sendError(exchange, 400, "media Id required");
            return;
        }

        //chk whether entry exists
        MediaEntry mediaEntry = (MediaEntry) mediaRepository.getOne(media_id);

        if(mediaEntry == null) {
            jsonHelper.sendError(exchange, 404, "Media entry not found");
            return;
        }

        //chk whether User is creator
        boolean isCreator = mediaRepository.chkCreator(media_id, user_id);

        if(!isCreator){
            jsonHelper.sendError(exchange, 401, "unauthorized to delete post");
            return;
        }

        mediaRepository.delete(media_id);
        jsonHelper.sendResponse(exchange, 200, mediaEntry);
    }

    public void getAvgRating(HttpExchange exchange) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}   //chk token validity

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID media_id = UUID.fromString(tmpValues[tmpValues.length-1]); //chk for mediaId in uri
            RatingRepository ratingRepository = new RatingRepository();
            List<Object> response = ratingRepository.getAllOfMedia(media_id);   //get all ratings of mediaentry

            if(!response.isEmpty()){    //chk whether media entry has ratings
                //calculate avg rating score of media entry
                float sum = 0;
                for(Object o : response){
                    if(o instanceof Rating){
                        Rating rating = (Rating) o;
                        sum += rating.getStarValue();
                    }
                }
                Map<String, Object> resp = new HashMap<>();
                resp.put("avgScore", sum/response.size());
                //NumValue avgScore = new NumValue(sum/response.size());

                jsonHelper.sendResponse(exchange, 200, resp);
                return;
            }

            jsonHelper.sendError(exchange, 404, "no ratings found");


        } catch (IllegalArgumentException exception){
            jsonHelper.sendError(exchange, 400, "correct input required");
        }
    }

    public void getAllRatings(HttpExchange exchange) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}   //chk token validity

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID media_id = UUID.fromString(tmpValues[tmpValues.length-1]); //get media id from uri
            RatingRepository ratingRepository = new RatingRepository();
            List<Object> ratings = ratingRepository.getAllOfMedia(media_id);

            if(!ratings.isEmpty()){
                //chk flag for visibility of comment
                for(Object r : ratings){
                    if(r instanceof Rating rating){
                        if(!rating.getVis()) rating.setComment(null);   //set comment to null if flag false
                    }
                }

                jsonHelper.sendResponse(exchange, 200, ratings);
                return;
            }

            jsonHelper.sendError(exchange, 404, "no ratings found");

        } catch (IllegalArgumentException exception){
            jsonHelper.sendError(exchange, 400, "correct input required");
        }
    }

    public void addFavourite(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        //chk if response has body
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);

        UUID media_id = null;
        String id = request.get("media_id");
        if (id != null) {
            try{
                media_id = UUID.fromString(id);
            }catch (IllegalArgumentException e){
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        //chk whether entry exists
        if(!mediaRepository.chkEntry(media_id)) {
            jsonHelper.sendError(exchange, 404, "media entry not found");
            return;
        }

        //chk whether entry has already been favourited
        if(mediaRepository.chkFav(media_id, user_id)) {
            jsonHelper.sendError(exchange, 400, "entry already marked as favourite");
            return;
        }

        //call repo function
        mediaRepository.saveFav(user_id, media_id);
        List<Object> favourites = mediaRepository.getFav(user_id);

        jsonHelper.sendResponse(exchange, 201, favourites);
    }

    public void remFavourite(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID media_id = UUID.fromString(tmpValues[tmpValues.length-1]); //get media id from uri
            if(!mediaRepository.chkFav(media_id, user_id)) {    //chk whether fav exists
                jsonHelper.sendError(exchange, 404, "entry not found");
                return;
            }

            mediaRepository.delFav(user_id, media_id);
            List<Object> favourites = mediaRepository.getFav(user_id);

            jsonHelper.sendResponse(exchange, 200, favourites);

        } catch (IllegalArgumentException exception){
            jsonHelper.sendError(exchange, 400, "correct input required");
        }
    }

    public void readFav(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        List<Object> favourites = mediaRepository.getFav(user_id);

        jsonHelper.sendResponse(exchange, 200, favourites);

    }

    public String getMostFrequent(List<String> attribute) { //get most frequent attribute in a list
        Map<String, Integer> genreCount = new HashMap<>();
        for (String a : attribute) {    //chk whether attribute already in map, return cnt and increment by 1
            genreCount.put(a, genreCount.getOrDefault(a, 0) + 1);
        }

        String mostFrequent = "";
        int maxCount = 0;

        //get attribute with highest count
        for (Map.Entry<String, Integer> entry : genreCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }

        return mostFrequent;
    }

    public void getRecommendations(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        //get list with lists of genres, types, ageRestr the user as rated
        List<List<String>> preferences = mediaRepository.getUserPreferences(user_id);
        List<String> genres = preferences.get(0);
        List<String> mediaTypes = preferences.get(1);
        List<String> ageRestrictions = preferences.get(2);

        //chk whether lists are empty
        if(mediaTypes.isEmpty() || genres.isEmpty() || ageRestrictions.isEmpty()){
            jsonHelper.sendError(exchange, 404, "no basis for recommendations found");
            return;
        }

        //determine most frequent genre
        String mostFrequentGenre = getMostFrequent(genres);

        //determine most frequent mediaType
        String mostFrequentMediaType = getMostFrequent(mediaTypes);

        //determine most frequent ageRestriction
        String mostFrequentAgeRestriction = getMostFrequent(ageRestrictions);

        //make list of user preferences
        List<String> userPref = new ArrayList<>();
        userPref.add(mostFrequentGenre);
        userPref.add(mostFrequentMediaType);
        userPref.add(mostFrequentAgeRestriction);

        //get entries that match user preferences
        List<Object> recommendations = mediaRepository.getByPreference(userPref);
        jsonHelper.sendResponse(exchange, 200, recommendations);
    }


    public void searchByTitle(HttpExchange exchange) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}   //chk token validity

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        String title = tmpValues[tmpValues.length-1];   //get title from uri
        List<Object> mediaEntries = mediaRepository.getByTitle(title);

        //chk for empty list
        if(mediaEntries.isEmpty()){
            jsonHelper.sendResponse(exchange, 404, mediaEntries);
            return;
        }

        jsonHelper.sendResponse(exchange, 200, mediaEntries);
    }

    public void sort(HttpExchange exchange, char TitleOrYear) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}   //chk token validity

        //call repo function with indicator whether to sort by title or by year
        List<Object> mediaEntries = mediaRepository.sorted(TitleOrYear);

        //chk for empty list
        if(mediaEntries.isEmpty()){
            jsonHelper.sendResponse(exchange, 404, mediaEntries);
            return;
        }

        jsonHelper.sendResponse(exchange, 200, mediaEntries);
    }
}
