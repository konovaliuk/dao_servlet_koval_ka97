package com.booking.koval.connection;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConnectionPool {
    private final String url;
    private final String login;
    private final String password;
    private final List<Connection> usedConnections = new ArrayList<>();
    private static ConnectionPool instance;

    private static int STARTING_POOL_SIZE = 10;
    private final List<Connection> pool = new ArrayList<>(STARTING_POOL_SIZE);

    private ConnectionPool() throws SQLException {

        this.url = "jdbc:mysql://localhost:3306/booking?useSSL=false&serverTimezone=GMT%2B3";
        login = "root";
        password = "root";

        for (int i = 0; i < STARTING_POOL_SIZE; i++) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            pool.add(DriverManager.getConnection(this.url, login, password));
        }
    }

    private Connection createConnection() throws SQLException {return DriverManager.getConnection(this.url, login, password);}

    public static ConnectionPool getInstance() throws SQLException {
        if (instance == null) {instance = new ConnectionPool();}
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (pool.size() == 0) {pool.add(createConnection());}
        Connection connection = pool.remove(pool.size()-1);
        usedConnections.add(connection);
        return connection;
    }

    public boolean releaseConnection(Connection connection) {
        pool.add(connection);
        while (pool.size() >= STARTING_POOL_SIZE+1) {pool.remove(pool.size()-1);}
        return usedConnections.remove(connection);
    }

    public void shutdown() throws SQLException {
        usedConnections.forEach(this::releaseConnection);
        for (Connection c : pool) {
            c.close();
        }
        pool.clear();
    }

    public long amountOfFreeConnections() {
        return pool.size();
    }

    public String getUrl() {
        return url;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
