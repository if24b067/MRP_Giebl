package org.mrp.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.User;
import org.mrp.repositories.UserRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;


public class AuthService {
    private UserRepository userRepository;
    private JsonHelper jsonHelper;

    public AuthService() {
        jsonHelper = new JsonHelper();
        userRepository = new UserRepository();
    }

    //function to validate token and return UUID of user
    public UUID validateToken(HttpExchange exchange) throws SQLException, IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization"); //get auth header

        //validate auth header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7); //remove "Bearer "

        //chk whether token validity
        UUID user_id = userRepository.chkToken(token);
        if (user_id == null) {
            jsonHelper.sendError(exchange, 401, "invalid token");
            return null;
        }

        return user_id;
    }

    public void register (HttpExchange exchange) throws IOException, SQLException {
        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");

        /* validate input */

        //chk whether username already exists
        if(userRepository.chkUsername(username)) {
            jsonHelper.sendError(exchange, 400, "Username already exists");
            return;
        }

        //chk whether required input was sent
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            jsonHelper.sendError(exchange, 400, "Username and password are required");
            return;
        }

        //chk username length
        if (username.length() < 3 || username.length() > 50) {
            jsonHelper.sendError(exchange, 400, "Username must be between 3 and 50 characters");
            return;
        }

        //chk password length
        if (password.length() < 6) {
            jsonHelper.sendError(exchange, 400, "Password must be at least 6 characters");
            return;
        }

        //hash password
        String pwHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        User user = new User(username, pwHash);
        UUID userId = userRepository.save(user);


        //response
        Map<String, Object> response = new HashMap<>();
        response.put("id", userId);
        response.put("username", username);
        jsonHelper.sendResponse(exchange, 201, response);
    }

    public void login (HttpExchange exchange) throws IOException, SQLException {
        //get info from exchange
        Map<String, String> request = jsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");


        /* validate input */

        //ensure required input was sent
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            jsonHelper.sendError(exchange, 400, "Username and password are required");
            return;
        }

        //chk whether user exists and username and password match
        if(!userRepository.chkLogin(username, password)) {
            jsonHelper.sendError(exchange, 400, "Username or password are invalid");
            return;
        }

        //generate and save token
        String token = username + "_" + UUID.randomUUID();
        userRepository.saveToken(token, username);

        //send token in response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);

        jsonHelper.sendResponse(exchange, 200, response);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = validateToken(exchange); //chk validity of token and get user id
        if(user_id==null){return;}

        User user = (User) userRepository.getOne(user_id);

        jsonHelper.sendResponse(exchange, 200, user);
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = validateToken(exchange); //chk validity of token and get user id
        if(user_id==null){return;}

        //chk whether request body is empty
        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        //get info from request
        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);
        String passwordOld = request.get("password_old");
        String username = request.get("username");
        String passwordNew = request.get("password_new");

        //chk whether required input was sent
        if(username == null || username.trim().isEmpty() ||
        passwordNew == null || passwordNew.trim().isEmpty() ||
        passwordOld == null || passwordOld.trim().isEmpty()){
            jsonHelper.sendError(exchange, 400, "valid input required");
            return;
        }

        //chk whether old password matches
        boolean correctPW = userRepository.chkPW(passwordOld, user_id);

        if(!correctPW){
            jsonHelper.sendError(exchange, 401, "unauthorized to edit user");
            return;
        }

        //hash new password
        String pwHash = BCrypt.withDefaults().hashToString(12, passwordNew.toCharArray());

        User user = new User(user_id, username, pwHash);
        userRepository.update(user);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);

        jsonHelper.sendResponse(exchange, 201, response);
    }

    public void delete(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = validateToken(exchange); //chk validity of token and get user id
        if(user_id==null){return;}

        User user = (User) userRepository.getOne(user_id);
        userRepository.delete(user_id);
        jsonHelper.sendResponse(exchange, 200, user);
    }
}
