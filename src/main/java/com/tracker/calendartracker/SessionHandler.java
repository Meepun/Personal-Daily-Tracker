package com.tracker.calendartracker;

public class SessionHandler {

    private static SessionHandler instance;
    private int userId;
    private String username;

    private SessionHandler() {
        // Initialize the session as needed
    }

    // Singleton pattern
    public static SessionHandler getInstance() {
        if (instance == null) {
            instance = new SessionHandler();
        }
        return instance;
    }

    // Getter for userId
    public int getUserId() {
        return userId;
    }

    // Setter for userId
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Setter for username
    public void setUsername(String username) {
        this.username = username;
    }

    // Clear session (useful for logout or after signup)
    public void clearSession() {
        this.userId = -1;  // Reset to default or invalid value
        this.username = null;  // Clear the username
    }
}
