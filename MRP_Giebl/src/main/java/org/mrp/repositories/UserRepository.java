package org.mrp.repositories;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.mrp.models.User;
import org.mrp.utils.Database;
import org.mrp.utils.UUIDv7Generator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class UserRepository implements Repository{
    private UUIDv7Generator uuidGenerator;
    private Database db;

    public UserRepository() {
        uuidGenerator = new UUIDv7Generator();
        db = new Database();
    }

    //save information in db
    @Override
    public <T> void save(T t) throws SQLException {
        if(t instanceof User) {
            User user = (User) t;
            //save to db
            UUID user_id = db.insert("INSERT INTO Users (user_id, username, password_hash, created_at) VALUES (?, ?, ?, ?)",
                    user.getUsername(),
                    user.getPasswordHash(),
                    LocalDate.now()
                    );
        }
    }

    @Override
    public <T> void update(T t) {
        if(t instanceof User) {
            User user = (User) t;
            //update in db
        }
    }

    @Override
    public void delete(UUID id) {
        //delete in db
    }

    @Override
    public List<Map<String, Object>> get() {
        return null;
    }

    //chk whether username already exists
    public boolean chkUsername(String username) throws SQLException {
        return db.exists("SELECT * FROM Users WHERE username = ?", username);
    }

    public boolean chkLogin(String username, String password) throws SQLException {
        ResultSet rs = db.query("SELECT (password_hash) FROM Users WHERE username = ?", username);
        if(!rs.next()) {return false;}  //username not found
        String pw_hash = rs.getString("password_hash");
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), pw_hash);
        return result.verified;
    }

    public UUID chkToken(String token) throws SQLException {
        ResultSet rs = db.query("SELECT (user_id) FROM Users WHERE token = ?", token);
        if(!rs.next()) {return null;}  //token not found
        return UUID.fromString(rs.getString("user_id"));
    }

    public void saveToken(String token, String username) throws SQLException {
        db.update("UPDATE Users SET token = ? WHERE username = ?", token, username);
    }
}
