package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class HomeController {

    @FXML private ComboBox<Integer> yearDropdown;
    @FXML private ListView<String> monthListView;
    @FXML private TabPane tabPane;
    @FXML private Label monthLabel;
    @FXML private Button previousMonthButton, nextMonthButton;
    @FXML private Button createNewTrackerButton;
    @FXML private Label welcomeLabel;

    private String userId;

    private LocalDate currentMonth = LocalDate.now();

    @FXML
    private void initialize() {

        // Retrieve userId from the session
        this.userId = SessionHandler.getInstance().getUserId();

        // Retrieve the username from your database or session
        String username = getUserNameFromDatabase(userId);  // Implement this method as per your logic

        // Set the label text dynamically
        welcomeLabel.setText("Welcome, " + username);

        initializeYearDropdown();
        initializeMonthListView();

        // Load the first tracker after login/signup
        loadUserTrackers();
    }

    private String getUserNameFromDatabase(String userId) {
        String sql = "SELECT username FROM loginsignup WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "User"; // Fallback value if username is not found
    }

    // Initialize the year dropdown
    private void initializeYearDropdown() {
        int currentYear = currentMonth.getYear();
        List<Integer> years = new ArrayList<>();
        for (int year = currentYear - 25; year <= currentYear + 50; year++) {
            years.add(year);
        }
        yearDropdown.setItems(FXCollections.observableArrayList(years));
        yearDropdown.setValue(currentYear);
        yearDropdown.setOnAction(this::handleYearSelection);
    }

    // Set up the month list and load initial tracker tabs
    private void initializeMonthListView() {
        List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        monthListView.setItems(FXCollections.observableArrayList(months));
    }

    private void loadUserTrackers() {
        // Clear existing tabs
        tabPane.getTabs().clear();

        // Load trackers from the database
        List<Tracker> trackers = Tracker.getUserTrackers(userId);

        // Add each tracker as a new tab
        for (Tracker tracker : trackers) {
            addTrackerTab(tracker);
        }
    }

    private void addTrackerTab(Tracker tracker) {
        Tab tab = new Tab(tracker.getTrackerName());
        tab.setContent(createCalendarContent(tracker));
        tabPane.getTabs().add(tab);
    }

    private AnchorPane createCalendarContent(Tracker tracker) {
        AnchorPane calendarPane = new AnchorPane();
        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);

        // Add weekday headers
        String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < weekdays.length; i++) {
            Label label = new Label(weekdays[i]);
            label.setStyle("-fx-font-weight: bold;");
            GridPane.setColumnIndex(label, i);
            calendarGrid.getChildren().add(label);
        }

        // Get the first day of the month to start placing dates in the correct column
        LocalDate firstDayOfMonth = tracker.getCurrentMonth().withDayOfMonth(1);  // Use Tracker's current month
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();  // 1 = Monday, 7 = Sunday
        if (firstDayOfWeek == 7) firstDayOfWeek = 0;  // Adjust for Sunday as the start of the week

        // Populate the calendar with days
        int daysInMonth = firstDayOfMonth.lengthOfMonth();  // Calculate days in the month directly
        Map<LocalDate, Tracker.ButtonState> dayStates = tracker.getMonthStates(firstDayOfMonth.getMonth());  // Use Tracker's method to get states for the current month

        // Loop through the days of the month and add them to the calendar grid
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDay = firstDayOfMonth.withDayOfMonth(day);

            // Determine the row and column for the current day
            int row = (firstDayOfWeek + day - 1) / 7;
            int col = (firstDayOfWeek + day - 1) % 7;

            // Create a button for each day
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45);

            // Get the state of the day
            Tracker.ButtonState state = dayStates.getOrDefault(currentDay, Tracker.ButtonState.NORMAL);
            dayButton.setUserData(state);
            tracker.applyButtonState(dayButton, state);  // Use Tracker's method to apply the state to the button

            // Add action to the button for checking or crossing the day
            dayButton.setOnAction(e -> tracker.handleDayClick(dayButton, currentDay));

            // Add the day button to the grid
            GridPane.setRowIndex(dayButton, row);
            GridPane.setColumnIndex(dayButton, col);
            calendarGrid.getChildren().add(dayButton);
        }

        // Add calendarGrid to the calendarPane
        calendarPane.getChildren().add(calendarGrid);
        return calendarPane;
    }

    @FXML
    public void handleCreateNewTracker(ActionEvent event) {
        // Create a new tracker
        String trackerName = "New Tracker"; // You can get this from user input if needed
        Tracker newTracker = new Tracker(userId, trackerName);
        newTracker.addNewTracker(trackerName);

        // Reload user trackers to include the new one
        loadUserTrackers();
    }

    @FXML
    private void handleYearSelection(ActionEvent event) {
        Integer selectedYear = yearDropdown.getValue();
        currentMonth = currentMonth.withYear(selectedYear);
        updateCalendar(currentMonth.getMonth().toString());
    }

    // Handles the month selection from the ListView
    @FXML
    public void handleMonthSelection(MouseEvent event) {
        String selectedMonth = monthListView.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            currentMonth = LocalDate.now().withMonth(Month.valueOf(selectedMonth.toUpperCase()).getValue()).withDayOfMonth(1);
            updateCalendar(selectedMonth);
        }
    }

    @FXML
    private void handlePreviousMonth(ActionEvent event) {
        currentMonth = currentMonth.minusMonths(1);
        updateCalendar(currentMonth.getMonth().toString());
    }

    @FXML
    private void handleNextMonth(ActionEvent event) {
        currentMonth = currentMonth.plusMonths(1);
        updateCalendar(currentMonth.getMonth().toString());
    }

    private void updateCalendar(String month) {
        monthLabel.setText(month + " " + currentMonth.getYear());
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tracker/calendartracker/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
