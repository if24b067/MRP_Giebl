//file paritally generated
package org.mrp.utils;

import java.sql.*;
import java.util.UUID;

public class Database {
    private String URL = "jdbc:postgresql://localhost:5432/mrp_db";
    private String USER = "postgres";
    private String PASSWORD = "postgres";
    private UUIDv7Generator uuidv7Generator;
    private Database instance;
    private Connection connection;

    public Database() {
        uuidv7Generator =  new UUIDv7Generator();
        connect();
    }

    public synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    //query and return resultset
    public ResultSet query(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = prepareStatement(sql, params);
        return stmt.executeQuery();
    }

    //update, insert, delete
    public int update(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = prepareStatement(sql, params);
        return stmt.executeUpdate();
    }

    //insert and generate uuidv7
    public UUID insert(String sql, Object... params) throws SQLException {
        UUID uuid = uuidv7Generator.randomUUID();

        //params array with UUID as first parameter
        Object[] newParams = new Object[params.length + 1];
        newParams[0] = uuid;
        System.arraycopy(params, 0, newParams, 1, params.length);

        PreparedStatement stmt = prepareStatement(sql, newParams);
        int affectedRows = stmt.executeUpdate();

        if (affectedRows == 0) {
            throw new SQLException("Insert failed, no rows affected.");
        }

        return uuid;
    }

    //chk if record exists
    public boolean exists(String sql, Object... params) throws SQLException {
        try (ResultSet rs = query(sql, params)) {
            return rs.next();
        }
    }


    //prepare stmt to prevent sql injection
    private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        setParameters(stmt, params);
        return stmt;
    }

    //bind params to ?
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

}