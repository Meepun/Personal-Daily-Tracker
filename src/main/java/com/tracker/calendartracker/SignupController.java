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
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupController {

    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passTextField;
    @FXML
    private TextField conpassTextField;
    @FXML
    private Button signupButton;
    @FXML
    private Button mainmenuButton;
    @FXML
    private ImageView hiLogoNoTextImageView;
    @FXML
    private Label errorLabel; // Add the error label for displaying error messages

    public void initialize() {
        // Load the logo image
        hiLogoNoTextImageView.setImage(new Image(getClass().getResource("/images/Hi Logo No Text.png").toString()));
    }

    @FXML
    private void handleSignupButton(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = passTextField.getText();
        String confirmedPassword = conpassTextField.getText();

        // Basic input validation
        if (username.isEmpty() || password.isEmpty() || confirmedPassword.isEmpty()) {
            displayErrorMessage("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmedPassword)) {
            displayErrorMessage("Passwords do not match.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            String sql = "INSERT INTO loginsignup (username, password) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 1) {
                navigateToHome(event); // Navigate to Home.fxml
            } else {
                displayErrorMessage("Failed to register user.");
            }
        } catch (SQLException e) {
            displayErrorMessage("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMainMenuButton(ActionEvent event) {
        navigateTo(event, "mainmenu.fxml", "Main Menu");
    }

    /**
     * Displays an error message in the error label.
     *
     * @param message The error message to be displayed.
     */
    private void displayErrorMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void navigateToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tracker/calendartracker/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
