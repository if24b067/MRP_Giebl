package org.mrp.repositories;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Repository {

    //TODO DTO
    public <T> void save(T t) throws SQLException;
    public <T> void update(T t) throws SQLException;
    public void delete(UUID id) throws SQLException;
    public List<Map<String, Object>> get() throws SQLException;
}
