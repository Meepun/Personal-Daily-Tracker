package javaprogram.demo;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize scenes
        Scene loginScene = createLoginScene(primaryStage);
        Scene signupScene = createSignupScene(primaryStage);

        // Set the initial scene
        primaryStage.setTitle("Login and Signup");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private Scene createLoginScene(Stage primaryStage) {
        // UI element para sa UI, wala pa to design
        Label lblLogin = new Label("Login");
        TextField tfUsername = new TextField();
        tfUsername.setPromptText("Username");
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("Password");
        Button btnLogin = new Button("Login");
        Button btnGoToSignup = new Button("Go to Signup");

        VBox loginLayout = new VBox(10, lblLogin, tfUsername, pfPassword, btnLogin, btnGoToSignup);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(20));

        // para magswitch to signup scene
        btnGoToSignup.setOnAction(e -> primaryStage.setScene(createSignupScene(primaryStage)));

        return new Scene(loginLayout, 300, 200);
    }

    private Scene createSignupScene(Stage primaryStage) {
        // Create UI elements for Signup
        Label lblSignup = new Label("Signup");
        TextField tfNewUsername = new TextField();
        tfNewUsername.setPromptText("Username");
        PasswordField pfNewPassword = new PasswordField();
        pfNewPassword.setPromptText("Password");
        PasswordField pfConfirmPassword = new PasswordField();
        pfConfirmPassword.setPromptText("Confirm Password");
        Button btnSignup = new Button("Signup");
        Button btnGoToLogin = new Button("Go to Login");

        VBox signupLayout = new VBox(10, lblSignup, tfNewUsername, pfNewPassword, pfConfirmPassword, btnSignup, btnGoToLogin);
        signupLayout.setAlignment(Pos.CENTER);
        signupLayout.setPadding(new Insets(20));

        // Event to switch to login scene
        btnGoToLogin.setOnAction(e -> primaryStage.setScene(createLoginScene(primaryStage)));

        return new Scene(signupLayout, 300, 250);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
