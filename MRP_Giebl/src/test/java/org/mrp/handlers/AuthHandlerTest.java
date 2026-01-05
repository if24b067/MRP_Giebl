package org.mrp.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.services.AuthService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class AuthHandlerTest {

    private AuthHandler authHandler;
    private HttpExchange exchange;
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = Mockito.mock(AuthService.class);  //mock authService
        authHandler = new AuthHandler(authService); //pass mocked service
        exchange = Mockito.mock(HttpExchange.class);    //mock HTTP exchange
    }

    @Test   //test register handling
    public void testHandleRegister() throws IOException, SQLException, URISyntaxException {
        URI uri = new URI("/register");
        when(exchange.getRequestURI()).thenReturn(uri);
        when(exchange.getRequestMethod()).thenReturn("POST");

        authHandler.handle(exchange);

        verify(authService).register(exchange);     //chk whether correct function called
    }

    @Test   //test login handling
    public void testHandleLogin() throws IOException, SQLException, URISyntaxException {
        URI uri = new URI("/login");
        when(exchange.getRequestURI()).thenReturn(uri);
        when(exchange.getRequestMethod()).thenReturn("POST");

        authHandler.handle(exchange);

        verify(authService).login(exchange);    //chk whether correct function called
    }

    @Test   //test read handling
    public void testHandleRead() throws IOException, SQLException, URISyntaxException {
        URI uri = new URI("/somePath");
        when(exchange.getRequestURI()).thenReturn(uri);
        when(exchange.getRequestMethod()).thenReturn("GET");

        authHandler.handle(exchange);

        verify(authService).read(exchange);
    }

    @Test   //test update handling
    public void testHandleUpdate() throws IOException, SQLException, URISyntaxException {
        URI uri = new URI("/somePath");
        when(exchange.getRequestURI()).thenReturn(uri);
        when(exchange.getRequestMethod()).thenReturn("PUT");

        authHandler.handle(exchange);

        verify(authService).update(exchange);
    }

    @Test   //test delete handling
    public void testHandleDelete() throws IOException, SQLException, URISyntaxException {
        URI uri = new URI("/somePath");
        when(exchange.getRequestURI()).thenReturn(uri);
        when(exchange.getRequestMethod()).thenReturn("DELETE");

        authHandler.handle(exchange);

        verify(authService).delete(exchange);
    }

//    @Test   //test handling of SQL Exceptions
//    public void testHandleSQLException() throws IOException, SQLException, URISyntaxException {
//        URI uri = new URI("/register");
//        when(exchange.getRequestURI()).thenReturn(uri);
//        when(exchange.getRequestMethod()).thenReturn("POST");
//
//        Headers headers = new Headers();    //mock response headers
//        when(exchange.getResponseHeaders()).thenReturn(headers);
//
//        doThrow(new SQLException()).when(authService).register(exchange);
//
//        authHandler.handle(exchange);
//
//        verify(exchange).sendResponseHeaders(eq(500), anyLong());   //chk for correct status code
//    }

}
