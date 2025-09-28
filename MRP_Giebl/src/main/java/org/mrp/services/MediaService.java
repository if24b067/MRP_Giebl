package org.mrp.services;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.utils.JsonHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaService {

    public MediaService() {}

    public void create (HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> request = JsonHelper.parseRequest(exchange, HashMap.class);
        String title = request.get("title");
        String desc = request.get("desc");
        String creatorToken = request.get("token");
        String releaseYearString = request.get("releaseYear");
        String ageRestrictionString = request.get("ageRestriction");
        String genreString = request.get("genres");

        //validate input
        if (title == null || title.trim().isEmpty() ||
                desc == null || desc.trim().isEmpty() ||
                creatorToken == null || creatorToken.trim().isEmpty() ||
                releaseYearString == null || releaseYearString.trim().isEmpty() ||
                ageRestrictionString == null || ageRestrictionString.trim().isEmpty() ||
                genreString == null || genreString.trim().isEmpty()) {
            JsonHelper.sendError(exchange, 400, "complete media information required");
            return;
        }


        //parse info if valid
        int releaseYear = Integer.parseInt(releaseYearString);
        int ageRestriction = Integer.parseInt(ageRestrictionString);
        List<String> genres = Arrays.asList(genreString.split(","));

        //insert into db
//        db.insert(
//                "INSERT INTO mediaEntries (title, desc, releaseYear, ageRestriction, genres) VALUES (?, ?, ?, ?, ?)",
//                title, desc, releaseYear, ageRestriction, genres
//        );

        //response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Media Entry created successfully");

        JsonHelper.sendResponse(exchange, 201, response);
    }
}
