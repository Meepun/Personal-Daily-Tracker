package com.tracker.calendartracker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class Tracker {

    private int trackerId;
    private int userId;
    private String trackerName;
    private LocalDate currentMonth;
    private Map<String, ButtonState> dayStates; // Stores states for each day in the month

    public Tracker(int trackerId, int userId, String trackerName, LocalDate currentMonth, Map<String, ButtonState> dayStates) {
        this.trackerId = trackerId;
        this.userId = userId;
        this.trackerName = trackerName;
        this.currentMonth = currentMonth;
        this.dayStates = dayStates;
    }

    // Getter and Setter methods
    public int getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(int trackerId) {
        this.trackerId = trackerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTrackerName() {
        return trackerName;
    }

    public void setTrackerName(String trackerName) {
        this.trackerName = trackerName;
    }

    public LocalDate getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(LocalDate currentMonth) {
        this.currentMonth = currentMonth;
    }

    public Map<String, ButtonState> getDayStates() {
        return dayStates;
    }

    public void setDayStates(Map<String, ButtonState> dayStates) {
        this.dayStates = dayStates;
    }

    // Enum to define button states
    public enum ButtonState {
        NORMAL,
        CHECKED,
        CROSSED
    }

    // Save the state of all days in the tracker to the database
    public void saveStateToDatabase() {
        // Ensure dayStates is not null before proceeding
        if (dayStates == null || dayStates.isEmpty()) {
            return; // No changes to save
        }

        // Prepare SQL insert statement
        String insertSQL = "INSERT INTO user_changes (user_id, tracker_id, datelog, state) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(user_id, tracker_id, datelog) " +  // Ensures no duplicate entries for the same date
                "DO UPDATE SET state = excluded.state";  // Update the state if there is a conflict

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            // Loop through all the days in the tracker and save their state
            for (Map.Entry<String, ButtonState> entry : dayStates.entrySet()) {
                String dateKey = entry.getKey();  // Format: "MM-dd"
                ButtonState state = entry.getValue();

                // Convert dateKey into LocalDate
                LocalDate date = LocalDate.parse(dateKey);  // Assuming dateKey is in the format "MM-dd"

                // Set values for the prepared statement
                preparedStatement.setInt(1, userId);               // Set user_id
                preparedStatement.setInt(2, trackerId);            // Set tracker_id
                preparedStatement.setString(3, date.toString());   // Set datelog (date of change)
                preparedStatement.setString(4, state.name());      // Set state (CHECKED, CROSSED, etc.)

                preparedStatement.addBatch();  // Add to batch for efficient execution
            }

            // Execute the batch of insertions
            int[] updateCounts = preparedStatement.executeBatch();
            System.out.println("Database update counts: " + java.util.Arrays.toString(updateCounts));

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception properly (e.g., show alert or log error)
        }
    }

    // Helper method to apply changes for a specific day
    public void setDayState(String dateKey, ButtonState state) {
        if (dayStates != null) {
            dayStates.put(dateKey, state);
        }
    }

    // Helper method to get the current state of a specific day
    public ButtonState getDayState(String dateKey) {
        if (dayStates != null && dayStates.containsKey(dateKey)) {
            return dayStates.get(dateKey);
        }
        return ButtonState.NORMAL; // Default to NORMAL if no state is found
    }
}
