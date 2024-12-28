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

public class SignupController {

    public TextField passTextField;
    public TextField conpassTextField;
    public TextField emailTextField;
    public Button signupButton;
    public Button mainmenuButton;

    @FXML
    private ImageView hiLogoNoTextImageView;

    public void initialize() {
        // Load the logo image
        hiLogoNoTextImageView.setImage(new Image(getClass().getResource("/images/Hi Logo No Text.png").toString()));
    }

    @FXML
    private void handleSignupButton(ActionEvent event) {
        // Perform sign-up logic here
        System.out.println("Sign up button clicked. Register user.");
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
