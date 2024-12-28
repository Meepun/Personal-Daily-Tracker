package com.tracker.calendartracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class LogInController {

    // FXML variables for the TextFields and Buttons
    public TextField emailTextField;
    public TextField passTextField;
    public Button loginButton;
    public Button mainmenuButton;

    // FXML ImageView variables for displaying images
    @FXML
    private ImageView hiLogoImageView;
    @FXML
    private ImageView untitledDesignImageView;

    public void initialize() {
        // Load the images into ImageViews
        hiLogoImageView.setImage(new Image(getClass().getResource("/images/Hi Logo.png").toString()));
        untitledDesignImageView.setImage(new Image(getClass().getResource("/images/Untitled design.png").toString()));
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        // Perform login validation here
        System.out.println("Login button clicked. Validate credentials.");
    }

    @FXML
    private void handleMainMenuButton(ActionEvent event) {
        navigateTo(event, "mainmenu.fxml", "Main Menu");
    }

    private void navigateTo(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tracker/calendartracker/mainmenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
