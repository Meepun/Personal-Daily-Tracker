package com.tracker.calendartracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    public Button loginButton;
    public Button mainmenuButton;
    @FXML
    private TextField usernameTextField; // User's username input field
    @FXML
    private PasswordField passTextField; // User's password input field

    private static final Logger LOGGER = Logger.getLogger(LogInController.class.getName());

    @FXML
    private Label errorLabel;

    @FXML
    private ImageView hiLogoImageView;
    @FXML
    private ImageView untitledDesignImageView;
    private String userId;

    public void initialize() {
        // Load the logo image
        hiLogoImageView.setImage(new Image(getClass().getResource("/images/Hi Logo.png").toString()));
        untitledDesignImageView.setImage(new Image(getClass().getResource("/images/Protect.png").toString()));
    }

    /**
     * Handles the login button click event.
     * Validates user credentials and navigates to the main menu on success.
     */
    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameTextField.getText().trim();
        String password = passTextField.getText().trim();

        // Clear any previous error messages
        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            displayErrorMessage("Username and password fields cannot be empty.");
            return;
        }

        // vinavalidate the credentials checking against the database
        if (validateCredentials(username, password)) {
            System.out.println("Login successful!");
            // Transition to HomeController
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tracker/calendartracker/home.fxml"));
                    Parent root = loader.load();

                    HomeController homeController = loader.getController();
                    homeController.setUserId(getUserId());  // Pass userId to HomeController
                    homeController.updateWelcomeUser(username);  // Pass username to home screen

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Home");
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        } else {
            displayErrorMessage("Invalid username or password. Please try again.");
        }
    }

    /**
     * Validates the user's credentials against the database.
     *
     * @param username    User's username input
     * @param password User's password input
     * @return True if credentials are valid, false otherwise
     */
    public boolean validateCredentials(String username, String password) {
        String query = "SELECT * FROM loginsignup WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userId = rs.getString("user_id");  // Store user_id
                return true;
            }
        } catch (SQLException e) {
            LOGGER.severe("Database error during login validation: " + e.getMessage());
        }
        return false;
    }

    public String getUserId() {
        return userId;
    }

    private void displayErrorMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
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
