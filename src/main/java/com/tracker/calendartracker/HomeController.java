package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

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

    // Enum to represent the different states of a button (Normal, Checked, Crossed)
    private enum ButtonState {
        NORMAL,
        CHECKED,
        CROSSED
    }

    @FXML
    public void initialize() {
        // Initialize currentMonth to the current date
        currentMonth = today.withDayOfMonth(1);

        // Populate the ListView with months
        monthListView.setItems(FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
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

        // Add the row for the days of the week (Sun, Mon, Tue, etc.)
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            calendarGrid.add(dayLabel, i, 0);  // Add labels in the first row
        }

        // Update the month label
        monthLabel.setText(month + " " + currentMonth.getYear());

        // Get the first day of the month
        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), Month.valueOf(month.toUpperCase()), 1);

        // Adjust the first day of the week to Sunday as the first day
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        if (firstDayOfWeek == 7) {
            firstDayOfWeek = 0;  // If it's Sunday (which is 7), set it to 0 to align with Sunday as the first day
        }

        int lengthOfMonth = firstOfMonth.lengthOfMonth(); // Get the number of days in the month

        // Create the grid with buttons for each day
        for (int day = 1; day <= lengthOfMonth; day++) {
            int row = (firstDayOfWeek + day - 1) / 7 + 1; // Calculate the row (starting from 1 for the second row)
            int col = (firstDayOfWeek + day - 1) % 7; // Calculate the column

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(50, 50);

            // Set the initial state to NORMAL
            dayButton.setUserData(ButtonState.NORMAL);

            dayButton.setOnAction(e -> handleDayClick(dayButton));

            // Add the button to the grid
            calendarGrid.add(dayButton, col, row);
            dayButtons.add(dayButton);
        }
    }

    private void handleDayClick(Button dayButton) {
        // Get the current state of the button
        ButtonState currentState = (ButtonState) dayButton.getUserData();

        // Cycle through the states: NORMAL -> CHECKED -> CROSSED -> NORMAL
        ButtonState nextState;
        switch (currentState) {
            case NORMAL:
                nextState = ButtonState.CHECKED;
                dayButton.setStyle("-fx-background-color: green;"); // Green (checked)
                break;
            case CHECKED:
                nextState = ButtonState.CROSSED;
                dayButton.setStyle("-fx-background-color: red;"); // Red (crossed)
                break;
            case CROSSED:
            default:
                nextState = ButtonState.NORMAL;
                dayButton.setStyle("-fx-background-color: transparent;"); // Transparent (normal)
                break;
        }

        // Set the new state for the button
        dayButton.setUserData(nextState);
    }

    // Handle clicking the "Previous Month" button
    @FXML
    private void handlePreviousMonth(ActionEvent event) {
        currentMonth = currentMonth.minusMonths(1);
        updateCalendar(currentMonth.getMonth().toString());
    }

    // Handle clicking the "Next Month" button
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

    // Method for creating new tracker tabs (not implemented fully yet)
    public Tab createNewTracker() {
        Tab newTab = new Tab("New Tracker");
        // Code for creating new tab
        // You can add a Label, TextField, or any other controls to the tab here
        Label label = new Label("New Tracker Content");
        newTab.setContent(label);
        return newTab;
    }

    // Event handler for month selection
    @FXML
    public void handleMonthSelection(MouseEvent event) {
        String selectedMonth = monthListView.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            updateCalendar(selectedMonth);
        }
    }
}
