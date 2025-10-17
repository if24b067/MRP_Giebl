package org.mrp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.services.AuthService;
import org.mrp.services.MediaService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;

public class MediaHandler implements HttpHandler {
    private MediaService mediaService;

    public MediaHandler() {
        this.mediaService = new MediaService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        MediaService mediaService = new MediaService();

        try {
            if (path.endsWith("/create") && "POST".equals(method)) {
                mediaService.create(exchange);
            } else if (path.endsWith("/update") && "PUT".equals(method)) {
                    mediaService.update(exchange);
            } else if (path.endsWith("/read") && "GET".equals(method)) {
                mediaService.read(exchange);
            } else if (path.endsWith("/delete") && "DELETE".equals(method)) {
                mediaService.delete(exchange);
            } else {
                JsonHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonHelper.sendError(exchange, 500, "Internal server error");
        }
    }
}
