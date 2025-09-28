package org.mrp.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.models.User;
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

        //validate input
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

        //check if username already exists
//        if (db.exists("SELECT 1 FROM users WHERE username = ?", username)) {
//            JsonHelper.sendError(exchange, 400, "Username already exists");
//            return;
//        }

        //hash password
        String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        //insert user with UUID
//        UUID userId = db.insert(
//                "INSERT INTO users (id, username, password_hash) VALUES (?, ?, ?)",
//                username, passwordHash
//        );

        String userId = "0189e8c6-6b1b-7def-b95b-6f2b8cdffd5a";

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


        //get username and pw from db to check input
        //for now hardcoded test input
        String testName = "max";
        String testPw = "1234";

        //validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "Username and password are required");
            return;
        }

        if (!username.equals(testName) || !password.equals(testPw)) {
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
