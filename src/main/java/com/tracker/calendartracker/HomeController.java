package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.time.*;

public class HomeController {

    // for creating new tracker/tab. will fix later. missing return statement.
    public Button createNewTracker(ActionEvent event) {
        Tab newTab = new Tab("New Tracker");
        AnchorPane anchorPane = new AnchorPane();
        GridPane newCalendarGrid = new GridPane();
        newCalendarGrid.setHgap(10);
        newCalendarGrid.setVgap(10);
        newCalendarGrid.getStyleClass().add("calendar-grid");
    }

    public SplitPane splitPane;

    private LocalDate today = LocalDate.now();
    @FXML
    private Button createNewTrackerButton;

    @FXML
    private Label monthLabel;

    @FXML
    private ListView<String> monthListView;

    @FXML
    public void initialize() {
        // Populate the ListView with months
        monthListView.setItems(FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));

        // Add listener to handle month selection changes
        monthListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateMonthLabel(newValue);
            }
        });
    }

    @FXML
    private void handleCreateNewTracker(ActionEvent event) {
        showAlert("Create New Tracker", "You clicked on 'Create New Tracker'. Implement the logic here.");
    }

private void updateMonthLabel(String month) {
        monthLabel.setText(month);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}