package com.tracker.calendartracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CalendarTrackerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainmenu.fxml"));
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root)); // Set the scene on the stage
        primaryStage.setTitle("Calendar Tracker"); // Set the title of the window
        primaryStage.show(); // Show the stage
    }

    public static void main(String[] args) {
        launch(args);
    }
}