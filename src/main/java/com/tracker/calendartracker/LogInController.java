package com.tracker.calendartracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class LogInController {

    @FXML
    private TextField emailTextField; // User's email input field
    @FXML
    private PasswordField passTextField; // User's password input field

    private static final Logger LOGGER = Logger.getLogger(LogInController.class.getName());

    @FXML
    private ImageView hiLogoImageView;
    @FXML
    private ImageView untitledDesignImageView;

    public void initialize() {
        // Load the logo image
        hiLogoImageView.setImage(new Image(getClass().getResource("/images/Hi Logo.png").toString()));
        untitledDesignImageView.setImage(new Image(getClass().getResource("/images/Untitled design.png").toString()));
    }

    /**
     * Handles the login button click event.
     * Validates user credentials and navigates to the main menu on success.
     */
    @FXML
    private void handleLoginButton(ActionEvent event) {
        String email = emailTextField.getText().trim();
        String password = passTextField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Email and password fields cannot be empty.");
            return;
        }

        // vinavalidate the credentials checking against the database
        if (validateCredentials(email, password)) {
            System.out.println("Login successful!");
            showAlert("Login Success", "You have logged in successfully!");

            // Navigate to the main menu
            navigateTo(event, "/com/tracker/calendartracker/mainmenu.fxml", "Main Menu");
        } else {
            showAlert("Login Failed", "Invalid email or password. Please try again.");
        }
    }

    /**
     * Validates the user's credentials against the database.
     *
     * @param email    User's email input
     * @param password User's password input
     * @return True if credentials are valid, false otherwise
     */
    private boolean validateCredentials(String email, String password) {
        String query = "SELECT * FROM loginsignup WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            LOGGER.severe("Database error during login validation: " + e.getMessage());
            showAlert("Database Error", "Could not connect to the database.");
        }
        return false;
    }

    /**
     * Displays an alert with the specified title and message.
     *
     * @param title   Alert title
     * @param message Alert message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the Main Menu button click event.
     * Navigates to the main menu screen.
     */
    @FXML
    private void handleMainMenuButton(ActionEvent event) {
        navigateTo(event, "/com/tracker/calendartracker/mainmenu.fxml", "Main Menu");
    }

    /**
     * Navigates to the specified FXML file and updates the stage title.
     *
     * @param event    The action event that triggered the navigation
     * @param fxmlFile Path to the FXML file
     * @param title    Window title for the new scene
     */
    private void navigateTo(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
