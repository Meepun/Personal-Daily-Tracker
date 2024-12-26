package javaprogram.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LogInController {

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javaprogram/demo/LogIn.fxml"));
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
