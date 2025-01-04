package com.tracker.calendartracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

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

    private String userId = "exampleUserId";

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
        currentMonth = today.withDayOfMonth(1);

        monthListView.setItems(FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));

        monthListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateCalendar(newValue);
            }
        });

        updateCalendar(currentMonth.getMonth().toString());
        createNewTrackerButton.setOnAction(this::handleCreateNewTracker);
    }

    private void updateCalendar(String month) {
        calendarGrid.getChildren().clear();
        dayButtons.clear();

        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.getStyleClass().add("calendar-header");
            calendarGrid.add(dayLabel, i, 0);
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

    private void saveButtonState(String key, ButtonState state) {
        userChanges.computeIfAbsent(userId, k -> new HashMap<>()).put(key, state);
    }

    private ButtonState loadButtonState(String key) {
        return userChanges.getOrDefault(userId, new HashMap<>()).getOrDefault(key, ButtonState.NORMAL);
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
        Label label = new Label("New Tracker Content");
        newTab.setContent(label);
        return newTab;
    }

    // ETO UNG GINAWA NI RJ NA INUPDATE KO FOR RETURN STATEMENT PARA MARUN
    // pinaltan ko lng pero experiment lng so retain ko toh as comment just in case
    /*
    // for creating new tracker/tab. will fix later. missing return statement.
    public Tab createNewTracker(ActionEvent event) {
        Tab newTab = new Tab("New Tracker");

        // Create content for the tab
        AnchorPane anchorPane = new AnchorPane();
        GridPane newCalendarGrid = new GridPane();
        newCalendarGrid.setHgap(10);
        newCalendarGrid.setVgap(10);
        newCalendarGrid.getStyleClass().add("calendar-grid");

        // mema ko lng toh para may something sa new tab
        Button newButton = new Button("Click Me");
        newButton.setOnAction(e -> showAlert("Button Clicked", "You clicked the button in the new tracker tab!"));
        newCalendarGrid.add(newButton, 0, 0);

        anchorPane.getChildren().add(newCalendarGrid);
        newTab.setContent(anchorPane);

        return newTab;
    }
     */

    @FXML
    public void handleMonthSelection(MouseEvent event) {
        String selectedMonth = monthListView.getSelectionModel().getSelectedItem();
        if (selectedMonth != null) {
            updateCalendar(selectedMonth);
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
