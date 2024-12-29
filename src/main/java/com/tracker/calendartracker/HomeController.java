package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

public class HomeController {

    @FXML
    private Button createNewTrackerButton;

    @FXML
    private Label monthLabel;

    @FXML
    private ListView<String> monthListView;

    private GridPane calendarGrid;

    private int year = 2025; // Start year

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