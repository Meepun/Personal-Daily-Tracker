package javaprogram.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SignupController {

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javaprogram/demo/register.fxml"));
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
