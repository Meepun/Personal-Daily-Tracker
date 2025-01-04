package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {

    @FXML
    private SplitPane splitPane;

    private LocalDate today = LocalDate.now();

    @FXML
    private Label welcomeLabel;  // Reference to the label in the FXML

    @FXML
    private Label monthLabel;

    @FXML
    private ListView<String> monthListView;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button createNewTrackerButton;

    private List<Button> dayButtons = new ArrayList<>();
    private LocalDate currentMonth;

    private String userId;
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId() {
        return userId;
    }

    private enum ButtonState {
        NORMAL,
        CHECKED,
        CROSSED
    }

    private static final Map<String, Map<String, ButtonState>> userChanges = new HashMap<>();

    private static final String BASE_PATH = "/images/";
    private static final String CHECKED_IMAGE_PATH = "Check.png";
    private static final String CROSSED_IMAGE_PATH = "X.png";

    @FXML
    public void initialize() {
        // Retrieve userId from the session
        this.userId = SessionHandler.getInstance().getUserId();

        // Retrieve the username from your database or session
        String username = getUserNameFromDatabase(userId);  // Implement this method as per your logic

        // Set the label text dynamically
        welcomeLabel.setText("Welcome, " + username);

        currentMonth = today.withDayOfMonth(1);

        monthListView.setItems(FXCollections.observableArrayList(
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        ));

        // Add listener to handle month selection changes
        monthListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateCalendar(newValue);
            }
        });

        // Initialize the calendar with the current month
        updateCalendar(currentMonth.getMonth().toString());

        // Handle create new tracker button
        createNewTrackerButton.setOnAction(this::handleCreateNewTracker);
    }


    private void updateCalendar(String month) {
        // Clear the previous grid
        calendarGrid.getChildren().clear();
        dayButtons.clear();

        // Add the row for the days of the week (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            calendarGrid.add(dayLabel, i, 0);  // Add labels in the first row
        }

        monthLabel.setText(month + " " + currentMonth.getYear());

        // Get the first day of the month
        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), Month.valueOf(month.toUpperCase()), 1);

        // Adjust the first day of the week to Sunday as the first day
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        if (firstDayOfWeek == 7) {
            firstDayOfWeek = 0;
        }

        int lengthOfMonth = firstOfMonth.lengthOfMonth();

        for (int day = 1; day <= lengthOfMonth; day++) {
            int row = (firstDayOfWeek + day - 1) / 7 + 1;
            int col = (firstDayOfWeek + day - 1) % 7;

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45);
            dayButton.setId(String.valueOf(day)); // Assign the day as the button ID

            String key = month + "-" + day;
            ButtonState state = loadButtonState(key);
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);

            dayButton.setOnAction(e -> handleDayClick(dayButton, key));
            calendarGrid.add(dayButton, col, row);
            dayButtons.add(dayButton);
        }
    }

    private void handleDayClick(Button dayButton, String key) {
        ButtonState currentState = (ButtonState) dayButton.getUserData();

        ButtonState nextState;
        switch (currentState) {
            case NORMAL:
                nextState = ButtonState.CHECKED;
                break;
            case CHECKED:
                nextState = ButtonState.CROSSED;
                break;
            case CROSSED:
            default:
                nextState = ButtonState.NORMAL;
                break;
        }

        dayButton.setUserData(nextState);
        applyButtonState(dayButton, nextState);
        saveButtonState(key, nextState);
    }

    private void saveButtonState(String dateKey, ButtonState state) {
        String sql = "INSERT INTO user_changes (user_id, datelog, state) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT(user_id, datelog) " +  // Ensure conflict on user_id + datelog
                "DO UPDATE SET state = excluded.state";  // Update the state if conflict occurs

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);  // Use the dynamically set userId
            pstmt.setString(2, dateKey);  // Date key (e.g., "JANUARY-1")
            pstmt.setString(3, state.toString()); // Save the state as a string
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private ButtonState loadButtonState(String dateKey) {
        String sql = "SELECT state FROM user_changes WHERE user_id = ? AND datelog = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, dateKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ButtonState.valueOf(rs.getString("state"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ButtonState.NORMAL; // Default to NORMAL if no state is found
    }

    private void applyButtonState(Button button, ButtonState state) {
        double imageSize = 20; // Adjust size as needed for your design

        switch (state) {
            case CHECKED:
                ImageView checkedImageView = new ImageView(new Image(getClass().getResourceAsStream(BASE_PATH + CHECKED_IMAGE_PATH)));
                checkedImageView.setFitWidth(imageSize);
                checkedImageView.setFitHeight(imageSize);
                checkedImageView.setPreserveRatio(true);
                button.setGraphic(checkedImageView);
                button.setText(""); // Temporarily remove text
                break;
            case CROSSED:
                ImageView crossedImageView = new ImageView(new Image(getClass().getResourceAsStream(BASE_PATH + CROSSED_IMAGE_PATH)));
                crossedImageView.setFitWidth(imageSize);
                crossedImageView.setFitHeight(imageSize);
                crossedImageView.setPreserveRatio(true);
                button.setGraphic(crossedImageView);
                button.setText(""); // Temporarily remove text
                break;
            case NORMAL:
            default:
                button.setGraphic(null);
                button.setText(button.getId()); // Restore the number as text
                break;
        }
    }

    @FXML
    private void handlePreviousMonth(ActionEvent event) {
        currentMonth = currentMonth.minusMonths(1);
        updateCalendar(currentMonth.getMonth().toString());
    }

    @FXML
    private void handleNextMonth(ActionEvent event) {
        currentMonth = currentMonth.plusMonths(1);
        updateCalendar(currentMonth.getMonth().toString());
    }

    @FXML
    private void handleCreateNewTracker(ActionEvent event) {
        Tab newTab = createNewTracker();
        tabPane.getTabs().add(newTab);
    }

    public Tab createNewTracker() {
        Tab newTab = new Tab("New Tracker");

        // Create root layout for the tab content
        BorderPane borderPane = new BorderPane();
        GridPane newCalendarGrid = new GridPane();
        newCalendarGrid.setHgap(10);
        newCalendarGrid.setVgap(10);
        newCalendarGrid.getStyleClass().add("calendar-grid");

        Label monthLabel = new Label();
        monthLabel.getStyleClass().add("month-label");

        HBox navigationBox = new HBox(10);
        Button prevMonthButton = new Button("<");
        Button nextMonthButton = new Button(">");

        // Set default tab calendar to the current month
        var ref = new Object() {
            LocalDate tabMonth = LocalDate.now().withDayOfMonth(1);
        };

        // Generate initial calendar view for the tab
        generateCalendar(newCalendarGrid, ref.tabMonth, monthLabel);

        // Handle previous/next button clicks to update the calendar
        prevMonthButton.setOnAction(e -> {
            ref.tabMonth = ref.tabMonth.minusMonths(1);
            generateCalendar(newCalendarGrid, ref.tabMonth, monthLabel);
        });

        nextMonthButton.setOnAction(e -> {
            ref.tabMonth = ref.tabMonth.plusMonths(1);
            generateCalendar(newCalendarGrid, ref.tabMonth, monthLabel);
        });

        navigationBox.getChildren().addAll(prevMonthButton, monthLabel, nextMonthButton);
        navigationBox.setAlignment(javafx.geometry.Pos.CENTER);

        borderPane.setTop(navigationBox);
        borderPane.setCenter(newCalendarGrid);
        newTab.setContent(borderPane);

        return newTab;
    }

    private void generateCalendar(GridPane grid, LocalDate currentMonth, Label monthLabel) {
        grid.getChildren().clear();

        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            grid.add(dayLabel, i, 0);  // First row: Days of the week
        }

        monthLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());

        LocalDate firstOfMonth = currentMonth.withDayOfMonth(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        if (firstDayOfWeek == 7) {
            firstDayOfWeek = 0;  // Sunday = 0
        }

        int lengthOfMonth = firstOfMonth.lengthOfMonth();

        for (int day = 1; day <= lengthOfMonth; day++) {
            int row = (firstDayOfWeek + day - 1) / 7 + 1;
            int col = (firstDayOfWeek + day - 1) % 7;

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45);
            dayButton.setId(String.valueOf(day));

            String key = currentMonth.getMonth().toString() + "-" + day;
            ButtonState state = loadButtonState(key);
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);

            dayButton.setOnAction(e -> handleDayClick(dayButton, key));
            grid.add(dayButton, col, row);
        }
    }


    @FXML
    public void handleMonthSelection(MouseEvent event) {
        String selectedMonth = monthListView.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            updateCalendar(selectedMonth);
        }
    }

    private String getUserNameFromDatabase(String userId) {
        String sql = "SELECT username FROM loginsignup WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "User"; // Fallback value if username is not found
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tracker/calendartracker/mainmenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Menu");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}