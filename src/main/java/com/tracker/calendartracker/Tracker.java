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
        this.trackerId = -1;
        this.currentMonth = today.withDayOfMonth(1);
    }

    public Tracker(int userId, String trackerName, int trackerId) {
        this.userId = userId;
        this.trackerName = trackerName;
        this.trackerId = trackerId;
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

    public LocalDate getCurrentMonth() {
        return currentMonth;
    }

    public static List<Tracker> getTrackersForCurrentUser(int userId) {
        List<Tracker> trackers = new ArrayList<>();
        String query = "SELECT tracker_id, tracker_name FROM trackers WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int trackerId = rs.getInt("tracker_id");
                    String trackerName = rs.getString("tracker_name");
                    trackers.add(new Tracker(userId, trackerName, trackerId));
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

            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");

            System.out.println("Attempting to delete tracker with ID: " + this.trackerId);

            if (this.trackerId == -1) {
                System.out.println("Tracker ID is invalid: " + this.trackerId);
                return false;
            }

            pstmt1.setInt(1, this.trackerId);
            int rowsAffectedChanges = pstmt1.executeUpdate();
            System.out.println("Deleted " + rowsAffectedChanges + " related records from 'user_changes'.");

            String checkUserChanges = "SELECT COUNT(*) FROM user_changes WHERE tracker_id = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkUserChanges)) {
                pstmtCheck.setInt(1, this.trackerId);
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Found " + count + " records in 'user_changes' referencing this tracker.");
                    }
                }
            }

            pstmt2.setInt(1, this.trackerId);
            int rowsAffectedTracker = pstmt2.executeUpdate();
            System.out.println("Rows affected in 'trackers' table: " + rowsAffectedTracker);

            if (rowsAffectedTracker > 0) {
                System.out.println("Successfully deleted tracker with ID: " + this.trackerId);
                return true;
            } else {
                System.out.println("Failed to delete the tracker.");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error while deleting tracker: " + e.getMessage());
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
