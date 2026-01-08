package org.mrp.services;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.Rating;
import org.mrp.repositories.RatingRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class RatingService {
    private RatingRepository ratingRepository;
    private AuthService authService;
    private JsonHelper jsonHelper;

    public RatingService() {
        ratingRepository = new RatingRepository();
        authService = new AuthService();
        jsonHelper = new JsonHelper();
    }

    public RatingService(RatingRepository ratingRepository, AuthService authService, JsonHelper jsonHelper) {
        this.ratingRepository = ratingRepository;
        this.authService = authService;
        this.jsonHelper = jsonHelper;
    }

    public void create(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);

        UUID mediaId = null;
        String media = request.get("mediaId");
        if (media != null) {
            try{
                mediaId = UUID.fromString(media);
            }catch (IllegalArgumentException e){
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        UUID creator = user_id;

        //chk whether user has already rated entry
        if (ratingRepository.chkUserAndMedia(creator, mediaId)) {
            jsonHelper.sendError(exchange, 400, "already rated this media entry");
            return;
        }

        Integer starValue = null;
        String value = request.get("starValue");
        if (value != null) {
            try {
                starValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        String comment = request.get("comment");

        Boolean visFlag = null;
        String vis = request.get("visFlag");
        if (vis != null) {
            if(vis.equalsIgnoreCase("true")) visFlag = true;
            else if(vis.equalsIgnoreCase("false")) visFlag = false;
            else {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        if(comment != null) {
            if (comment.length() > 100) {
                jsonHelper.sendError(exchange, 400, "input too long");
                return;
            }
        }

        Rating rating = new Rating(creator, mediaId, starValue, comment, /*likes,*/ visFlag);

        //call repo function
        UUID ratingId = ratingRepository.save(rating);
        Rating r = (Rating) ratingRepository.getOne(ratingId);

        jsonHelper.sendResponse(exchange, 201, r);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID rating_id = UUID.fromString(tmpValues[tmpValues.length-1]);
            Rating rating = (Rating) ratingRepository.getOne(rating_id);

            if(rating == null){
                jsonHelper.sendError(exchange, 404, "Media not found");
                return;
            }

            if(!rating.getVis()) rating.setComment(null);
            jsonHelper.sendResponse(exchange, 200, rating);

        } catch (IllegalArgumentException exception){
            if(Objects.equals(tmpValues[tmpValues.length - 1], "own")){
                List<Object> ratings = ratingRepository.getOwn(user_id);
                jsonHelper.sendResponse(exchange, 200, ratings);
                return;
            }
            List<Object> ratings = ratingRepository.getAll();

            for(Object r : ratings){
                if(r instanceof Rating rating){
                    if(!rating.getVis()) rating.setComment(null);
                }
            }

            jsonHelper.sendResponse(exchange, 200, ratings);
        }
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);

        UUID ratingId = null;
        String id = request.get("rating_id");
        if (id != null) {
            try{
                ratingId = UUID.fromString(id);
            }catch (IllegalArgumentException e){
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        //chk whether User is creator
        boolean isCreator = ratingRepository.chkCreator(ratingId, user_id);

        if(!isCreator){
            jsonHelper.sendError(exchange, 401, "unauthorized to edit post");
            return;
        }

        //get info from exchange if authorised
        Integer starValue = null;
        String value = request.get("starValue");
        if (value != null) {
            try {
                starValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        String comment = request.get("comment");

        Boolean visFlag = null;
        String vis = request.get("vis");
        if (vis != null) {
            if(vis.equalsIgnoreCase("true")) visFlag = true;
            else if(vis.equalsIgnoreCase("false")) visFlag = false;
            else {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        UUID creator = user_id;

        //validate input
        if (comment == null || comment.trim().isEmpty() ||
            starValue == null || /*likes == null ||*/ visFlag == null) {
            jsonHelper.sendError(exchange, 400, "Correct input required");
            return;
        }

        if (comment.length() > 100) {
            jsonHelper.sendError(exchange, 400, "input too long");
            return;
        }

        UUID mediaId = null;
        Rating rating = new Rating(ratingId, creator, mediaId, starValue, comment/*, likes*/, visFlag);

        //call repo function
        ratingRepository.update(rating);
        Rating r = (Rating) ratingRepository.getOne(ratingId);

        jsonHelper.sendResponse(exchange, 200, r);
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
            jsonHelper.sendError(exchange, 400, "rating Id required");
            return;
        }

        //chk whether entry exists
        Rating rating = (Rating) ratingRepository.getOne(rating_id);

        if(rating == null) {
            jsonHelper.sendError(exchange, 404, "Rating not found");
            return;
        }

        //chk whether User is creator
        boolean isCreator = ratingRepository.chkCreator(rating_id, user_id);

        if(!isCreator){
            jsonHelper.sendError(exchange, 401, "unauthorized to delete post");
            return;
        }

        ratingRepository.delete(rating_id);
        jsonHelper.sendResponse(exchange, 200, rating);
    }

    public void like (HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange);
        if(user_id==null){return;}

        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);

        UUID rating_id = null;
        String rating = request.get("rating_id");
        if (rating != null) {
            try{
                rating_id = UUID.fromString(rating);
            }catch (IllegalArgumentException e){
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        }

        //chk whether user has already liked rating
        if (ratingRepository.chkUserAndRating(user_id, rating_id)) {
            jsonHelper.sendError(exchange, 400, "already liked this rating");
            return;
        }

        //call repo function
        ratingRepository.like(user_id, rating_id);
        int likes = ratingRepository.getCntOfLikes(rating_id);

        //TODO correct JSON response
        jsonHelper.sendResponse(exchange, 201, likes);
    }

    public void cntLikes(HttpExchange exchange) throws IOException, SQLException{
        if(authService.validateToken(exchange) == null) {return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID rating_id = UUID.fromString(tmpValues[tmpValues.length-1]);
            int likesCnt = ratingRepository.getCntOfLikes(rating_id);

            //TODO correct JSON response
            jsonHelper.sendResponse(exchange, 200, likesCnt);

        } catch (IllegalArgumentException exception){
            jsonHelper.sendError(exchange, 400, "invalid input");
        }
    }
}