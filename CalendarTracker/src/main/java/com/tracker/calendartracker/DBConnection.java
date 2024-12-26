package com.tracker.calendartracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/personal-tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    private static Connection connection;

    // Establish connection
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the database.");
        }
        return connection;
    }
}
