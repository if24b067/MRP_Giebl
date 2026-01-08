package org.mrp.services;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.Headers;
import org.mockito.Mockito;
import org.mrp.models.Rating;
import org.mrp.repositories.RatingRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RatingServiceTest {
    private RatingRepository ratingRepository;
    private RatingService ratingService;
    private AuthService authService;
    private HttpExchange exchange;
    private JsonHelper jsonHelper;
    InputStream requestBody;

    @BeforeEach
    public void setUp() {
        ratingRepository = Mockito.mock(RatingRepository.class);  //mock ratingRepository
        authService = Mockito.mock(AuthService.class);  //mock authService
        jsonHelper = Mockito.mock(JsonHelper.class);    //mock jsonHelper
        ratingService = new RatingService(ratingRepository, authService, jsonHelper); //pass mocked repo and service
        exchange = Mockito.mock(HttpExchange.class);    //mock HTTP exchange
        requestBody = mock(InputStream.class);
    }

    /* test read*/
    @Test
    void testReadShouldReturnErrorWhenRatingNotFound() throws IOException, SQLException, URISyntaxException {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/" + UUID.randomUUID()));
        when(ratingRepository.getOne(any())).thenReturn(null);

        // Act
        ratingService.read(exchange);

        // Assert
        verify(jsonHelper).sendError(exchange, 404, "Media not found");
    }

    @Test
    void testReadShouldReturnOwnRatings() throws IOException, SQLException, URISyntaxException {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/own"));
        when(ratingRepository.getOwn(userId)).thenReturn(Collections.singletonList(new Rating()));

        // Act
        ratingService.read(exchange);

        // Assert
        verify(jsonHelper).sendResponse(eq(exchange), eq(200), anyList());
    }

    @Test
    void testReadShouldSetCommentToNullWhenNotVisible() throws IOException, SQLException, URISyntaxException {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/" + UUID.randomUUID()));
        Rating rating = new Rating();
        rating.setVis(false);
        rating.setComment("Visible Comment");
        when(ratingRepository.getOne(any())).thenReturn(rating);

        // Act
        ratingService.read(exchange);

        // Assert
        assertNull(rating.getComment());
        verify(jsonHelper).sendResponse(eq(exchange), eq(200), eq(rating));
    }

    /* test update */
    @Test
    void testUpdateShouldReturnErrorWhenRequestBodyIsEmpty() throws IOException, SQLException {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(0); // Simulate empty body

        // Act
        ratingService.update(exchange);

        // Assert
        verify(jsonHelper).sendError(exchange, 400, "request body is empty");
    }

    @Test
    void testUpdateShouldReturnErrorWhenRatingIdIsNotPresent() throws IOException, SQLException {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(1); // Simulate non-empty body
        Map<String, String> requestMap = new HashMap<>();
        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);

        // Act
        ratingService.update(exchange);

        // Assert
        verify(jsonHelper).sendError(exchange, 400, "correct input required");
    }

    @Test
    void testUpdateShouldReturnErrorWhenRatingIdIsInvalid() throws IOException, SQLException {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(1);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("rating\\id", "invalid-uuid");
        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);

        // Act
        ratingService.update(exchange);

        // Assert
        verify(jsonHelper).sendError(exchange, 400, "correct input required");
    }

//    @Test
//    void testUpdateShouldReturnErrorWhenUserIsNotCreator() throws IOException, SQLException {
//        // Arrange
//        UUID userId = UUID.randomUUID();
//        UUID ratingId = UUID.randomUUID();
//        when(authService.validateToken(exchange)).thenReturn(userId);
//        when(exchange.getRequestBody()).thenReturn(requestBody);
//        when(requestBody.available()).thenReturn(1);
//
//        Map<String, String> requestMap = new HashMap<>();
//        requestMap.put("rating\\id", ratingId.toString());
//        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);
//        when(ratingRepository.chkCreator(ratingId, userId)).thenReturn(false);
//
//        // Act
//        ratingService.update(exchange);
//
//        // Assert
//        verify(jsonHelper).sendError(exchange, 401, "unauthorized to edit post");
//    }

    /* test delete */
    /* test like */
}
