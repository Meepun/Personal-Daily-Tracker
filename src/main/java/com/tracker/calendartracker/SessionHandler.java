package com.tracker.calendartracker;

public class SessionHandler {

    private static SessionHandler instance;
    private int userId;

    private SessionHandler() {
    }

    // Singleton pattern
    public static SessionHandler getInstance() {
        if (instance == null) {
            instance = new SessionHandler();
        }
        return instance;
    }

    public int getUserId() {

        return userId;
    }

    public void setUserId(int userId) {

        this.userId = userId;
    }

    public void clearSession() {
        this.userId = -1;
    }
}
