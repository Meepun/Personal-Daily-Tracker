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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {

    @FXML
    private SplitPane splitPane;

    private LocalDate today = LocalDate.now(); // Get the current date

    @FXML
    private Label monthLabel;

    @FXML
    private ListView<String> monthListView;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button createNewTrackerButton; // Added the button for creating a new tracker

    private List<Button> dayButtons = new ArrayList<>();
    private LocalDate currentMonth; // This will track the currently displayed month

    private String userId = "exampleUserId"; // This should be dynamically obtained from login

    // Enum to represent the different states of a button (Normal, Checked, Crossed)
    private enum ButtonState {
        NORMAL,
        CHECKED,
        CROSSED
    }

    // Map to store button states per user and date
    private static final Map<String, Map<String, ButtonState>> userChanges = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize currentMonth to the current date
        currentMonth = today.withDayOfMonth(1);

        // Populate the ListView with months
        monthListView.setItems(FXCollections.observableArrayList(
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"

        ));

        // Add listener to handle month selection changes
        monthListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateCalendar(newValue);
            }
        });

        // Initialize the calendar with the current month
        updateCalendar(currentMonth.getMonth().toString());

        // Handle create new tracker button
        createNewTrackerButton.setOnAction(this::handleCreateNewTracker);
    }

    private void updateCalendar(String month) {
        // Clear the previous grid
        calendarGrid.getChildren().clear();
        dayButtons.clear();

        // Add the row for the days of the week (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            calendarGrid.add(dayLabel, i, 0);  // Add labels in the first row
        }

        // Update the month label
        monthLabel.setText(month + " " + currentMonth.getYear());

        // Get the first day of the month
        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), Month.valueOf(month.toUpperCase()), 1);

        // Adjust the first day of the week to Sunday as the first day
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        if (firstDayOfWeek == 7) {
            firstDayOfWeek = 0;
        }

        int lengthOfMonth = firstOfMonth.lengthOfMonth(); // Get the number of days in the month

        // Create the grid with buttons for each day
        for (int day = 1; day <= lengthOfMonth; day++) {
            int row = (firstDayOfWeek + day - 1) / 7 + 1; // Calculate the row (starting from 1 for the second row)
            int col = (firstDayOfWeek + day - 1) % 7; // Calculate the column

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45);

            // Retrieve state from userChanges
            String key = month + "-" + day;
            ButtonState state = loadButtonState(key);
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);

            dayButton.setOnAction(e -> handleDayClick(dayButton, key));

            // Add the button to the grid
            calendarGrid.add(dayButton, col, row);
            dayButtons.add(dayButton);
        }
    }

    private void handleDayClick(Button dayButton, String key) {
        // Get the current state of the button
        ButtonState currentState = (ButtonState) dayButton.getUserData();

        // Cycle through the states: NORMAL -> CHECKED -> CROSSED -> NORMAL
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

        // Update state and apply style
        dayButton.setUserData(nextState);
        applyButtonState(dayButton, nextState);

        // Save changes for user
        saveButtonState(key, nextState);
    }

    private void saveButtonState(String key, ButtonState state) {
        userChanges.computeIfAbsent(userId, k -> new HashMap<>()).put(key, state);
    }

    private ButtonState loadButtonState(String key) {
        return userChanges.getOrDefault(userId, new HashMap<>()).getOrDefault(key, ButtonState.NORMAL);
    }

    private void applyButtonState(Button button, ButtonState state) {
        switch (state) {
            case CHECKED:
                button.setStyle("-fx-background-color: green;"); // Green (checked)
                break;
            case CROSSED:
                button.setStyle("-fx-background-color: red;"); // Red (crossed)
                break;
            case NORMAL:
            default:
                button.setStyle("-fx-background-color: transparent;"); // Transparent (normal)
                break;
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

    @FXML
    private void handleCreateNewTracker(ActionEvent event) {
        Tab newTab = createNewTracker();
        tabPane.getTabs().add(newTab); // Add the new tab to the TabPane
    }

    public Tab createNewTracker() {
        Tab newTab = new Tab("New Tracker");
        Label label = new Label("New Tracker Content");
        newTab.setContent(label);
        return newTab;
    }

    // ETO UNG GINAWA NI RJ NA INUPDATE KO FOR RETURN STATEMENT PARA MARUN
    // pinaltan ko lng pero experiment lng so retain ko toh as comment just in case
    /*
    // for creating new tracker/tab. will fix later. missing return statement.
    public Tab createNewTracker(ActionEvent event) {
        Tab newTab = new Tab("New Tracker");

        // Create content for the tab
        AnchorPane anchorPane = new AnchorPane();
        GridPane newCalendarGrid = new GridPane();
        newCalendarGrid.setHgap(10);
        newCalendarGrid.setVgap(10);
        newCalendarGrid.getStyleClass().add("calendar-grid");

        // mema ko lng toh para may something sa new tab
        Button newButton = new Button("Click Me");
        newButton.setOnAction(e -> showAlert("Button Clicked", "You clicked the button in the new tracker tab!"));
        newCalendarGrid.add(newButton, 0, 0);

        anchorPane.getChildren().add(newCalendarGrid);
        newTab.setContent(anchorPane);

        return newTab;
    }
     */
    @FXML
    public void handleMonthSelection(MouseEvent event) {
        String selectedMonth = monthListView.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            updateCalendar(selectedMonth);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
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
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
