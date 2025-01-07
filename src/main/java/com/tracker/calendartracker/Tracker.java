package com.tracker.calendartracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Tracker {

    private LocalDate today = LocalDate.now();
    private static int userId;
    private String trackerName;
    private int trackerId;
    private LocalDate currentMonth;

    public Tracker(int userId, String trackerName) {
        this.userId = userId;
        this.trackerName = trackerName;
        this.trackerId = -1;  // Default value, will be set after adding to DB
        this.currentMonth = today.withDayOfMonth(1);
    }

    public Tracker(int userId, String trackerName, int trackerId) {
        this.userId = userId;
        this.trackerName = trackerName;
        this.trackerId = trackerId;  // Set the actual trackerId passed from DB
        this.currentMonth = today.withDayOfMonth(1);
    }

    public String getTrackerName() {
        return this.trackerName;
    }

    public void setTrackerName(String trackerName) {
        this.trackerName = trackerName;
    }

    public int getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(int trackerId) {
        this.trackerId = trackerId;
    }

    public LocalDate getCurrentMonth() {
        return currentMonth;
    }

    public static List<Tracker> getTrackersForCurrentUser(int userId) {
        List<Tracker> trackers = new ArrayList<>();
        String query = "SELECT tracker_id, tracker_name FROM trackers WHERE user_id = ?"; // Filter by user_id

        // Fetch trackers from the database based on user_id
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId); // Set the logged-in user's ID

            // Execute the query and process the result set
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int trackerId = rs.getInt("tracker_id");
                    String trackerName = rs.getString("tracker_name");
                    trackers.add(new Tracker(trackerId, trackerName)); // Create Tracker objects
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trackers;
    }

    public boolean deleteTracker() {
        String deleteUserChangesQuery = "DELETE FROM user_changes WHERE tracker_id = ?";
        String deleteTrackerQuery = "DELETE FROM trackers WHERE tracker_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt1 = conn.prepareStatement(deleteUserChangesQuery);
             PreparedStatement pstmt2 = conn.prepareStatement(deleteTrackerQuery)) {

            // First, delete any entries related to this tracker in the user_changes table
            pstmt1.setInt(1, this.trackerId);
            int rowsAffectedChanges = pstmt1.executeUpdate();

            // Then, delete the tracker from the trackers table
            pstmt2.setInt(1, this.trackerId);
            int rowsAffectedTracker = pstmt2.executeUpdate();

            // If both deletions are successful, return true
            return rowsAffectedChanges >= 0 && rowsAffectedTracker > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addNewTracker(int userId, String trackerName) {
        String sql = "INSERT INTO trackers (user_id, tracker_name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, trackerName);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    this.trackerId = generatedKeys.getInt(1);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean renameTracker(String newName, Connection connection) {
        String query = "UPDATE trackers SET tracker_name = ? WHERE tracker_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newName);
            preparedStatement.setInt(2, this.trackerId);
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                this.trackerName = newName;
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Tracker> getUserTrackers(int userId) {
        List<Tracker> trackers = new ArrayList<>();
        String sql = "SELECT * FROM trackers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int trackerId = rs.getInt("tracker_id");
                trackers.add(new Tracker(userId, rs.getString("tracker_name"), trackerId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trackers;
    }
}
