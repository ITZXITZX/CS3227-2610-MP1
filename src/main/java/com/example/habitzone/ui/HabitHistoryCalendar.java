package com.example.habitzone.ui;

import com.example.habitzone.usecase.HabitHistory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Scrollable month view of one habit's completion history. */
final class HabitHistoryCalendar extends ScrollPane {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM uuuu");
    private final VBox months = new VBox(16);
    private final LocalDate today;

    HabitHistoryCalendar(LocalDate today) {
        this.today = today;
        getStyleClass().add("history-calendar");
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setContent(months);
        VBox.setVgrow(this, Priority.ALWAYS);
    }

    LocalDate today() {
        return today;
    }

    void show(HabitHistory history) {
        Set<LocalDate> completedDates = new HashSet<>();
        history.completions().forEach(completion -> completedDates.add(completion.date()));

        YearMonth currentMonth = YearMonth.from(today);
        YearMonth firstMonth = completedDates.stream().min(LocalDate::compareTo)
                .map(YearMonth::from).filter(month -> month.isBefore(currentMonth)).orElse(currentMonth);
        YearMonth lastMonth = completedDates.stream().max(LocalDate::compareTo)
                .map(YearMonth::from).filter(month -> month.isAfter(currentMonth)).orElse(currentMonth);

        months.getChildren().clear();
        for (YearMonth month = firstMonth; !month.isAfter(lastMonth); month = month.plusMonths(1)) {
            months.getChildren().add(createMonth(month, completedDates));
        }
        Platform.runLater(() -> scrollTo(currentMonth));
    }

    void clear() {
        months.getChildren().clear();
    }

    private VBox createMonth(YearMonth month, Set<LocalDate> completedDates) {
        Label heading = new Label(month.format(MONTH_FORMAT));
        heading.getStyleClass().add("calendar-month-heading");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-grid");
        for (int column = 0; column < 7; column++) {
            DayOfWeek day = DayOfWeek.SUNDAY.plus(column);
            Label label = new Label(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            label.getStyleClass().add("calendar-weekday");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            grid.add(label, column, 0);
        }

        int startColumn = month.atDay(1).getDayOfWeek().getValue() % 7;
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            Label dayLabel = new Label(Integer.toString(day));
            dayLabel.getStyleClass().add("calendar-day");
            dayLabel.setUserData(date);
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            if (completedDates.contains(date)) {
                dayLabel.getStyleClass().add("completed");
                dayLabel.setAccessibleText(date + ", completed");
            } else {
                dayLabel.setAccessibleText(date.toString());
            }
            int position = startColumn + day - 1;
            grid.add(dayLabel, position % 7, position / 7 + 1);
        }
        for (int column = 0; column < 7; column++) {
            Region width = new Region();
            width.setMinWidth(36);
            GridPane.setHgrow(width, Priority.ALWAYS);
            grid.add(width, column, 7);
        }

        VBox card = new VBox(10, heading, grid);
        card.getStyleClass().add("calendar-month");
        card.setUserData(month);
        return card;
    }

    private void scrollTo(YearMonth month) {
        layout();
        months.layout();
        months.getChildren().stream().filter(node -> month.equals(node.getUserData())).findFirst()
                .ifPresent(node -> {
                    double scrollableHeight = months.getHeight() - getViewportBounds().getHeight();
                    setVvalue(scrollableHeight <= 0 ? 0 : node.getBoundsInParent().getMinY() / scrollableHeight);
                });
    }

    VBox monthsForTesting() {
        return months;
    }
}
