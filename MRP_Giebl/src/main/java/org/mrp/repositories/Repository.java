package org.mrp.repositories;

import org.mrp.models.User;

public interface Repository {
    public void save(User user);
    public void update();
    public void delete();
}
