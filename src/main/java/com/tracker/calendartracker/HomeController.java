package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.geometry.Pos;
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
        String username = getUserNameFromDatabase(userId);

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
    }

    private void initializeYearDropdown() {
        int currentYear = today.getYear();
        List<Integer> years = new ArrayList<>();
        for (int year = currentYear - 25; year <= currentYear + 50; year++) {
            years.add(year);
        }
        yearDropdown.setItems(FXCollections.observableArrayList(years));
        yearDropdown.setValue(currentYear);
        yearDropdown.setOnAction(this::handleYearSelection);
    }

    private void handleYearSelection(ActionEvent event) {
        Integer selectedYear = yearDropdown.getValue();
        currentMonth = currentMonth.withYear(selectedYear);
        updateCalendar(currentMonth.getMonth().toString());
    }

    private void updateCalendar(String month) {
        calendarGrid.getChildren().clear();
        dayButtons.clear();

        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            GridPane.setHalignment(dayLabel, HPos.CENTER); // Center horizontally
            GridPane.setValignment(dayLabel, VPos.CENTER);
            calendarGrid.add(dayLabel, i, 0);  // Add labels in the first row
        }

        monthLabel.setText(month + " " + currentMonth.getYear());

        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), Month.valueOf(month.toUpperCase()), 1);
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
            dayButton.setId(String.valueOf(day));

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
                "ON CONFLICT(user_id, datelog) " +
                "DO UPDATE SET state = excluded.state";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, dateKey);
            pstmt.setString(3, state.toString());
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
        return ButtonState.NORMAL;
    }

    private void applyButtonState(Button button, ButtonState state) {
        double imageSize = 20;

        switch (state) {
            case CHECKED:
                ImageView checkedImageView = new ImageView(new Image(getClass().getResourceAsStream(BASE_PATH + CHECKED_IMAGE_PATH)));
                checkedImageView.setFitWidth(imageSize);
                checkedImageView.setFitHeight(imageSize);
                checkedImageView.setPreserveRatio(true);
                button.setGraphic(checkedImageView);
                button.setText("");
                break;
            case CROSSED:
                ImageView crossedImageView = new ImageView(new Image(getClass().getResourceAsStream(BASE_PATH + CROSSED_IMAGE_PATH)));
                crossedImageView.setFitWidth(imageSize);
                crossedImageView.setFitHeight(imageSize);
                crossedImageView.setPreserveRatio(true);
                button.setGraphic(crossedImageView);
                button.setText("");
                break;
            case NORMAL:
            default:
                button.setGraphic(null);
                button.setText(button.getId());
                break;
        }
    }

    @FXML
    private void handleCreateNewTracker(ActionEvent event) {
        Tab newTab = createNewTracker();
        tabPane.getTabs().add(newTab);
    }

    public Tab createNewTracker() {
        Tab newTab = new Tab("New Tracker");

        // Root layout for the tab content
        BorderPane borderPane = new BorderPane();
        GridPane newCalendarGrid = new GridPane();
        newCalendarGrid.setHgap(10);
        newCalendarGrid.setVgap(10);
        newCalendarGrid.getStyleClass().add("calendar-grid");
        newCalendarGrid.setAlignment(Pos.CENTER);

        Label monthLabel = new Label();
        monthLabel.getStyleClass().add("month-label");
        monthLabel.setAlignment(Pos.BASELINE_CENTER);

        HBox navigationBox = new HBox(10);
        Button prevMonthButton = new Button("<");
        Button nextMonthButton = new Button(">");

        // Set default tab calendar to the current month
        var ref = new Object() {
            LocalDate tabMonth = LocalDate.now().withDayOfMonth(1);
        };

        // Generate initial calendar view for the tab
        generateCalendar(newCalendarGrid, ref.tabMonth, monthLabel);

        // Previous month button action
        prevMonthButton.setOnAction(e -> {
            ref.tabMonth = ref.tabMonth.minusMonths(1);
            generateCalendar(newCalendarGrid, ref.tabMonth, monthLabel);
        });

        // Next month button action
        nextMonthButton.setOnAction(e -> {
            ref.tabMonth = ref.tabMonth.plusMonths(1);
            generateCalendar(newCalendarGrid, ref.tabMonth, monthLabel);
        });

        // Sync yearDropdown with tab calendar
        yearDropdown.setOnAction(event -> {
            Integer selectedYear = yearDropdown.getValue();
            if (selectedYear != null) {
                ref.tabMonth = ref.tabMonth.withYear(selectedYear);
                generateCalendar(newCalendarGrid, ref.tabMonth, monthLabel);
            }
        });

        // Sync with monthListView selection
        monthListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ref.tabMonth = ref.tabMonth.withMonth(Month.valueOf(newValue.toUpperCase()).getValue());
                generateCalendar(newCalendarGrid, ref.tabMonth, monthLabel);
            }
        });

        // Set up navigation and add to the tab layout
        navigationBox.getChildren().addAll(prevMonthButton, monthLabel, nextMonthButton);
        navigationBox.setAlignment(Pos.TOP_CENTER);

        borderPane.setTop(navigationBox);
        // Add elements to new tab
        borderPane.setCenter(newCalendarGrid);
        borderPane.setTop(navigation);
        newTab.setContent(borderPane);

        return newTab;
    }

    private void generateCalendar(GridPane grid, LocalDate currentMonth, Label monthLabel) {
        grid.getChildren().clear();

        // Add headers for days of the week
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            grid.add(dayLabel, i, 0);  // Place day labels in the first row
        }

        // Update the month label with the selected month and year
        monthLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());

        // Get the first day of the current month and determine its weekday position
        LocalDate firstOfMonth = currentMonth.withDayOfMonth(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;  // Ensure Sunday = 0

        int lengthOfMonth = firstOfMonth.lengthOfMonth();

        // Loop through each day of the month and populate the calendar grid
        for (int day = 1; day <= lengthOfMonth; day++) {
            int row = (firstDayOfWeek + day - 1) / 7 + 1;
            int col = (firstDayOfWeek + day - 1) % 7;

            // Create a button for each day
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45);
            dayButton.setId(String.valueOf(day));

            // Generate a unique key for tracking the state of each day
            String key = currentMonth.getMonth().toString() + "-" + day;

            // Load the button state (e.g., checked or unchecked)
            ButtonState state = loadButtonState(key);
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);

            // Handle click events to toggle state
            dayButton.setOnAction(e -> handleDayClick(dayButton, key));

            // Add the day button to the grid
            grid.add(dayButton, col, row);
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
    public void handleMonthSelection(MouseEvent event) {
        String selectedMonth = monthListView.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            // Update currentMonth based on the selected month
            currentMonth = LocalDate.now().withMonth(Month.valueOf(selectedMonth.toUpperCase()).getValue()).withDayOfMonth(1);
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
        return "Unknown User";
    }
}
