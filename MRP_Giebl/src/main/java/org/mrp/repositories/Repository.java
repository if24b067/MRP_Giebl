package org.mrp.repositories;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface Repository {

    <T> UUID save(T t) throws SQLException;
    <T> void update(T t) throws SQLException;
    void delete(UUID id) throws SQLException;
    List<Object> getAll() throws SQLException;
    Object getOne(UUID id) throws SQLException;
}
