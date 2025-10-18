package org.mrp.repositories;

import java.util.List;
import java.util.UUID;

public interface Repository {

    //TODO DTO
    public <T> void save(T t);
    public <T> void update(T t);
    public void delete(UUID id);
    public <T> List<T> get();
}
