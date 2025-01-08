package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class HomeController {

    @FXML private ComboBox<Integer> yearDropdown;
    @FXML private ListView<String> monthListView;
    @FXML private TabPane tabPane;
    @FXML private Button createNewTrackerButton;
    @FXML public  Button deleteTrackerButton;
    @FXML private Label welcomeLabel;

    private int userId;
    private LocalDate currentMonth = LocalDate.now();
    private LocalDate today = LocalDate.now();
    private Map<Tab, Tracker> trackerMap = new HashMap<>();
    @FXML
    private Label monthLabel = new Label();

    private static final String BASE_PATH = "/images/";
    private static final String CHECKED_IMAGE_PATH = "Check.png";
    private static final String CROSSED_IMAGE_PATH = "X.png";

    @FXML
    private void initialize() {
        // Retrieve userId from the session
        this.userId = SessionHandler.getInstance().getUserId();

        String username = getUserNameFromDatabase(String.valueOf(userId));

        welcomeLabel.setText("Welcome, " + username);

        currentMonth = today.withDayOfMonth(1);

        monthListView.setItems(FXCollections.observableArrayList(
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        ));

        monthListView.setStyle(
                "-fx-background-color: orange;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-selection-bar: orange;" +
                        "-fx-selection-bar-text: white;"
        );

        // Listener to handle month selection changes
        monthListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateCalendar(newValue);
            }
        });

        yearDropdown.setOnMouseClicked(this::handleMonthYearSelection);

        // Initialize the calendar with the current month
        updateCalendar(currentMonth.getMonth().toString());

        // Handle create new tracker button
        createNewTrackerButton.setOnAction(this::handleCreateNewTracker);
        deleteTrackerButton.setOnAction(this::handleDeleteTracker);

        initializeYearDropdown();
        initializeMonthListView();

        // Load the first tracker after login/signup
        loadUserTrackers();

        tabPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {  // Check if it's a double click
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();  // Get the tab that was double-clicked
                if (selectedTab != null) {
                    handleRenameTracker(selectedTab);  // Rename the selected tab's tracker
                }
            }
        });
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
        return "User";
    }

    private void initializeYearDropdown() {
        int currentYear = currentMonth.getYear();
        List<Integer> years = new ArrayList<>();
        for (int year = currentYear - 25; year <= currentYear + 50; year++) {
            years.add(year);
        }
        yearDropdown.setItems(FXCollections.observableArrayList(years));
        yearDropdown.setValue(currentYear);
        yearDropdown.setOnAction(this::handleYearSelection);
    }

    private void initializeMonthListView() {
        List<String> months = Arrays.asList("JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER");
        monthListView.setItems(FXCollections.observableArrayList(months));
    }

    private void loadUserTrackers() {

        tabPane.getTabs().clear();

        List<Tracker> trackers = Tracker.getUserTrackers(userId);

        System.out.println("Loaded " + trackers.size() + " trackers.");

        for (Tracker tracker : trackers) {
            if (tracker != null) {
                addTrackerTab(tracker);
            } else {
                System.out.println("Warning: Tracker is null. Skipping.");
            }
        }
    }

    // Adds new Trackers with independent contents
    private void addTrackerTab(Tracker tracker) {
        Tab tab = new Tab(tracker.getTrackerName());
        tab.setUserData(tracker); // Associate tracker with tab

        Label newMonthLabel = new Label(tracker.getCurrentMonth().getMonth().name() + " " + tracker.getCurrentMonth().getYear());
        HBox tabNavBar = new HBox(newMonthLabel);
        tabNavBar.setAlignment(Pos.CENTER);
        newMonthLabel.setFont(Font.font("Berlin Sans FB Demi", 20));

        AnchorPane calendarContent = createCalendarContent(tracker);

        AnchorPane tabContent = new AnchorPane();
        tabContent.getChildren().addAll(tabNavBar, calendarContent);

        AnchorPane.setTopAnchor(tabNavBar, 0.0);
        AnchorPane.setLeftAnchor(tabNavBar, 0.0);
        AnchorPane.setRightAnchor(tabNavBar, 0.0);
        AnchorPane.setTopAnchor(calendarContent, 50.0);

        tab.setContent(tabContent);
        tabPane.getTabs().add(tab);
    }

    @FXML
    private void handleRenameTracker(Tab tab) {
        Tracker tracker = (Tracker) tab.getUserData();

        if (tracker == null) {
            showAlert("Error", "Tracker not found.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(tracker.getTrackerName());
        dialog.setTitle("Rename Tracker");
        dialog.setHeaderText("Rename the tracker");
        dialog.setContentText("Enter new tracker name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            try (Connection connection = DBConnection.getConnection()) {
                if (tracker.renameTracker(newName, connection)) {
                    tracker.setTrackerName(newName);
                    tab.setText(newName);
                } else {
                    showAlert("Error", "Unable to rename tracker.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Database connection error.");
            }
        });
    }

    private AnchorPane createCalendarContent(Tracker tracker) {
        AnchorPane calendarPane = new AnchorPane();

        if (tracker == null) {
            System.err.println("Error: Tracker is null. Unable to create calendar content.");
            return calendarPane;
        }

        GridPane calendarGrid = new GridPane();
        calendarGrid.setLayoutX(3.0);
        calendarGrid.setLayoutY(-18.0);
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.getStyleClass().add("calendar-grid");
        calendarGrid.setPrefSize(515.0, 430.0);
        calendarGrid.setAlignment(Pos.TOP_CENTER);

        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            GridPane.setValignment(dayLabel, VPos.TOP);
            calendarGrid.add(dayLabel, i, 0);
        }

        LocalDate firstOfMonth = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), 1);
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

            // Generates the Date Key
            String key = currentMonth.getYear() + "-" + currentMonth.getMonth().name() + "-" + day;

            ButtonState state = loadButtonState(key, tracker.getTrackerId());
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);

            dayButton.setOnAction(e -> handleDayClick(dayButton, key, tracker));

            calendarGrid.add(dayButton, col, row);
        }

        calendarPane.getChildren().addAll(calendarGrid);
        return calendarPane;
    }

    @FXML
    private void handleDeleteTracker(ActionEvent actionEvent) {
        int currentUserId = userId;

        List<Tracker> userTrackers = Tracker.getTrackersForCurrentUser(currentUserId);

        List<String> trackerNames = userTrackers.stream()
                .map(Tracker::getTrackerName)
                .collect(Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, trackerNames);
        dialog.setTitle("Delete Tracker");
        dialog.setHeaderText("Select a tracker to delete");
        dialog.setContentText("Choose tracker:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedName -> {
            Tracker trackerToDelete = userTrackers.stream()
                    .filter(tracker -> tracker.getTrackerName().equals(selectedName))
                    .findFirst()
                    .orElse(null);

            if (trackerToDelete != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmation");
                confirmAlert.setHeaderText("Are you sure?");
                confirmAlert.setContentText("Do you really want to delete \"" + selectedName + "\"?");

                Optional<ButtonType> confirmation = confirmAlert.showAndWait();
                if (confirmation.isPresent() && confirmation.get() == ButtonType.OK) {
                    boolean success = trackerToDelete.deleteTracker();
                    if (success) {
                        tabPane.getTabs().clear();
                        loadUserTrackers();
                    } else {
                        showAlert("Error", "Unable to delete tracker. It might have related changes or issues.");
                    }
                }
            }
        });
    }

    @FXML
    private void handleYearSelection(ActionEvent event) {
        Integer selectedYear = yearDropdown.getValue();
        currentMonth = currentMonth.withYear(selectedYear);
        updateCalendar(currentMonth.getMonth().toString());
    }

    @FXML
    public void handleMonthYearSelection(MouseEvent event) {
        String selectedMonth = monthListView.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            currentMonth = LocalDate.now().withMonth(Month.valueOf(selectedMonth.toUpperCase()).getValue()).withDayOfMonth(1);

            Integer selectedYear = yearDropdown.getValue();
            currentMonth = currentMonth.withYear(selectedYear);

            updateCalendar(currentMonth.getMonth().toString());
        }
    }

    private void updateCalendar(String month) {
        monthLabel.setText(month + " " + currentMonth.getYear());

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab != null) {
            Tracker tracker = (Tracker) selectedTab.getUserData();

            AnchorPane newCalendarContent = createCalendarContent(tracker);
            AnchorPane existingTabContent = (AnchorPane) selectedTab.getContent();

            existingTabContent.getChildren().clear();
            existingTabContent.getChildren().addAll(monthLabel, newCalendarContent);

            AnchorPane.setTopAnchor(newCalendarContent, 50.0);
        }
    }

    public void handleCreateNewTracker(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Tracker");
        dialog.setHeaderText("Create a new tracker");
        dialog.setContentText("Enter tracker name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(trackerName -> {
            Tracker newTracker = new Tracker(userId, trackerName);
            newTracker.addNewTracker(userId, trackerName);


            int trackerId = newTracker.getTrackerId();

            if (trackerId != -1) {
                Tab newTab = new Tab(trackerName);
                trackerMap.put(newTab, newTracker);
                tabPane.getTabs().add(newTab);

                loadUserTrackers();
            } else {
                showAlert("Error", "Failed to create new tracker.");
            }
        });
    }

    public enum ButtonState { NORMAL, CHECKED, CROSSED }

    private ButtonState loadButtonState(String dateKey, int trackerId) {
        String sql = "SELECT state FROM user_changes WHERE user_id = ? AND tracker_id = ? AND datelog = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, trackerId);
            pstmt.setString(3, dateKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ButtonState.valueOf(rs.getString("state"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ButtonState.NORMAL;
    }

    private void saveButtonState(String dateKey, ButtonState state, int trackerId) {
        String sql = "INSERT INTO user_changes (user_id, tracker_id, datelog, state) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(tracker_id, datelog) " +
                "DO UPDATE SET state = excluded.state";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, trackerId);
            pstmt.setString(3, dateKey);
            pstmt.setString(4, state.toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyButtonState(Button button, ButtonState state) {
        double imageSize = 20;

        switch (state) {
            case CHECKED:
                ImageView checkedImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(BASE_PATH + CHECKED_IMAGE_PATH))));
                checkedImageView.setFitWidth(imageSize);
                checkedImageView.setFitHeight(imageSize);
                checkedImageView.setPreserveRatio(true);
                button.setGraphic(checkedImageView);
                button.setText("");
                break;
            case CROSSED:
                ImageView crossedImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(BASE_PATH + CROSSED_IMAGE_PATH))));
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

    public void handleDayClick(Button dayButton, String key, Tracker tracker) {
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
        saveButtonState(key, nextState, tracker.getTrackerId());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #C37B1EFF; -fx-font-size: 14px; -fx-text-fill: white;");
        dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Node buttonBar = dialogPane.lookup(".button-bar");
        if (buttonBar instanceof HBox) {
            ((HBox) buttonBar).setAlignment(Pos.CENTER);
            ((HBox) buttonBar).setSpacing(10);
        }

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            SessionHandler.getInstance().clearSession();

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
        } else {
            alert.close();
        }
    }

}
