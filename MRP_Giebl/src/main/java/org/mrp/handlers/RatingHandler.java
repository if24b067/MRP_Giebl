package org.mrp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.services.RatingService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;

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
            if (path.endsWith("like") && "POST".equals(method)) {
                ratingService.like(exchange);
            } else if ("POST".equals(method)) {
                ratingService.create(exchange);
            } else if (path.contains("/likes") && "GET".equals(method)) {
                    ratingService.cntLikes(exchange);
            } else if ("GET".equals(method)) {
                ratingService.read(exchange);
            } else if ("PUT".equals(method)) {
                ratingService.update(exchange);
            } else if ("DELETE".equals(method)) {
                ratingService.delete(exchange);
            } else {
                JsonHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JsonHelper.sendError(exchange, 500, "error in Database");
        } catch (IOException e) {
            e.printStackTrace();
            JsonHelper.sendError(exchange, 400, "unexpected input");
        } catch (Exception e) {
            e.printStackTrace();
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }


}
