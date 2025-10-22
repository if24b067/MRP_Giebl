-- Drop tables if they already exist
DROP TABLE IF EXISTS Ratings;
DROP TABLE IF EXISTS MediaEntries;
DROP TABLE IF EXISTS Users;

-- Create Users table
CREATE TABLE Users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    token VARCHAR(255)
);

-- Create MediaEntries table
CREATE TABLE MediaEntries (
    media_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    creator UUID NOT NULL,
    release_year INT NOT NULL,
    age_restriction INT NOT NULL,
    genres VARCHAR(255),
    FOREIGN KEY (creator) REFERENCES Users(user_id)
);

-- Create Ratings table
CREATE TABLE Ratings (
    rating_id UUID PRIMARY KEY,
    creator UUID NOT NULL,
    media_entry UUID NOT NULL,
    star_value INT CHECK (star_value >= 1 AND star_value <= 5),
    comment VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    likes INT DEFAULT 0,
    FOREIGN KEY (creator) REFERENCES Users(user_id),
    FOREIGN KEY (media_entry) REFERENCES MediaEntries(media_id)
);
