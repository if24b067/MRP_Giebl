package org.mrp.services;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaService {

    public MediaService() {}

    public void create (HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint not yet further developed");

        JsonHelper.sendResponse(exchange, 418, response);
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
