package com.tracker.calendartracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmedPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            String sql = "INSERT INTO loginsignup (username, password) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 1) {
                showAlert("Success", "User registered successfully!");
                // Optionally, navigate to a different scene after successful registration
            } else {
                showAlert("Error", "Failed to register user.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}