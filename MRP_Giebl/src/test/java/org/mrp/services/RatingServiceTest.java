package org.mrp.services;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.mrp.models.Rating;
import org.mrp.repositories.RatingRepository;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

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
        requestBody = Mockito.mock(InputStream.class);  //mock InputStream
    }

    /* test read function */
    @Test
    void testReadShouldReturnErrorWhenRatingNotFound() throws IOException, SQLException, URISyntaxException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/" + UUID.randomUUID()));
        when(ratingRepository.getOne(any())).thenReturn(null);

        ratingService.read(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 404, "Media not found");
    }

    @Test
    void testReadShouldReturnOwnRatings() throws IOException, SQLException, URISyntaxException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/own"));
        when(ratingRepository.getOwn(userId)).thenReturn(Arrays.asList(new Rating(), new Rating()));

        ratingService.read(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendResponse(eq(exchange), eq(200), anyList());
    }

    @Test
    void testReadShouldSetCommentToNullWhenNotVisible() throws IOException, SQLException, URISyntaxException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/" + UUID.randomUUID()));
        Rating rating = new Rating();
        rating.setVis(false);
        rating.setComment("Visible Comment");
        when(ratingRepository.getOne(any())).thenReturn(rating);

        ratingService.read(exchange);

        //chk whether function behaved as expected
        assertNull(rating.getComment());
        verify(jsonHelper).sendResponse(eq(exchange), eq(200), eq(rating));
    }

    /* test update */
    @Test
    void testUpdateShouldReturnErrorWhenRequestBodyIsEmpty() throws IOException, SQLException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(0); // Simulate empty body

        ratingService.update(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 400, "request body is empty");
    }

    @Test
    void testUpdateShouldReturnErrorWhenRatingIdIsNotPresent() throws IOException, SQLException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(1); // Simulate non-empty body
        Map<String, String> requestMap = new HashMap<>();   //map does not include rating id
        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);

        ratingService.update(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 400, "correct input required");
    }

    @Test
    void testUpdateShouldReturnErrorWhenRatingIdIsInvalid() throws IOException, SQLException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(1);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("rating_id", "invalid-uuid");    //map includes invalid rating id
        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);

        ratingService.update(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 400, "correct input required");
    }

    @Test
    void testUpdateShouldReturnErrorWhenUserIsNotCreator() throws IOException, SQLException {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(1);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("rating_id", ratingId.toString());   //map includes rating id
        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);
        when(ratingRepository.chkCreator(ratingId, userId)).thenReturn(false);  //user is not creator

        ratingService.update(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 401, "unauthorized to edit post");
    }

    /* test delete */
    @Test
    void testDeleteShouldReturnErrorWhenRatingNotFound() throws IOException, SQLException, URISyntaxException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/" + UUID.randomUUID()));
        when(ratingRepository.getOne(any())).thenReturn(null);  //not found

        ratingService.delete(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 404, "Rating not found");
    }

    @Test
    void testDeleteShouldReturnErrorWhenRatingIdIsNotPresent() throws IOException, SQLException, URISyntaxException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/"));    //no rating id

        ratingService.delete(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 400, "rating Id required");
    }

    @Test
    void testDeleteShouldReturnErrorWhenUserIsNotCreator() throws IOException, SQLException, URISyntaxException {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();
        Rating rating = new Rating();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestURI()).thenReturn(new URI("/ratings/" + ratingId)); //rating id present
        when(ratingRepository.getOne(ratingId)).thenReturn(rating); //rating found
        when(ratingRepository.chkCreator(ratingId, userId)).thenReturn(false);  //user not creator

        ratingService.delete(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 401, "unauthorized to delete post");
    }

    /* test like */
    @Test
    void testLikeShouldReturnErrorWhenRequestBodyIsEmpty() throws IOException, SQLException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(0); // Simulate empty body

        ratingService.like(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 400, "request body is empty");
    }

    @Test
    void testLikeShouldReturnErrorWhenRatingIdIsNotPresent() throws IOException, SQLException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(1); // Simulate non-empty body
        Map<String, String> requestMap = new HashMap<>();   //map does not include rating id
        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);

        ratingService.like(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 400, "correct input required");
    }

    @Test
    void testLikeShouldReturnErrorWhenRatingIdIsInvalid() throws IOException, SQLException {
        UUID userId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(1);    //non empty body
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("rating_id", "invalid-uuid");    //includes invalid rating id
        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);

        ratingService.like(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 400, "correct input required");
    }

    @Test
    void testLikeShouldReturnErrorWhenUserHasAlreadyLiked() throws IOException, SQLException {
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();
        //mock expected behaviour
        when(authService.validateToken(exchange)).thenReturn(userId);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(requestBody.available()).thenReturn(1);    //non empty body
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("rating_id", ratingId.toString());   //valid rating id
        when(jsonHelper.parseRequest(exchange, Map.class)).thenReturn(requestMap);
        when(ratingRepository.chkUserAndRating(userId, ratingId)).thenReturn(true); //already liked

        ratingService.like(exchange);

        //chk whether function behaved as expected
        verify(jsonHelper).sendError(exchange, 400, "already liked this rating");
    }
}
