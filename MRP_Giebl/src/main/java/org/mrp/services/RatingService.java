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
import java.sql.Timestamp;
import java.util.*;

public class RatingService {
    private RatingRepository ratingRepository;
    private AuthService authService;

    public RatingService() {
        ratingRepository = new RatingRepository();
        authService = new AuthService();
    }

    public void create(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            JsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = JsonHelper.parseRequest(exchange, Map.class);

        UUID mediaId = null;
        String media = request.get("mediaId");
        if (media != null) {
            try{
                mediaId = UUID.fromString(media);
            }catch (IllegalArgumentException e){
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        UUID creator = user_id;

        //chk whether user has already rated entry
        if (ratingRepository.chkUserAndMedia(creator, mediaId)) {
            JsonHelper.sendError(exchange, 400, "already rated this media entry");
            return;
        }

        Integer starValue = null;
        String value = request.get("starValue");
        if (value != null) {
            try {
                starValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        String comment = request.get("comment");

        Integer likes = null;
        String like = request.get("likes");
        if (like != null) {
            try {
                likes = Integer.parseInt(like);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        Boolean visFlag = null;
        String vis = request.get("visFlag");
        if (vis != null) {
            if(vis.equalsIgnoreCase("true")) visFlag = true;
            else if(vis.equalsIgnoreCase("false")) visFlag = false;
            else {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        if(comment != null) {
            if (comment.length() > 100) {
                JsonHelper.sendError(exchange, 400, "input too long");
                return;
            }
        }

        Rating rating = new Rating(creator, mediaId, starValue, comment, likes, visFlag);

        //call repo function
        UUID ratingId = ratingRepository.save(rating);
        Rating r = (Rating) ratingRepository.getOne(ratingId);

        JsonHelper.sendResponse(exchange, 201, r);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {
        if(authService.validateToken(exchange) == null) {return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID rating_id = UUID.fromString(tmpValues[tmpValues.length-1]);
            Rating rating = (Rating) ratingRepository.getOne(rating_id);

            if(rating == null){
                JsonHelper.sendError(exchange, 404, "Media not found");
                return;
            }

            if(!rating.getVis()) rating.setComment(null);
            JsonHelper.sendResponse(exchange, 200, rating);

        } catch (IllegalArgumentException exception){
            List<Object> ratings = ratingRepository.getAll();

            for(Object r : ratings){
                if(r instanceof Rating rating){
                    if(!rating.getVis()) rating.setComment(null);
                }
            }

            JsonHelper.sendResponse(exchange, 200, ratings);
        }
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            JsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        Map<String, String> request = JsonHelper.parseRequest(exchange, Map.class);

        UUID ratingId = null;
        String id = request.get("rating_id");
        if (id != null) {
            try{
                ratingId = UUID.fromString(id);
            }catch (IllegalArgumentException e){
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            JsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        //chk whether User is creator
        boolean isCreator = ratingRepository.chkCreator(ratingId, user_id);

        if(!isCreator){
            JsonHelper.sendError(exchange, 401, "unauthorized to edit post");
            return;
        }

        //get info from exchange if authorised

        Integer starValue = null;
        String value = request.get("starValue");
        if (value != null) {
            try {
                starValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        String comment = request.get("comment");

        Integer likes = null;
        String like = request.get("likes");
        if (like != null) {
            try {
                likes = Integer.parseInt(like);
            } catch (NumberFormatException e) {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        Boolean visFlag = null;
        String vis = request.get("vis");
        if (vis != null) {
            if(vis.equalsIgnoreCase("true")) visFlag = true;
            else if(vis.equalsIgnoreCase("false")) visFlag = false;
            else {
                JsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        UUID creator = user_id;

        //validate input
        if (comment == null || comment.trim().isEmpty() ||
            starValue == null || likes == null || visFlag == null) {
            JsonHelper.sendError(exchange, 400, "Correct input required");
            return;
        }

        if (comment.length() > 100) {
            JsonHelper.sendError(exchange, 400, "input too long");
            return;
        }

        UUID mediaId = null;
        Rating rating = new Rating(ratingId, creator, mediaId, starValue, comment, likes, visFlag);

        //call repo function
        ratingRepository.update(rating);
        Rating r = (Rating) ratingRepository.getOne(ratingId);

        JsonHelper.sendResponse(exchange, 200, r);
    }

    public void delete(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        //get info from exchange
        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");
        UUID rating_id = null;
        try{
            rating_id = UUID.fromString(tmpValues[tmpValues.length-1]);
        } catch (IllegalArgumentException exception){
            JsonHelper.sendError(exchange, 400, "rating Id required");
            return;
        }

        //chk whether entry exists
        Rating rating = (Rating) ratingRepository.getOne(rating_id);

        if(rating == null) {
            JsonHelper.sendError(exchange, 404, "Rating not found");
            return;
        }

        //chk whether User is creator
        boolean isCreator = ratingRepository.chkCreator(rating_id, user_id);

        if(!isCreator){
            JsonHelper.sendError(exchange, 401, "unauthorized to delete post");
            return;
        }

        ratingRepository.delete(rating_id);
        JsonHelper.sendResponse(exchange, 200, rating);
    }
}