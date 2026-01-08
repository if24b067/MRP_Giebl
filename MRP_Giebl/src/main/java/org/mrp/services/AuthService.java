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

    //function to validate token and return UUID user
    public UUID validateToken(HttpExchange exchange) throws SQLException, IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7); //remove "Bearer "

        UUID user_id = userRepository.chkToken(token);
        if (user_id == null) {
            jsonHelper.sendError(exchange, 401, "invalid token");
            return null;
        }

        return user_id;
    }

    public void register (HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = jsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");

        //validate input
        if(userRepository.chkUsername(username)) {
            jsonHelper.sendError(exchange, 400, "Username already exists");
            return;
        }

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            jsonHelper.sendError(exchange, 400, "Username and password are required");
            return;
        }

        if (username.length() < 3 || username.length() > 50) {
            jsonHelper.sendError(exchange, 400, "Username must be between 3 and 50 characters");
            return;
        }

        if (password.length() < 6) {
            jsonHelper.sendError(exchange, 400, "Password must be at least 6 characters");
            return;
        }

        //hash password
        String pwHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        //createMedia Timestamp
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        User user = new User(username, pwHash, createdAt);
        UUID userId = userRepository.save(user);


        //response
        Map<String, Object> response = new HashMap<>();
        response.put("id", userId);
        response.put("username", username);
        jsonHelper.sendResponse(exchange, 201, response);
    }

    public void login (HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = jsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");


        //validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            jsonHelper.sendError(exchange, 400, "Username and password are required");
            return;
        }

        if(!userRepository.chkLogin(username, password)) {
            jsonHelper.sendError(exchange, 400, "Username or password are invalid");
            return;
        }


        String token = username + "_" + UUID.randomUUID().toString();
        userRepository.saveToken(token, username);

        //response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);

        jsonHelper.sendResponse(exchange, 200, response);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = validateToken(exchange);
        if(user_id==null){return;}

        User user = (User) userRepository.getOne(user_id);

        jsonHelper.sendResponse(exchange, 200, user);
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = validateToken(exchange);
        if(user_id==null){return;}

        InputStream is  = exchange.getRequestBody();
        if(is.available() == 0){
            jsonHelper.sendError(exchange, 400, "request body is empty");
            return;
        }

        Map<String, String> request = jsonHelper.parseRequest(exchange, Map.class);
        String passwordOld = request.get("password_old");
        String username = request.get("username");
        String passwordNew = request.get("password_new");

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

        String pwHash = BCrypt.withDefaults().hashToString(12, passwordNew.toCharArray());

        User user = new User(user_id, username, pwHash);

        userRepository.update(user);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);

        jsonHelper.sendResponse(exchange, 201, response);
    }

    public void delete(HttpExchange exchange) throws IOException, SQLException {
        UUID user_id = validateToken(exchange);
        if(user_id==null){return;}

        User user = (User) userRepository.getOne(user_id);
        userRepository.delete(user_id);
        jsonHelper.sendResponse(exchange, 200, user);
    }
}
