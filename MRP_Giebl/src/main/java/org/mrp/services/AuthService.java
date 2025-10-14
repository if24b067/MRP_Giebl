package org.mrp.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.User;
import org.mrp.repositories.UserRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {

    public AuthService() {}

    public void register (HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = JsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");
        UserRepository userRepository = new UserRepository();

        //validate input
        if(userRepository.chkUsername(username)) {
            JsonHelper.sendError(exchange, 400, "Username already exists");
            return;
        }

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Username and password are required");
            return;
        }

        if (username.length() < 3 || username.length() > 50) {
            JsonHelper.sendError(exchange, 400, "Username must be between 3 and 50 characters");
            return;
        }

        if (password.length() < 6) {
            JsonHelper.sendError(exchange, 400, "Password must be at least 6 characters");
            return;
        }

        //hash password
        String pwHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        //create Timestamp
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        //tmp hilfswert UUID
        String userId = "0189e8c6-6b1b-7def-b95b-6f2b8cdffd5a";

        User user = new User(userId, username, pwHash, createdAt);
        userRepository.saveUser(user);


        //response
        Map<String, Object> response = new HashMap<>();
        response.put("id", userId);
        response.put("username", username);
        response.put("message", "User registered successfully");

        JsonHelper.sendResponse(exchange, 201, response);
    }

    public void login (HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = JsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");
        UserRepository userRepository = new UserRepository();


        //validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Username and password are required");
            return;
        }

        if(!userRepository.chkLogin(username, password)) {
            JsonHelper.sendError(exchange, 400, "Username or password are invalid");
            return;
        }


        String token = username + "_" + UUID.randomUUID().toString();

        //response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "User logged in successfully");

        JsonHelper.sendResponse(exchange, 201, response);
    }
}
