package com.tracker.calendartracker;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CalendarView extends VBox {
    private GridPane calendarGrid;
    private Label monthLabel;
    private LocalDate currentMonth;

    private static final String BASE_PATH = "/images/";
    private static final String CHECKED_IMAGE_PATH = "Check.png";
    private static final String CROSSED_IMAGE_PATH = "X.png";

    private enum ButtonState {
        NORMAL,
        CHECKED,
        CROSSED
    }

    private static final Map<String, ButtonState> buttonStates = new HashMap<>();

    public CalendarView() {
        this.currentMonth = LocalDate.now().withDayOfMonth(1);
        initializeView();
    }

    private void initializeView() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));

        // Header with month label and buttons
        HBox header = new HBox(10);
        Button prevButton = new Button("Previous");
        Button nextButton = new Button("Next");
        monthLabel = new Label();
        monthLabel.setFont(new Font(16));

        prevButton.setOnAction(e -> switchMonth(-1));
        nextButton.setOnAction(e -> switchMonth(1));

        header.getChildren().addAll(prevButton, monthLabel, nextButton);
        header.setSpacing(15);

        // Calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);

        updateCalendar();

        this.getChildren().addAll(header, calendarGrid);
    }

    private void switchMonth(int direction) {
        currentMonth = currentMonth.plusMonths(direction);
        updateCalendar();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        monthLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());

        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            calendarGrid.add(dayLabel, i, 0);
        }

        LocalDate firstOfMonth = currentMonth.withDayOfMonth(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = firstOfMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            int row = (firstDayOfWeek + day - 1) / 7 + 1;
            int col = (firstDayOfWeek + day - 1) % 7;

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45);
            String key = currentMonth.getMonth().toString() + "-" + day;

            // Load existing state or default to NORMAL
            ButtonState state = buttonStates.getOrDefault(key, ButtonState.NORMAL);
            dayButton.setUserData(state);
            applyButtonState(dayButton, state);

            dayButton.setOnAction(e -> handleDayClick(dayButton, key));
            calendarGrid.add(dayButton, col, row);
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
        buttonStates.put(key, nextState);
    }

    private void applyButtonState(Button button, ButtonState state) {
        double imageSize = 20;

        switch (state) {
            case CHECKED:
                ImageView checkedImageView = new ImageView(new Image(getClass().getResourceAsStream(BASE_PATH + CHECKED_IMAGE_PATH)));
                checkedImageView.setFitWidth(imageSize);
                checkedImageView.setFitHeight(imageSize);
                button.setGraphic(checkedImageView);
                button.setText("");  // Hide the day number
                break;
            case CROSSED:
                ImageView crossedImageView = new ImageView(new Image(getClass().getResourceAsStream(BASE_PATH + CROSSED_IMAGE_PATH)));
                crossedImageView.setFitWidth(imageSize);
                crossedImageView.setFitHeight(imageSize);
                button.setGraphic(crossedImageView);
                button.setText("");
                break;
            case NORMAL:
            default:
                button.setGraphic(null);
                button.setText(button.getId());  // Restore the day number
                break;
        }
    }
}
