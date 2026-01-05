package org.mrp.services;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.mrp.repositories.RatingRepository;

import java.io.IOException;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RatingServiceTest {
    private RatingRepository ratingRepository;
    private RatingService ratingService;
    private AuthService authService;
    private HttpExchange exchange;

    @BeforeEach
    public void setUp() {
        ratingRepository = Mockito.mock(RatingRepository.class);  //mock ratingRepository
        authService = Mockito.mock(AuthService.class);  //mock authService
        ratingService = new RatingService(ratingRepository, authService); //pass mocked repo and service
        exchange = Mockito.mock(HttpExchange.class);    //mock HTTP exchange
    }

//    @Test
//    void createShouldReturnErrorWhenTokenIsInvalid() throws IOException, SQLException {
//        when(authService.validateToken(exchange)).thenReturn(null);
//        when(exchange.getRequestHeaders()).thenReturn(new Headers()); // Create a mock for HttpHeaders
//
//        // Mocking the relevant behavior
//        when(exchange.getRequestHeaders().getFirst("Authorization")).thenReturn(null);
//
//        ratingService.create(exchange);
//
//        verify(exchange, times(1)).sendResponseHeaders(401, -1);
//    }
}
