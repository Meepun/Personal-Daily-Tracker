package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

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
    private ComboBox<Integer> yearDropdown; // Added ComboBox for year selection

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

        // Initialize the year dropdown
        initializeYearDropdown();

        // Add hover effect on the monthLabel
        monthLabel.setOnMouseEntered(e -> handleMonthLabelHover(true));
        monthLabel.setOnMouseExited(e -> handleMonthLabelHover(false));

        // Make monthLabel clickable for year navigation
        monthLabel.setOnMouseClicked(this::handleMonthLabelClick);
    }

    private void initializeYearDropdown() {
        // Get the current year
        int currentYear = today.getYear();
        // Create a list of years (10 years before and after current)
        List<Integer> years = new ArrayList<>();
        for (int year = currentYear - 25; year <= currentYear + 50; year++) {
            years.add(year);
        }
        // Populate the ComboBox with years
        yearDropdown.setItems(FXCollections.observableArrayList(years));
        yearDropdown.setValue(currentYear); // Set the current year as selected
        // Add listener for year selection
        yearDropdown.setOnAction(this::handleYearSelection);
    }
    private void handleYearSelection(ActionEvent event) {
        // Get selected year from dropdown
        Integer selectedYear = yearDropdown.getValue();
        // Update currentMonth and refresh calendar view
        currentMonth = currentMonth.withYear(selectedYear);
        updateCalendar(currentMonth.getMonth().toString()); // Refresh calendar to display the selected year
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
            GridPane.setHalignment(dayLabel, HPos.CENTER); // Center horizontally
            GridPane.setValignment(dayLabel, VPos.CENTER);
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

            // Include the year in the key
            String key = currentMonth.getYear() + "-" + month + "-" + day;
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
            pstmt.setString(2, dateKey);  // Date key (e.g., "2025-JANUARY-1")
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
            pstmt.setString(2, dateKey); // Use the full date key including year
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

    // Hover effect for monthLabel (change only when hovering over the year portion)
    private void handleMonthLabelHover(boolean isHovered) {
        String[] parts = monthLabel.getText().split(" ");
        if (parts.length > 1) {
            String month = parts[0];  // e.g., "January"
            String year = parts[1];   // e.g., "2025"
            if (isHovered) {
                monthLabel.setText(month + " " + year);
                monthLabel.setStyle("-fx-text-fill: #c37b1e; -fx-font-weight: bold; cursor: text;");
            } else {
                monthLabel.setText(month + " " + year);
                monthLabel.setStyle("-fx-text-fill: black;");
            }
        }
    }

    @FXML
    private void handleMonthLabelClick(MouseEvent event) {
        // Check if the clicked label text is the current month/year
        if (monthLabel != null) {
            String currentText = monthLabel.getText();

            // Extract the year from the text (assuming it's in "Month Year" format)
            String year = currentText.split(" ")[1];

            // Create a TextField for year editing
            TextField yearField = new TextField(year);
            yearField.setStyle("-fx-font-size: 18px; -fx-text-fill: #333333;");
            yearField.getStyleClass().add("editable-year-field"); // Apply custom CSS for TextField

            // Set the TextField in place of the month label
            monthLabel.setGraphic(yearField);

            // Focus the field to allow typing immediately
            yearField.requestFocus();

            // Handle Enter key to save changes
            yearField.setOnAction(e -> {
                try {
                    // Parse the entered year
                    int newYear = Integer.parseInt(yearField.getText().trim());
                    updateYear(newYear);
                    monthLabel.setText(monthLabel.getText().split(" ")[0] + " " + newYear); // Update the label
                    monthLabel.setGraphic(null); // Remove the TextField after saving
                } catch (NumberFormatException ex) {
                    // Handle invalid input (non-numeric value)
                    System.out.println("Invalid year input.");
                }
            });

            // Handle losing focus (e.g., if the user clicks outside the TextField)
            yearField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    // Cancel editing if focus is lost
                    monthLabel.setText(monthLabel.getText().split(" ")[0] + " " + year); // Revert to original year
                    monthLabel.setGraphic(null);
                }
            });
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

    private void updateYear(int newYear) {
        // Update the year in the calendar logic
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.YEAR, newYear);

        // Refresh the calendar view with the new year
        refreshCalendar(currentCalendar.get(Calendar.MONTH), newYear);
    }

    private void refreshCalendar(int month, int year) {
        // Logic to update the displayed month grid based on the selected year and month
        // This will refresh the calendar UI based on the new year

        // Example: Update the month label (e.g., "January 2025")
        monthLabel.setText(new DateFormatSymbols().getMonths()[month] + " " + year);
        // Update the calendar grid (you will likely need to clear the grid and refill it)
        populateCalendarGrid(month, year);
    }

    private void populateCalendarGrid(int month, int year) {
        // Clear existing calendar cells
        calendarGrid.getChildren().clear();

        // Set the correct number of days in the month
        LocalDate firstOfMonth = LocalDate.of(year, month + 1, 1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        int lastDayOfMonth = firstOfMonth.lengthOfMonth();

        // Populate the calendar with the correct days
        for (int i = 1; i <= lastDayOfMonth; i++) {
            int row = (firstDayOfWeek + i - 2) / 7; // Calculate row
            int col = (firstDayOfWeek + i - 2) % 7; // Calculate column

            Label dayLabel = new Label(String.valueOf(i));
            dayLabel.getStyleClass().add("calendar-cell");

            // Add the label to the grid
            calendarGrid.add(dayLabel, col, row);
        }
    }

    // Helper method to show a simple alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}