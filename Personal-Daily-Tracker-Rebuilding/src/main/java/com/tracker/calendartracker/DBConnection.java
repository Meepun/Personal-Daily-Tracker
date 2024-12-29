package com.tracker.calendartracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private static final String URL = "jdbc:sqlite:src/main/java/com/tracker/calendartracker/tracker.db"; // Adjust path if necessary
    private static Connection connection;
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    /**
     * Establishes and returns the connection to the SQLite database.
     * It will check if the current connection is open before creating a new one.
     * @return Connection object to the database
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
                LOGGER.info("Database connection established.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
        }
        return connection;
    }

    /**
     * Closes the current database connection (optional, depending sa project natin, eme eme lang tong part nato).
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to close the database connection", e);
        }
    }
}
