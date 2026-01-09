package org.mrp.services;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.LeaderBoard;
import org.mrp.models.NumValue;
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
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        //chk whether request body is empty
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);

        //get media id and parse to UUID, chk for null
        UUID mediaId = null;
        String media = request.get("mediaId");
        if (media != null) {
            try{
                mediaId = UUID.fromString(media);
            }catch (IllegalArgumentException e){
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
        }

        UUID creator = user_id;

        //chk whether user has already rated entry
        if (ratingRepository.chkUserAndMedia(creator, mediaId)) {
            jsonHelper.sendError(exchange, 400, "already rated this media entry");
            return;
        }

        /*get rest of info if not already rated*/
        //get starValue and parse to int, chk for null
        Integer starValue = null;
        String value = request.get("starValue");
        if (value != null) {
            try {
                starValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
        }

        //get visibility flag for comment and parse to bool, chk for null
        Boolean visFlag = null;
        String vis = request.get("visFlag");
        if (vis != null) {
            if(vis.equalsIgnoreCase("true")) visFlag = true;
            else if(vis.equalsIgnoreCase("false")) visFlag = false;
            else {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
        }

        String comment = request.get("comment");
        if(comment != null) {
            if (comment.length() > 100) {
                jsonHelper.sendError(exchange, 400, "input too long");
                return;
            }
        }   //comment is optional, possible to be null

        Rating rating = new Rating(creator, mediaId, starValue, comment, /*likes,*/ visFlag);

        //call repo function
        UUID ratingId = ratingRepository.save(rating);
        Rating r = (Rating) ratingRepository.getOne(ratingId);

        jsonHelper.sendResponse(exchange, 201, r);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID rating_id = UUID.fromString(tmpValues[tmpValues.length-1]);    //if rating id in uri get specific rating
            Rating rating = (Rating) ratingRepository.getOne(rating_id);

            if(rating == null){
                jsonHelper.sendError(exchange, 404, "Media not found");
                return;
            }

            //chk for comment visibility flag
            if(!rating.getVis()) rating.setComment(null);
            jsonHelper.sendResponse(exchange, 200, rating);

        } catch (IllegalArgumentException exception){
            if(Objects.equals(tmpValues[tmpValues.length - 1], "own")){ //if uri contains own get ratings of user
                List<Object> ratings = ratingRepository.getOwn(user_id);

                if(ratings.isEmpty()){
                    jsonHelper.sendError(exchange, 404, "Media not found");
                    return;
                }

                //no visibility chk for ratings if user is creator
                jsonHelper.sendResponse(exchange, 200, ratings);
                return;
            }
            List<Object> ratings = ratingRepository.getAll();

            //chk comments visibility
            for(Object r : ratings){
                if(r instanceof Rating rating){
                    if(!rating.getVis()) rating.setComment(null);
                }
            }

            jsonHelper.sendResponse(exchange, 200, ratings);
        }
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        //chk whether request has body
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);

        //get rating id from request and parse to UUID, chk for null
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
        //get starValue parse to int, chk for null
        Integer starValue = null;
        String value = request.get("starValue");
        if (value != null) {
            try {
                starValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        //get visibility flag for comment parse to bool, chk for null
        Boolean visFlag = null;
        String vis = request.get("vis");
        if (vis != null) {
            if(vis.equalsIgnoreCase("true")) visFlag = true;
            else if(vis.equalsIgnoreCase("false")) visFlag = false;
            else {
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        String comment = request.get("comment");
        if (comment.length() > 100) {   //chk comment length
            jsonHelper.sendError(exchange, 400, "input too long");
            return;
        }

        Rating rating = new Rating(ratingId, user_id, null, starValue, comment, visFlag);

        //call repo function
        ratingRepository.update(rating);
        Rating r = (Rating) ratingRepository.getOne(ratingId);

        jsonHelper.sendResponse(exchange, 200, r);
    }

    public void delete(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        //get info from exchange
        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");
        UUID rating_id = null;
        try{
            rating_id = UUID.fromString(tmpValues[tmpValues.length-1]); //chk whether UUID in uri
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
        UUID user_id = authService.validateToken(exchange); //chk token validity and get user id
        if(user_id==null){return;}

        //chk whether body is empty
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);

        //get rating id and parse to UUID, chk for null
        UUID rating_id = null;
        String rating = request.get("rating_id");
        if (rating != null) {
            try{
                rating_id = UUID.fromString(rating);
            }catch (IllegalArgumentException e){
                jsonHelper.sendError(exchange, 400, "correct input required");
                return;
            }
        } else {
            jsonHelper.sendError(exchange, 400, "correct input required");
            return;
        }

        //chk whether user has already liked rating
        if (ratingRepository.chkUserAndRating(user_id, rating_id)) {
            jsonHelper.sendError(exchange, 400, "already liked this rating");
            return;
        }

        //call repo function
        ratingRepository.like(user_id, rating_id);
        NumValue likes = new NumValue(ratingRepository.getCntOfLikes(rating_id));

        jsonHelper.sendResponse(exchange, 201, likes);
    }

    public void cntLikes(HttpExchange exchange) throws IOException, SQLException{
        if(authService.validateToken(exchange) == null) {return;}   //chk token validity

        String path = exchange.getRequestURI().getPath();
        String[] tmpValues = path.split("/");

        try{
            UUID rating_id = UUID.fromString(tmpValues[tmpValues.length-1]);    //chk for UUID in uri
            NumValue likesCnt = new NumValue(ratingRepository.getCntOfLikes(rating_id));    //cnt likes for rating

            jsonHelper.sendResponse(exchange, 200, likesCnt);

        } catch (IllegalArgumentException exception){
            jsonHelper.sendError(exchange, 400, "invalid input");
        }
    }

    private void removeElement(List<String> list, String element) {
        //remove specifiy element from list
        int index;
        while ((index = list.indexOf(element)) >= 0) {  //get index of specific element
            list.remove(index);
        }
    }

    public void getLeaderboard(HttpExchange exchange) throws IOException, SQLException{
        if(authService.validateToken(exchange) == null) {return;}   //chk token validity

        List<Object> ratings = ratingRepository.getAll();
        List<String> users = new ArrayList<String>();
        String first = null;
        String second = null;
        String third = null;

        //get all users who have rated
        for(Object r : ratings){
            if(r instanceof Rating rating){
                users.add(rating.getCreator().toString());
            }
        }
        MediaService mediaService = new MediaService();
        first = mediaService.getMostFrequent(users);    //get user with most ratings
        removeElement(users, first);    //remove user with most ratings

        if(!users.isEmpty()) {  //chk list isnt empty after removing elements
            second = mediaService.getMostFrequent(users);   //get user with most ratings (out of list from which first place has been removed)
            removeElement(users, second);   //remove user
        } else {
            //set second and third to null, if only one user has ratings
            LeaderBoard leaderboard = new LeaderBoard(first, null, null);
            jsonHelper.sendResponse(exchange, 200, leaderboard);
            return;
        }

        if(!users.isEmpty()) {  //chk list isnt empty after removing elements
            third = mediaService.getMostFrequent(users);
        } else {
            //set third to null if only two users have ratings
            LeaderBoard leaderboard = new LeaderBoard(first, second, null);
            jsonHelper.sendResponse(exchange, 200, leaderboard);
            return;
        }

        LeaderBoard leaderboard = new LeaderBoard(first, second, third);
        jsonHelper.sendResponse(exchange, 200, leaderboard);
    }
}