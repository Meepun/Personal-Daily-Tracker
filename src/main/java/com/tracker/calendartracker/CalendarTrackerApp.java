package com.tracker.calendartracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CalendarTrackerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainmenu.fxml"));
        Parent root = loader.load();

        // Set scene with dynamic size
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Calendar Tracker");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}