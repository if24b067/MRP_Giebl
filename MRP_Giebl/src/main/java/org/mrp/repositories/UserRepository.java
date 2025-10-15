package org.mrp.repositories;

import org.mrp.models.User;

import java.util.Objects;

public class UserRepository implements Repository{

    public UserRepository() {
    }

    //save information in db
    @Override
    public void save(User user) {
        //save to db
    }

    @Override
    public void update() {
        //update in db
    }

    @Override
    public void delete() {
        //delete in db
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

}
