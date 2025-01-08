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
    private TextField usernameTextField;
    @FXML
    private PasswordField passTextField;

    private static final Logger LOGGER = Logger.getLogger(LogInController.class.getName());

    @FXML
    private Label errorLabel;

    @FXML
    private ImageView hiLogoImageView;
    @FXML
    private ImageView untitledDesignImageView;
    private int userId;

    public void initialize() {
        hiLogoImageView.setImage(new Image(getClass().getResource("/images/Hi Logo.png").toString()));
        untitledDesignImageView.setImage(new Image(getClass().getResource("/images/Protect.png").toString()));
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameTextField.getText().trim();
        String password = passTextField.getText().trim();

        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            displayErrorMessage("Username and password fields cannot be empty.");
            return;
        }

        if (validateCredentials(username, password)) {
            System.out.println("Login successful!");

            SessionHandler.getInstance().setUserId(userId);

            navigateTo(event, "/com/tracker/calendartracker/Home.fxml", "Home");
        } else {
            displayErrorMessage("Invalid username or password. Please try again.");
        }
    }

    public boolean validateCredentials(String username, String password) {
        String query = "SELECT * FROM loginsignup WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userId = Integer.parseInt(rs.getString("user_id"));

                checkIfUserHasTracker(String.valueOf(userId));

                return true;
            }
        } catch (SQLException e) {
            LOGGER.severe("Database error during login validation: " + e.getMessage());
        }
        return false;
    }

    private void checkIfUserHasTracker(String userId) {
        String trackerQuery = "SELECT COUNT(*) FROM trackers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(trackerQuery)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                createFirstTracker(conn, userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createFirstTracker(Connection connection, String userId) {
        String insertTrackerQuery = "INSERT INTO trackers (user_id, tracker_name) VALUES (?, ?)";
        try (PreparedStatement psTracker = connection.prepareStatement(insertTrackerQuery)) {
            psTracker.setString(1, userId);
            psTracker.setString(2, "My First Tracker");
            psTracker.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public int getUserId() {

        return userId;
    }

    private void displayErrorMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleMainMenuButton(ActionEvent event) {
        navigateTo(event, "/com/tracker/calendartracker/mainmenu.fxml", "Main Menu");
    }

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
