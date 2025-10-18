package org.mrp.repositories;

import org.mrp.models.User;
import org.mrp.utils.UUIDv7Generator;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class UserRepository implements Repository{
    UUIDv7Generator uuidGenerator = new UUIDv7Generator();

    public UserRepository() {
        uuidGenerator = new UUIDv7Generator();
    }

    //save information in db
    @Override
    public <T> void save(T t) {
        if(t instanceof User) {
            User user = (User) t;
            //save to db
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
    public <T> List<T> get() {
        return null;
    }

    //chk whether username already exists
    public boolean chkUsername(String username) {
        return Objects.equals(username, "Max");
        //chk with db
    }

    public boolean chkLogin(String username, String password) {
        return Objects.equals(username, "Max") && Objects.equals(password, "1234");
        //chk with db
    }

    public UUID chkToken(String token) {
        //chk token with db
        if(Objects.equals(token, "Max_94faa0df-8317-4b18-ad77-4fb2fc607a58")) return uuidGenerator.randomUUID();
        return null;
    }
}
