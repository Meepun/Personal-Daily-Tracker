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
    private Button createNewCalendarButton;

    @FXML
    private Button manageCalendarsButton;

    @FXML
    private Button exportToPdfButton;

    @FXML
    private Button exportToExcelButton;

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
    private void handleCreateNewCalendar(ActionEvent event) {
        showAlert("Create New Calendar", "You clicked on 'Create New Calendar'. Implement the logic here.");
    }

    @FXML
    private void handleManageCalendars(ActionEvent event) {
        showAlert("Manage Your Calendars", "You clicked on 'Manage Your Calendars'. Implement the logic here.");
    }

    @FXML
    private void handleExportToPdf(ActionEvent event) {
        showAlert("Export Calendar to PDF", "You clicked on 'Export Calendar to PDF'. Implement the logic here.");
    }

    @FXML
    private void handleExportToExcel(ActionEvent event) {
        showAlert("Export Calendar to Excel", "You clicked on 'Export Calendar to Excel'. Implement the logic here.");
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