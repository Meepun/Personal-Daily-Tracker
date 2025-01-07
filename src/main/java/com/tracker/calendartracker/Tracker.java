package com.tracker.calendartracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class Tracker {

    private LocalDate today = LocalDate.now();
    private static String userId;
    private String trackerName;
    private int trackerId;
    private LocalDate currentMonth;

    public Tracker(String userId, String trackerName) {
        this.userId = userId;
        this.trackerName = trackerName;
        this.trackerId = trackerId;
        this.currentMonth = today.withDayOfMonth(1);
    }

    // Getter and Setter for trackerName
    public String getTrackerName() {
        return trackerName;
    }

    public void setTrackerName(String trackerName) {
        this.trackerName = trackerName;
    }

    // Getter and Setter for trackerId
    public int getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(int trackerId) {
        this.trackerId = trackerId;
    }

    // Getter for currentMonth
    public LocalDate getCurrentMonth() {
        return currentMonth;
    }

    // Add New Tracker
    public void addNewTracker(String trackerName) {
        String sql = "INSERT INTO trackers (user_id, tracker_name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, trackerName);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                trackerId = rs.getInt(1);  // Retrieve the generated tracker ID
                System.out.println("Tracker added with ID: " + trackerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete Tracker
    public void deleteTracker() {
        String sql = "DELETE FROM trackers WHERE tracker_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trackerId);  // Use trackerId as int
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Also delete associated user changes
        deleteTrackerChanges(trackerId);
    }

    private void deleteTrackerChanges(int trackerId) {
        String sql = "DELETE FROM user_changes WHERE tracker_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trackerId);  // Use trackerId as int
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get Tracker Names for User
    public static List<String> getTrackerNamesForUser(String userId) {
        List<String> trackerNames = new ArrayList<>();
        String sql = "SELECT tracker_name FROM trackers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                trackerNames.add(rs.getString("tracker_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trackerNames;
    }

    // Load Tracker by Name
    public static Tracker loadTrackerByName(String trackerName, String userId) {
        String sql = "SELECT * FROM trackers WHERE user_id = ? AND tracker_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, trackerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Tracker(userId, rs.getString("tracker_name"));  // Pass trackerId
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Tracker> getUserTrackers(String userId) {
        List<Tracker> trackers = new ArrayList<>();
        String sql = "SELECT * FROM trackers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                trackers.add(new Tracker(userId, rs.getString("tracker_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trackers;
    }
}
