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
import java.util.stream.Collectors;

public class HomeController {

    @FXML private ComboBox<Integer> yearDropdown;
    @FXML private ListView<String> monthListView;
    @FXML private TabPane tabPane;
    @FXML private Label monthLabel;
    @FXML private Button previousMonthButton, nextMonthButton;
    @FXML private Button createNewTrackerButton;
    @FXML public  Button deleteTrackerButton;
    @FXML private Label welcomeLabel;

    private int userId;
    private String trackerName;
    private LocalDate currentMonth = LocalDate.now();
    private LocalDate today = LocalDate.now();
    private Map<Tab, Tracker> trackerMap = new HashMap<>();


    private static final String BASE_PATH = "/images/";
    private static final String CHECKED_IMAGE_PATH = "Check.png";
    private static final String CROSSED_IMAGE_PATH = "X.png";

    @FXML
    private void initialize() {
        // Retrieve userId from the session
        this.userId = SessionHandler.getInstance().getUserId();

        // Retrieve the username from your database or session
        String username = getUserNameFromDatabase(String.valueOf(userId));

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

        yearDropdown.setOnMouseClicked(this::handleMonthYearSelection);

        // Initialize the calendar with the current month
        updateCalendar(currentMonth.getMonth().toString());

        // Handle create new tracker button
        createNewTrackerButton.setOnAction(this::handleCreateNewTracker);
        deleteTrackerButton.setOnAction(this::handleDeleteTracker);

        initializeYearDropdown();
        initializeMonthListView();

        // Load the first tracker after login/signup
        loadUserTrackers();

        // Add double-click event listener to the tabs
        tabPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {  // Check if it's a double click
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();  // Get the tab that was double-clicked
                if (selectedTab != null) {
                    handleRenameTracker(selectedTab);  // Rename the selected tab's tracker
                }
            }
        });
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

        // Debug log to check if trackers are loaded correctly
        System.out.println("Loaded " + trackers.size() + " trackers.");

        // Add each tracker as a new tab
        for (Tracker tracker : trackers) {
            addTrackerTab(tracker);
        }
    }

    private void addTrackerTab(Tracker tracker) {
        // Create a new tab for the tracker
        Tab tab = new Tab(tracker.getTrackerName());

        // Set the tracker as the tab's user data to associate it with the tab
        tab.setUserData(tracker);  // Link the tab to the Tracker

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

        // Add the mouse event listener to the tab's content (AnchorPane)
        tabContent.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {  // Double-click detection
                handleRenameTracker(tab);  // Pass the tab to the rename handler
            }
        });
    }

    @FXML
    private void handleRenameTracker(Tab tab) {
        // Get the Tracker object associated with the clicked tab
        Tracker tracker = (Tracker) tab.getUserData();

        // Check if the tracker is valid
        if (tracker == null) {
            showAlert("Error", "Tracker not found.");
            return;
        }

        // Create the input dialog for renaming the tracker
        TextInputDialog dialog = new TextInputDialog(tracker.getTrackerName());
        dialog.setTitle("Rename Tracker");
        dialog.setHeaderText("Rename the tracker");
        dialog.setContentText("Enter new tracker name:");

        // Show the dialog and process the result if present
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            // Attempt to rename the tracker in the database and update UI
            try (Connection connection = DBConnection.getConnection()) {
                if (tracker.renameTracker(newName, connection)) {
                    // Update the tracker name in the trackerMap and the tab label
                    tracker.setTrackerName(newName);
                    tab.setText(newName);  // Update the tab label
                } else {
                    showAlert("Error", "Unable to rename tracker.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Database connection error.");
            }
        });
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
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            GridPane.setValignment(dayLabel, VPos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
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
    private void handleDeleteTracker(ActionEvent actionEvent) {
        // Assuming `userId` is available and stores the logged-in user ID
        int currentUserId = userId;

        // Fetch all trackers for the current user from the database
        List<Tracker> userTrackers = Tracker.getTrackersForCurrentUser(currentUserId);

        // Extract tracker names from the list of Tracker objects
        List<String> trackerNames = userTrackers.stream()
                .map(Tracker::getTrackerName)
                .collect(Collectors.toList());

        // Show the dialog to choose which tracker to delete
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, trackerNames);
        dialog.setTitle("Delete Tracker");
        dialog.setHeaderText("Select a tracker to delete");
        dialog.setContentText("Choose tracker:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedName -> {
            // Find the Tracker object corresponding to the selected name
            Tracker trackerToDelete = userTrackers.stream()
                    .filter(tracker -> tracker.getTrackerName().equals(selectedName))
                    .findFirst()
                    .orElse(null);

            if (trackerToDelete != null) {
                // Confirm deletion
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmation");
                confirmAlert.setHeaderText("Are you sure?");
                confirmAlert.setContentText("Do you really want to delete \"" + selectedName + "\"?");

                Optional<ButtonType> confirmation = confirmAlert.showAndWait();
                if (confirmation.isPresent() && confirmation.get() == ButtonType.OK) {
                    // Delete the tracker from the database
                    boolean success = trackerToDelete.deleteTracker();
                    if (success) {
                        // After deletion, clear all tabs
                        tabPane.getTabs().clear();
                        // Reload user trackers and update the UI
                        loadUserTrackers();  // Re-populate the TabPane with the remaining trackers
                    } else {
                        showAlert("Error", "Unable to delete tracker. It might have related changes or issues.");
                    }
                }
            }
        });
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

    public void handleCreateNewTracker(ActionEvent actionEvent) {
        // Show a dialog for the user to input the new tracker name
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Tracker");
        dialog.setHeaderText("Create a new tracker");
        dialog.setContentText("Enter tracker name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(trackerName -> {
            // Create a new Tracker object and add it to the database
            Tracker newTracker = new Tracker(userId, trackerName);  // Create tracker instance
            newTracker.addNewTracker(userId, trackerName);  // Adds the new tracker to the database

            // Get the generated trackerId after insertion
            int trackerId = newTracker.getTrackerId();

            if (trackerId != -1) {  // If the trackerId is valid
                // Add the tracker to the UI (TabPane)
                Tab newTab = new Tab(trackerName);
                trackerMap.put(newTab, newTracker);
                tabPane.getTabs().add(newTab);  // Add the new tracker tab

                // Reload the user trackers to reflect the new one
                loadUserTrackers();
            } else {
                showAlert("Error", "Failed to create new tracker.");
            }
        });
    }

    public enum ButtonState { NORMAL, CHECKED, CROSSED }

    private ButtonState loadButtonState(String dateKey, int trackerId) {
        String sql = "SELECT state FROM user_changes WHERE user_id = ? AND tracker_id = ? AND datelog = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, trackerId);
            pstmt.setString(3, dateKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ButtonState.valueOf(rs.getString("state"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ButtonState.NORMAL;
    }

    private void saveButtonState(String dateKey, ButtonState state, int trackerId) {
        String sql = "INSERT INTO user_changes (user_id, tracker_id, datelog, state) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(user_id, tracker_id, datelog) " +
                "DO UPDATE SET state = excluded.state";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, trackerId);
            pstmt.setString(3, dateKey);
            pstmt.setString(4, state.toString());
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(content);
        alert.showAndWait();
    }

}
