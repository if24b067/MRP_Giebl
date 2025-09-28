package org.mrp;

import com.sun.net.httpserver.HttpServer;
import org.mrp.handlers.AuthHandler;
import org.mrp.handlers.MediaHandler;
import org.mrp.handlers.TestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) throws IOException {
        // Server auf Port 8000 erstellen
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Handler für verschiedene Pfade registrieren
        server.createContext("/", new TestHandler());
        server.createContext("/api/auth", new AuthHandler());
        //server.createContext("/api/users/login", new LoginHandler());
        server.createContext("/api/media", new MediaHandler());

        // Executor setzen (null = Standard-Executor)
        server.setExecutor(null);

        // Server starten
        server.start();
        System.out.println("Server läuft auf http://localhost:8000");
    }
}