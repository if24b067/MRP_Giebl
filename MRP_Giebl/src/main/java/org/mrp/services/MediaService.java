package org.mrp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.MediaEntry;
import org.mrp.repositories.MediaRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class MediaService {
    MediaRepository mediaRepository = new MediaRepository();

    public MediaService() {
        mediaRepository = new MediaRepository();
    }


    public void createMedia(HttpExchange exchange) throws IOException, SQLException {
        //TODO put validate token in other class
        AuthService authService = new AuthService();
        UUID user_id = authService.validateToken(exchange);

        if (user_id == null) {
            JsonHelper.sendError(exchange, 401, "invalid token");
            return;
        }

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
        System.out.println("genres" + genres);

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

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Media Entry created");

        JsonHelper.sendResponse(exchange, 201, response);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {
        AuthService authService = new AuthService();
        UUID user_id = authService.validateToken(exchange);

        if (user_id == null) {
            JsonHelper.sendError(exchange, 401, "invalid token");
            return;
        }

        List<MediaEntry> mediaEntries = mediaRepository.get();

        Map<UUID, Object> response = mediaEntries.stream().collect(Collectors.toMap(MediaEntry::getId, Function.identity()));

        JsonHelper.sendResponse(exchange, 200, response);
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        AuthService authService = new AuthService();
        UUID user_id = authService.validateToken(exchange);

        if (user_id == null) {
            JsonHelper.sendError(exchange, 401, "invalid token");
            return;
        }

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


        MediaEntry mediaEntry = new MediaEntry(title, desc, user_id, releaseYear, ageRestriction, genres);

        //call repo function
        mediaRepository.update(mediaEntry);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Media Entry updated");

        JsonHelper.sendResponse(exchange, 201, response);
    }

    public void delete(HttpExchange exchange) throws IOException, SQLException {
        AuthService authService = new AuthService();
        UUID user_id = authService.validateToken(exchange);

        if (user_id == null) {
            JsonHelper.sendError(exchange, 401, "invalid token");
            return;
        }

        //get info from exchange
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            JsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }
        Map<String, String> request = JsonHelper.parseRequest(exchange, Map.class);
        String media_id = request.get("media_id");

        if(media_id == null || media_id.isEmpty()){
            JsonHelper.sendError(exchange, 400, "media Id required");
            return;
        }


        //chk whether User is creator
        boolean isCreator = mediaRepository.chkCreator(UUID.fromString(media_id), user_id);

        if(!isCreator){
            JsonHelper.sendError(exchange, 401, "unauthorized to delete post");
            return;
        }

        //call repo function
        mediaRepository.delete(UUID.fromString(media_id));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Media Entry deleted");

        JsonHelper.sendResponse(exchange, 200, response);
    }
}
