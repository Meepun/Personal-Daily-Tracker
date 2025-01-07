package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class Tracker {

    private LocalDate today = LocalDate.now();
    private static String userId;
    private String trackerName;
    private int trackerId;
    private LocalDate currentMonth;

    enum ButtonState { NORMAL, CHECKED, CROSSED }

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

    // Get the states of all the days in a specific month
    public Map<LocalDate, ButtonState> getMonthStates(Month month) {
        Map<LocalDate, ButtonState> states = new HashMap<>();

        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), month, 1);
        int lengthOfMonth = firstOfMonth.lengthOfMonth();

        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate date = LocalDate.of(currentMonth.getYear(), month, day);
            String dateKey = date.toString(); // Use the date as the key

            ButtonState state = loadButtonState(dateKey);
            states.put(date, state);
        }

        return states;
    }

    // Load the button state from the database based on the date key
    private ButtonState loadButtonState(String dateKey) {
        String sql = "SELECT state FROM user_changes WHERE user_id = ? AND datelog = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, dateKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ButtonState.valueOf(rs.getString("state"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ButtonState.NORMAL;
    }

    // Save the button state into the database for a given date key
    private void saveButtonState(String dateKey, ButtonState state) {
        String sql = "INSERT INTO user_changes (user_id, datelog, state) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT(user_id, datelog) DO UPDATE SET state = excluded.state";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, dateKey);
            pstmt.setString(3, state.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Apply the button state to the button (e.g., adding a check or cross icon)
    void applyButtonState(Button button, ButtonState state) {
        switch (state) {
            case CHECKED:
                button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/Check.png"))));
                break;
            case CROSSED:
                button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/X.png"))));
                break;
            case NORMAL:
            default:
                button.setGraphic(null);
                break;
        }
    }

    // Update the calendar view with the new month (used by HomeController)
    public void updateCalendarView(String trackerName, GridPane calendarGrid, Month month) {
        Map<LocalDate, ButtonState> states = getMonthStates(month);

        // Clear existing grid content
        calendarGrid.getChildren().clear();

        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        // Set the month label
        calendarGrid.setGridLinesVisible(true);

        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), month, 1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() == 7 ? 0 : firstOfMonth.getDayOfWeek().getValue();
        int lengthOfMonth = firstOfMonth.lengthOfMonth();

        for (int day = 1; day <= lengthOfMonth; day++) {
            int row = (firstDayOfWeek + day - 1) / 7 + 1;
            int col = (firstDayOfWeek + day - 1) % 7;

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45);
            LocalDate date = LocalDate.of(currentMonth.getYear(), month, day);
            ButtonState state = states.get(date);
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);
            dayButton.setOnAction(e -> handleDayClick(dayButton, date));
            calendarGrid.add(dayButton, col, row);
        }
    }

    // Handle day button clicks (changing the state of the button)
    public void handleDayClick(Button dayButton, LocalDate date) {
        ButtonState currentState = (ButtonState) dayButton.getUserData();
        ButtonState nextState = currentState == ButtonState.NORMAL ? ButtonState.CHECKED :
                currentState == ButtonState.CHECKED ? ButtonState.CROSSED :
                        ButtonState.NORMAL;
        dayButton.setUserData(nextState);
        applyButtonState(dayButton, nextState);
        saveButtonState(date.toString(), nextState);
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
            pstmt.setString(1, Tracker.userId);
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
            pstmt.setString(1, Tracker.userId);
            pstmt.setString(2, trackerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Tracker(Tracker.userId, rs.getString("tracker_name"));  // Pass trackerId
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
