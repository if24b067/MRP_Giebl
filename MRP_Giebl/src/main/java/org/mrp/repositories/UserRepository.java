package org.mrp.repositories;

import org.mrp.models.User;

import java.util.Objects;

public class UserRepository {

    public UserRepository() {
    }

    //chk whether username already exists
    public boolean chkUsername(String username) {
        return Objects.equals(username, "max");
        //check if username already exists
//        if (db.exists("SELECT 1 FROM users WHERE username = ?", username)) {
//            JsonHelper.sendError(exchange, 400, "Username already exists");
//            return;
//        }
    }

    public boolean chkLogin(String username, String password) {
        return Objects.equals(username, "max") && Objects.equals(password, "1234");
    }

    //save information in db
    public void saveUser(User user) {
        //insert user with UUID
//        UUID userId = db.insert(
//                "INSERT INTO users (id, username, password_hash) VALUES (?, ?, ?)",
//                user.id, user.username, user.passwordHash, user.createdAt
//        );
    }
}
