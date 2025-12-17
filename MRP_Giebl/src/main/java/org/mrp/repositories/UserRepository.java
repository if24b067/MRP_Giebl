package org.mrp.repositories;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.mrp.models.MediaEntry;
import org.mrp.models.Rating;
import org.mrp.models.User;
import org.mrp.utils.Database;
import org.mrp.utils.UUIDv7Generator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    public <T> UUID save(T t) throws SQLException {
        UUID user_id = null;
        if(t instanceof User) {
            User user = (User) t;
            //save to db
            user_id = db.insert("INSERT INTO Users (user_id, username, password_hash, created_at) VALUES (?, ?, ?, ?)",
                    user.getUsername(),
                    user.getPasswordHash(),
                    LocalDate.now()
                    );
        }
        return user_id;
    }

    @Override
    public <T> void update(T t) throws SQLException {
        if(t instanceof User) {
            User user = (User) t;

            db.update("UPDATE Users SET username = ?, password_hash = ? WHERE user_id = ?",
                    user.getUsername(),
                    user.getPasswordHash(),
                    user.getId()
                    );
        }
    }

    @Override
    public void delete(UUID id) throws SQLException {
        db.update("DELETE FROM Users WHERE user_id = ?", id);
    }

    @Override
    public List<Object> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        ResultSet rs = db.query("SELECT * FROM Users");

        while (rs.next()) {

            User user = new User(
                    (UUID) rs.getObject("user_id"),
                    rs.getString("username"),
                    null,
                    rs.getTimestamp("created_at")
            );

            users.add(user);
        }

        return new ArrayList<Object>(users);
    }

    @Override
    public Object getOne(UUID id) throws SQLException {
        ResultSet rs = db.query("SELECT * FROM Users WHERE user_id = ?", id);

        if(rs.next())
        {

            return new User(
                    (UUID) rs.getObject("user_id"),
                    rs.getString("username"),
                    null,
                    rs.getTimestamp("created_at")
            );
        }
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
        return (UUID) rs.getObject("user_id");
    }

    public void saveToken(String token, String username) throws SQLException {
        db.update("UPDATE Users SET token = ? WHERE username = ?", token, username);
    }

    public boolean chkPW(String password, UUID user_id) throws SQLException {
        ResultSet rs = db.query("SELECT (password_hash) FROM Users WHERE user_id = ?", user_id);
        if(!rs.next()) {return false;}  //user not found
        String pw_hash = rs.getString("password_hash");
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), pw_hash);
        return result.verified;
    }
}
