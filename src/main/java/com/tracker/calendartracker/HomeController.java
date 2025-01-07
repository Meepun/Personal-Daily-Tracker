package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private LocalDate today = LocalDate.now();

    private static final String BASE_PATH = "/images/";
    private static final String CHECKED_IMAGE_PATH = "Check.png";
    private static final String CROSSED_IMAGE_PATH = "X.png";

    @FXML
    private void initialize() {
        // Retrieve userId from the session
        this.userId = SessionHandler.getInstance().getUserId();

        // Retrieve the username from your database or session
        String username = getUserNameFromDatabase(userId);

        // Set the label text dynamically
        welcomeLabel.setText("Welcome, " + username);

        currentMonth = today.withDayOfMonth(1);

        monthListView.setItems(FXCollections.observableArrayList(
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        ));

        monthListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateCalendar(newValue);
            }
        });

        // Initialize the calendar with the current month
        updateCalendar(currentMonth.getMonth().toString());

        // Handle create new tracker button
        createNewTrackerButton.setOnAction(this::handleCreateNewTracker);

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
        // Create a new tab for the tracker
        Tab tab = new Tab(tracker.getTrackerName());

        // Create a new navigation bar for the tab
        AnchorPane tabNavBar = createTabNavBar();

        // Create the calendar content for this tab
        AnchorPane calendarContent = createCalendarContent(tracker);

        // Create an AnchorPane to hold both the nav bar and the calendar content
        AnchorPane tabContent = new AnchorPane();

        // Position the navigation bar at the top of the tab content
        AnchorPane.setTopAnchor(tabNavBar, 0.0);
        AnchorPane.setLeftAnchor(tabNavBar, 0.0);
        AnchorPane.setRightAnchor(tabNavBar, 0.0);

        // Position the calendar content below the navigation bar
        AnchorPane.setTopAnchor(calendarContent, 50.0); // Adjust the value as necessary
        AnchorPane.setLeftAnchor(calendarContent, 0.0);
        AnchorPane.setRightAnchor(calendarContent, 0.0);

        // Add both the nav bar and the calendar content to the tab content
        tabContent.getChildren().addAll(tabNavBar, calendarContent);

        // Set the content of the tab
        tab.setContent(tabContent);

        // Add the tab to the TabPane
        tabPane.getTabs().add(tab);
    }

    private AnchorPane createTabNavBar() {
        // Create a new AnchorPane for the navigation bar
        AnchorPane navBar = new AnchorPane();

        navBar.getChildren().addAll(previousMonthButton, monthLabel, nextMonthButton);
        return navBar;
    }

    // Updated to no longer require the `navBar` parameter
    private AnchorPane createCalendarContent(Tracker tracker) {
        AnchorPane calendarPane = new AnchorPane();

        GridPane calendarGrid = new GridPane();
        calendarGrid.setLayoutX(3.0);
        calendarGrid.setLayoutY(14.0);
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.getStyleClass().add("calendar-grid");
        calendarGrid.setPrefSize(525.0, 374.0);
        calendarGrid.setAlignment(Pos.TOP_CENTER);

        // Update the calendar label
        updateCalendar(currentMonth.getMonth().toString());

        // Add the row for the days of the week (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            GridPane.setHalignment(dayLabel, HPos.CENTER); // Center horizontally
            GridPane.setValignment(dayLabel, VPos.CENTER);
            calendarGrid.add(dayLabel, i, 0);  // Add labels in the first row
        }

        // Get the first day of the month
        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), 1);

        // Adjust the first day of the week to Sunday as the first day
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        if (firstDayOfWeek == 7) {
            firstDayOfWeek = 0;  // Sunday as 0
        }

        int lengthOfMonth = firstOfMonth.lengthOfMonth();

        // Loop through the days of the month and add buttons
        for (int day = 1; day <= lengthOfMonth; day++) {
            int row = (firstDayOfWeek + day - 1) / 7 + 1;
            int col = (firstDayOfWeek + day - 1) % 7;

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45);
            dayButton.setId(String.valueOf(day)); // Assign the day as the button ID

            // Create the key for the date (use the full date format to store state)
            String key = currentMonth.getYear() + "-" + currentMonth.getMonth().name() + "-" + day;
            ButtonState state = loadButtonState(key, tracker.getTrackerId());  // Load the current state of the day
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);  // Apply the visual state (checked, crossed, etc.)

            // Set action for the button (to handle clicks)
            dayButton.setOnAction(e -> handleDayClick(dayButton, key, tracker));

            // Add the button to the grid at the appropriate position
            calendarGrid.add(dayButton, col, row);
        }

        // Add the calendar grid to the calendar pane
        calendarPane.getChildren().addAll(calendarGrid);

        // No need to pass the navBar anymore, it is added directly to the calendarPane
        return calendarPane;
    }

    @FXML
    public void handleCreateNewTracker(ActionEvent event) {
        // Create a new tracker
        String trackerName = "New Tracker";  // You can get this from user input if needed
        Tracker newTracker = new Tracker(userId, trackerName);
        newTracker.addNewTracker(trackerName);  // Adds the new tracker to the database

        // Get the generated trackerId (this is done inside addNewTracker method)
        int trackerId = newTracker.getTrackerId();  // Retrieve the trackerId after insertion

        // If the trackerId is valid (not -1), proceed to load user trackers
        if (trackerId != -1) {
            loadUserTrackers();  // Reload user trackers to include the new one
        } else {
            System.out.println("Failed to create new tracker. Tracker ID is invalid.");
        }
    }


    @FXML
    private void handleYearSelection(ActionEvent event) {
        Integer selectedYear = yearDropdown.getValue();
        currentMonth = currentMonth.withYear(selectedYear);
        updateCalendar(currentMonth.getMonth().toString());
    }

    // Handles the month selection from the ListView
    @FXML
    public void handleMonthYearSelection(MouseEvent event) {
        String selectedMonth = monthListView.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            currentMonth = LocalDate.now().withMonth(Month.valueOf(selectedMonth.toUpperCase()).getValue()).withDayOfMonth(1);

            Integer selectedYear = yearDropdown.getValue();
            currentMonth = currentMonth.withYear(selectedYear);

            updateCalendar(currentMonth.getMonth().toString());
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

    public enum ButtonState { NORMAL, CHECKED, CROSSED }

    private ButtonState loadButtonState(String dateKey, int trackerId) {
        String sql = "SELECT state FROM user_changes WHERE user_id = ? AND tracker_id = ? AND datelog = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);      // user_id
            pstmt.setInt(2, trackerId);      // tracker_id
            pstmt.setString(3, dateKey);     // datelog (formatted as "YYYY-MM-DD")
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ButtonState.valueOf(rs.getString("state"));  // Return the stored state
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ButtonState.NORMAL;  // Default state if no entry is found
    }

    private void saveButtonState(String dateKey, ButtonState state, int trackerId) {
        String sql = "INSERT INTO user_changes (user_id, tracker_id, datelog, state) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(user_id, tracker_id, datelog) " +
                "DO UPDATE SET state = excluded.state";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);      // user_id
            pstmt.setInt(2, trackerId);      // tracker_id
            pstmt.setString(3, dateKey);     // datelog (formatted as "YYYY-MM-DD")
            pstmt.setString(4, state.toString());  // state (e.g., "CHECKED")
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void applyButtonState(Button button, ButtonState state) {
        double imageSize = 20;

        switch (state) {
            case CHECKED:
                ImageView checkedImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(BASE_PATH + CHECKED_IMAGE_PATH))));
                checkedImageView.setFitWidth(imageSize);
                checkedImageView.setFitHeight(imageSize);
                checkedImageView.setPreserveRatio(true);
                button.setGraphic(checkedImageView);
                button.setText(""); // Temporarily remove text
                break;
            case CROSSED:
                ImageView crossedImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(BASE_PATH + CROSSED_IMAGE_PATH))));
                crossedImageView.setFitWidth(imageSize);
                crossedImageView.setFitHeight(imageSize);
                crossedImageView.setPreserveRatio(true);
                button.setGraphic(crossedImageView);
                button.setText(""); // Temporarily remove text
                break;
            case NORMAL:
            default:
                button.setGraphic(null);
                button.setText(button.getId()); // Restore the number as text
                break;
        }
    }

    public void handleDayClick(Button dayButton, String key, Tracker tracker) {
        ButtonState currentState = (ButtonState) dayButton.getUserData();

        ButtonState nextState;
        switch (currentState) {
            case NORMAL:
                nextState = ButtonState.CHECKED;
                break;
            case CHECKED:
                nextState = ButtonState.CROSSED;
                break;
            case CROSSED:
            default:
                nextState = ButtonState.NORMAL;
                break;
        }

        dayButton.setUserData(nextState);
        applyButtonState(dayButton, nextState);
        saveButtonState(key, nextState, tracker.getTrackerId());
    }
}
