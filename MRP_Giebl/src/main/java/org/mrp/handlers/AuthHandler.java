package org.mrp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mrp.services.AuthService;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;


public class AuthHandler implements HttpHandler {
    private AuthService authService;

    public AuthHandler() {
        this.authService = new AuthService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.endsWith("/register") && "POST".equals(method)) {
                authService.register(exchange);
            } else if (path.endsWith("/login") && "POST".equals(method)) {
                authService.login(exchange);
            } else if ("GET".equals(method)) {
                authService.read(exchange);
            } else if ("PUT".equals(method)) {
                authService.update(exchange);
            } else if ("DELETE".equals(method)) {
                authService.delete(exchange);
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


