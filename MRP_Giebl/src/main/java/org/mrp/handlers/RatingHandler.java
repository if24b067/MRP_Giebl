package org.mrp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.services.AuthService;
import org.mrp.services.RatingService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;

public class RatingHandler implements HttpHandler {
    private RatingService ratingService;

    public RatingHandler() {
        this.ratingService = new RatingService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.endsWith("/create") && "POST".equals(method)) {
                ratingService.create(exchange);
            } else if (path.endsWith("/read") && "GET".equals(method)) {
                ratingService.read(exchange);
            } else if (path.endsWith("/update") && "PUT".equals(method)) {
                ratingService.update(exchange);
            } else if (path.endsWith("/delete") && "DELETE".equals(method)) {
                ratingService.delete(exchange);
            } else {
                JsonHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }


}
