package com.tracker.calendartracker;

public class SessionHandler {
    private static SessionHandler instance;
    private String userId;

    private SessionHandler() {}

    public static SessionHandler getInstance() {
        if (instance == null) {
            instance = new SessionHandler();
        }
        return instance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void clearSession() {
        this.userId = null;
    }
}
