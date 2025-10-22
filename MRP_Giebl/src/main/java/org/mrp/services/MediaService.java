package org.mrp.services;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.MediaEntry;
import org.mrp.repositories.MediaRepository;
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
                desc == null || desc.trim().isEmpty() /*||
                releaseYear == null || ageRestriction == null ||
                genres.isEmpty()*/) {
            JsonHelper.sendError(exchange, 400, "Correct input required");
            return;
        }

        if (title.length() > 30 || desc.length() > 100 || genres.size() > 10) {
            JsonHelper.sendError(exchange, 400, "input too long");
            return;
        }


        MediaEntry mediaEntry = new MediaEntry(title, desc, user_id, releaseYear, ageRestriction, genres);

        //call repo function
        mediaRepository.save(mediaEntry);

        JsonHelper.sendResponse(exchange, 201, mediaEntry);
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

        MediaEntry mediaEntry = new MediaEntry(media_id, title, desc, user_id, releaseYear, ageRestriction, genres);

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
}
