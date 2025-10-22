package org.mrp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.services.MediaService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;

public class MediaHandler implements HttpHandler {
    private MediaService mediaService;

    public MediaHandler() {
        this.mediaService = new MediaService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method)) {
                mediaService.createMedia(exchange);
            } else if ("PUT".equals(method)) {
                    mediaService.update(exchange);
            } else if ("GET".equals(method)) {
                mediaService.read(exchange);
            } else if ("DELETE".equals(method)) {
                mediaService.delete(exchange);
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
