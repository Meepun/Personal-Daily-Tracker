package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class HomeController {

    @FXML private ComboBox<Integer> yearDropdown;
    @FXML private ListView<String> monthListView;
    @FXML private TabPane tabPane;
    @FXML private Button createNewTrackerButton;
    @FXML public  Button deleteTrackerButton;
    @FXML private Label welcomeLabel;

    private int userId;
    private LocalDate currentMonth = LocalDate.now();
    private LocalDate today = LocalDate.now();
    private Map<Tab, Tracker> trackerMap = new HashMap<>();
    @FXML
    private Label monthLabel = new Label();

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

        // Apply styling to the ListView
        monthListView.setStyle(
                "-fx-background-color: orange;" +        // Set background color for the list
                        "-fx-text-fill: white;" +               // Set text color
                        "-fx-font-weight: bold;" +              // Make text bold
                        "-fx-selection-bar: orange;" +          // Set selected item's background color
                        "-fx-selection-bar-text: white;"        // Set selected item's text color
        );

        // Add listener to handle month selection changes
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
        List<String> months = Arrays.asList("JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER");
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
            if (tracker != null) {
                addTrackerTab(tracker);
            } else {
                System.out.println("Warning: Tracker is null. Skipping.");
            }
        }
    }

    private void addTrackerTab(Tracker tracker) {
        // Create a new tab for the tracker
        Tab tab = new Tab(tracker.getTrackerName());
        tab.setUserData(tracker); // Associate tracker with tab

        // Initialize and set the month label
        monthLabel.setText(tracker.getCurrentMonth().getMonth().name() + " " + tracker.getCurrentMonth().getYear());

        // Create an HBox for the navigation bar and center-align the month label
        HBox tabNavBar = new HBox(monthLabel);
        tabNavBar.setAlignment(Pos.CENTER); // Center align the label
        tabNavBar.setAlignment(Pos.CENTER); // Center align the label


        // Create calendar content
        AnchorPane calendarContent = createCalendarContent(tracker);

        // Create a container for navbar and calendar
        AnchorPane tabContent = new AnchorPane();
        tabContent.getChildren().addAll(tabNavBar, calendarContent);

        // Position calendar content below the nav bar
        AnchorPane.setTopAnchor(tabNavBar, 0.0);
        AnchorPane.setLeftAnchor(tabNavBar, 0.0);
        AnchorPane.setRightAnchor(tabNavBar, 0.0); // Ensure the nav bar spans the width
        AnchorPane.setTopAnchor(calendarContent, 50.0);

        // Set tab content and add tab to the TabPane
        tab.setContent(tabContent);
        tabPane.getTabs().add(tab);
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

    private AnchorPane createCalendarContent(Tracker tracker) {
        AnchorPane calendarPane = new AnchorPane();

        // Check if tracker is null
        if (tracker == null) {
            System.err.println("Error: Tracker is null. Unable to create calendar content.");
            return calendarPane; // Return an empty calendar pane or handle the error as needed
        }

        // Create a GridPane for the calendar
        GridPane calendarGrid = new GridPane();
        calendarGrid.setLayoutX(3.0);
        calendarGrid.setLayoutY(-18.0);
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.getStyleClass().add("calendar-grid");
        calendarGrid.setPrefSize(525.0, 430.0);
        calendarGrid.setAlignment(Pos.TOP_CENTER);

        // Add the row for the days of the week (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            GridPane.setValignment(dayLabel, VPos.TOP);
            calendarGrid.add(dayLabel, i, 0);
        }

        // Get the first day of the month
        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), 1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        if (firstDayOfWeek == 7) { // If Sunday is the first day
            firstDayOfWeek = 0; // Adjust for Sunday
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

            // Check if trackerId is valid before loading state
            ButtonState state = loadButtonState(key, tracker.getTrackerId());
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);  // Apply the visual state (checked, crossed, etc.)

            // Set action for the button (to handle clicks)
            dayButton.setOnAction(e -> handleDayClick(dayButton, key, tracker));

            // Add the button to the grid at the appropriate position
            calendarGrid.add(dayButton, col, row);
        }

        // Add the calendar grid to the calendar pane
        calendarPane.getChildren().addAll(calendarGrid);
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

    private void updateCalendar(String month) {
        // Update the text of the monthLabel
        monthLabel.setText(month + " " + currentMonth.getYear());

        // Get the currently selected tab
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab != null) {
            // Get the tracker associated with this tab
            Tracker tracker = (Tracker) selectedTab.getUserData();

            // Rebuild calendar content
            AnchorPane newCalendarContent = createCalendarContent(tracker);
            AnchorPane existingTabContent = (AnchorPane) selectedTab.getContent();

            // Clear the old content but keep the navbar
            existingTabContent.getChildren().clear();
            existingTabContent.getChildren().addAll(monthLabel, newCalendarContent);

            // Reposition content
            AnchorPane.setTopAnchor(newCalendarContent, 50.0);
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
                "ON CONFLICT(tracker_id, datelog) " + // Conflict resolution on tracker_id and datelog
                "DO UPDATE SET state = excluded.state"; // Using ON CONFLICT to update state

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);           // Set the userId
            pstmt.setInt(2, trackerId);        // Set the trackerId
            pstmt.setString(3, dateKey);       // Set the dateKey (e.g., "2025-JAN-15")
            pstmt.setString(4, state.toString());  // Set the button state (e.g., "CHECKED")

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

    @FXML
    private void handleLogout(ActionEvent event) {
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");

        // Style the dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #C37B1EFF; -fx-font-size: 14px; -fx-text-fill: white;");
        dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));

        // Add Yes and No buttons
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Center-align the buttons
        Node buttonBar = dialogPane.lookup(".button-bar");
        if (buttonBar instanceof HBox) {
            ((HBox) buttonBar).setAlignment(Pos.CENTER); // Center the buttons
            ((HBox) buttonBar).setSpacing(10);          // Add spacing between buttons
        }

        // Wait for the user's response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            // Proceed with logout

            // Clear session data
            SessionHandler.getInstance().clearSession();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tracker/calendartracker/mainmenu.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Main Menu");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Do nothing, stay on the current page
            alert.close();
        }
    }

}
