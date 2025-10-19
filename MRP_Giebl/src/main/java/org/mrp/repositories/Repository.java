package org.mrp.repositories;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Repository {

    <T> void save(T t) throws SQLException;
    <T> void update(T t) throws SQLException;
    void delete(UUID id) throws SQLException;
    List<Map<String, Object>> get() throws SQLException;
}
