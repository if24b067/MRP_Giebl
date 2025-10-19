package org.mrp.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.User;
import org.mrp.repositories.UserRepository;
import org.mrp.utils.JsonHelper;
import org.mrp.utils.UUIDv7Generator;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AuthService {
    private UserRepository userRepository;

    public AuthService() {
        userRepository = new UserRepository();
    }

    //function to validate token and return UUID user
    public UUID validateToken(HttpExchange exchange) throws SQLException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        return userRepository.chkToken(token);
    }

    public void register (HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = JsonHelper.parseRequest(exchange, HashMap.class);
        String username = request.get("username");
        String password = request.get("password");

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
        //createMedia Timestamp
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        //tmp hilfswert UUID
        UUIDv7Generator uuidv7Generator = new UUIDv7Generator();
        UUID userId = uuidv7Generator.randomUUID();

        User user = new User(userId, username, pwHash, createdAt);
        userRepository.save(user);


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
        userRepository.saveToken(token, username);

        //response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "User logged in successfully");

        JsonHelper.sendResponse(exchange, 200, response);
    }

    public void read(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint not yet further developed");

        JsonHelper.sendResponse(exchange, 418, response);
    }

    public void update(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint not yet further developed");

        JsonHelper.sendResponse(exchange, 418, response);
    }

    public void delete(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint not yet further developed");

        JsonHelper.sendResponse(exchange, 418, response);
    }
}
