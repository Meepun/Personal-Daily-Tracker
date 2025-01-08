package com.tracker.calendartracker;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class MainMenuController {

    public Button signupButton;
    public Button loginButton;

    @FXML
    private ImageView hiLogoImageView;

    public void initialize() {
        hiLogoImageView.setImage(new Image(getClass().getResource("/images/Hi Logo.png").toString()));
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        navigateTo(event, "LogIn.fxml", "Log In");
    }

    @FXML
    private void handleSignupButton(ActionEvent event) {
        navigateTo(event, "register.fxml", "Sign Up");
    }

    private void navigateTo(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tracker/calendartracker/" + fxmlFile));
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
