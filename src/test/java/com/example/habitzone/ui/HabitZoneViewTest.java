package com.example.habitzone.ui;

import com.example.habitzone.command.CommandResult;
import com.example.habitzone.domain.CompletionLog;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.domain.HabitPriority;
import com.example.habitzone.usecase.HabitHistory;
import com.example.habitzone.usecase.HabitSnapshot;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HabitZoneViewTest {
    private Stage stage;

    @BeforeAll
    static void startJavaFx() throws Exception {
        try {
            runOnFxThread(() -> {
                Platform.setImplicitExit(false);
                return null;
            });
        } catch (IllegalStateException toolkitNotStarted) {
            Platform.startup(() -> Platform.setImplicitExit(false));
        }
    }

    @AfterEach
    void closeStage() throws Exception {
        if (stage != null) {
            runOnFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }

    @Test
    void highlightsHabitWhoseHistoryIsDisplayed() throws Exception {
        List<HabitSnapshot> habits = habits(3);
        HabitSnapshot displayed = habits.get(1);

        ListView<HabitSnapshot> list = showView(habits, displayed, 500);

        assertEquals(displayed, list.getSelectionModel().getSelectedItem());
    }

    @Test
    void scrollsDisplayedHabitIntoViewInALongList() throws Exception {
        List<HabitSnapshot> habits = habits(60);
        HabitSnapshot displayed = habits.getLast();

        ListView<HabitSnapshot> list = showView(habits, displayed, 280);

        boolean selectedCellIsVisible = runOnFxThread(() -> list.lookupAll(".list-cell").stream()
                .filter(ListCell.class::isInstance)
                .map(ListCell.class::cast)
                .anyMatch(cell -> !cell.isEmpty() && cell.getItem().equals(displayed)
                        && cell.getBoundsInParent().intersects(list.getLayoutBounds())));
        assertTrue(selectedCellIsVisible, "the habit with displayed history should be scrolled into view");
    }

    @Test
    void doesNotScrollWhenSelectedHabitIsAlreadyVisible() throws Exception {
        List<HabitSnapshot> habits = habits(60);
        HabitSnapshot clicked = habits.get(23);
        ListView<HabitSnapshot> list = showInteractiveView(habits, 280);

        int firstVisibleBeforeClick = runOnFxThread(() -> {
            list.scrollTo(20);
            list.getParent().layout();
            int firstVisible = firstVisibleIndex(list);
            list.getSelectionModel().select(clicked);
            list.getParent().layout();
            return firstVisible;
        });

        assertEquals(firstVisibleBeforeClick, runOnFxThread(() -> firstVisibleIndex(list)));
        assertEquals(clicked, list.getSelectionModel().getSelectedItem());
    }

    @Test
    void updatesCalendarWhenSelectionMovesDownAndUp() throws Exception {
        List<HabitSnapshot> habits = habits(3);
        LocalDate firstHabitCompletion = LocalDate.of(2026, 8, 10);
        LocalDate secondHabitCompletion = LocalDate.of(2026, 8, 20);
        MainWindowController[] controllerHolder = new MainWindowController[1];
        HabitHistoryCalendar[] calendarHolder = new HabitHistoryCalendar[1];

        ListView<HabitSnapshot> list = runOnFxThread(() -> {
            MainWindowController controller = new MainWindowController(input -> {
                if (input.equals("list")) {
                    return CommandResult.habits("Habits", habits);
                }
                String habitName = input.substring("history ".length());
                HabitSnapshot habit = habits.stream().filter(candidate -> candidate.name().equals(habitName))
                        .findFirst().orElseThrow();
                LocalDate completion = habit.equals(habits.getFirst())
                        ? firstHabitCompletion : secondHabitCompletion;
                return CommandResult.history("History", new HabitHistory(
                        habit.id(), habit.name(), List.of(new CompletionLog(completion))));
            }, () -> { });
            controllerHolder[0] = controller;
            controller.loadInitialHabits();
            HabitZoneView view = new HabitZoneView(controller);
            calendarHolder[0] = view.historyCalendarForTesting();
            stage = new Stage();
            stage.setScene(new Scene(view, 800, 500));
            stage.show();
            return view.habitListForTesting();
        });

        runOnFxThread(() -> {
            list.getSelectionModel().selectFirst();
            list.getSelectionModel().selectNext();
            return null;
        });

        assertEquals(Optional.of(habits.get(1).id()), controllerHolder[0].displayedHistoryHabitId());
        assertEquals(List.of(secondHabitCompletion), completedDates(calendarHolder[0]));

        runOnFxThread(() -> {
            list.getSelectionModel().selectPrevious();
            return null;
        });

        assertEquals(Optional.of(habits.getFirst().id()), controllerHolder[0].displayedHistoryHabitId());
        assertEquals(List.of(firstHabitCompletion), completedDates(calendarHolder[0]));
    }

    @Test
    void calendarCoversEveryMonthFromEarliestCompletionThroughCurrentMonth() throws Exception {
        LocalDate today = LocalDate.of(2026, 9, 1);
        HabitHistory history = history(LocalDate.of(2026, 6, 12), LocalDate.of(2026, 8, 30));

        HabitHistoryCalendar calendar = runOnFxThread(() -> {
            HabitHistoryCalendar result = new HabitHistoryCalendar(today);
            result.show(history);
            return result;
        });

        assertEquals(List.of(
                        YearMonth.of(2026, 6), YearMonth.of(2026, 7),
                        YearMonth.of(2026, 8), YearMonth.of(2026, 9)),
                runOnFxThread(() -> calendar.monthsForTesting().getChildren().stream()
                        .map(node -> (YearMonth) node.getUserData()).toList()));
    }

    @Test
    void calendarMarksOnlyCompletionDatesAsCompleted() throws Exception {
        LocalDate completed = LocalDate.of(2026, 8, 18);
        HabitHistoryCalendar calendar = runOnFxThread(() -> {
            HabitHistoryCalendar result = new HabitHistoryCalendar(LocalDate.of(2026, 8, 20));
            result.show(history(completed));
            return result;
        });

        assertTrue(runOnFxThread(() -> calendar.monthsForTesting().lookupAll(".calendar-day.completed").stream()
                .anyMatch(node -> completed.equals(node.getUserData()))));
        assertEquals(1, runOnFxThread(() -> calendar.monthsForTesting()
                .lookupAll(".calendar-day.completed").size()));
    }

    @Test
    void calendarIncludesCurrentMonthWhenHistoryContainsAFutureCompletion() throws Exception {
        HabitHistoryCalendar calendar = runOnFxThread(() -> {
            HabitHistoryCalendar result = new HabitHistoryCalendar(LocalDate.of(2026, 9, 1));
            result.show(history(LocalDate.of(2026, 11, 2)));
            return result;
        });

        assertEquals(List.of(YearMonth.of(2026, 9), YearMonth.of(2026, 10), YearMonth.of(2026, 11)),
                runOnFxThread(() -> calendar.monthsForTesting().getChildren().stream()
                        .map(node -> (YearMonth) node.getUserData()).toList()));
    }

    @Test
    void calendarDefaultsToCurrentMonthWhenPastHistoryRequiresScrolling() throws Exception {
        LocalDate today = LocalDate.of(2026, 9, 1);
        HabitHistory history = history(LocalDate.of(2025, 1, 10));

        HabitHistoryCalendar calendar = runOnFxThread(() -> {
            HabitHistoryCalendar result = new HabitHistoryCalendar(today);
            result.show(history);
            stage = new Stage();
            stage.setScene(new Scene(result, 420, 300));
            stage.show();
            result.applyCss();
            result.layout();
            return result;
        });
        runOnFxThread(() -> null); // allow the calendar's deferred initial scroll to run

        assertTrue(runOnFxThread(() -> calendar.getVvalue() > 0.9),
                "the current month should be visible initially instead of the earliest completion");
    }

    private ListView<HabitSnapshot> showView(List<HabitSnapshot> habits, HabitSnapshot displayed,
                                              double height) throws Exception {
        return runOnFxThread(() -> {
            MainWindowController controller = new MainWindowController(
                    input -> input.equals("list")
                            ? CommandResult.habits("Habits", habits)
                            : CommandResult.history("History", new HabitHistory(
                                    displayed.id(), displayed.name(), List.of())),
                    () -> { });
            controller.loadInitialHabits();
            controller.showHabitHistory(displayed);

            HabitZoneView view = new HabitZoneView(controller);
            stage = new Stage();
            stage.setScene(new Scene(view, 800, height));
            stage.show();
            view.applyCss();
            view.layout();
            return view.habitListForTesting();
        });
    }

    private ListView<HabitSnapshot> showInteractiveView(List<HabitSnapshot> habits, double height) throws Exception {
        return runOnFxThread(() -> {
            MainWindowController controller = interactiveController(habits);
            controller.loadInitialHabits();

            HabitZoneView view = new HabitZoneView(controller);
            stage = new Stage();
            stage.setScene(new Scene(view, 800, height));
            stage.show();
            view.applyCss();
            view.layout();
            return view.habitListForTesting();
        });
    }

    private MainWindowController interactiveController(List<HabitSnapshot> habits) {
        return new MainWindowController(input -> {
            if (input.equals("list")) {
                return CommandResult.habits("Habits", habits);
            }
            String habitName = input.substring("history ".length());
            HabitSnapshot habit = habits.stream().filter(candidate -> candidate.name().equals(habitName))
                    .findFirst().orElseThrow();
            return CommandResult.history("History", new HabitHistory(habit.id(), habit.name(), List.of()));
        }, () -> { });
    }

    private static int firstVisibleIndex(ListView<HabitSnapshot> list) {
        return list.lookupAll(".list-cell").stream()
                .filter(ListCell.class::isInstance)
                .map(ListCell.class::cast)
                .filter(cell -> !cell.isEmpty()
                        && cell.getParent().getLayoutBounds().intersects(cell.getBoundsInParent()))
                .mapToInt(ListCell::getIndex)
                .min().orElseThrow();
    }

    private static List<HabitSnapshot> habits(int count) {
        List<HabitSnapshot> habits = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            habits.add(new HabitSnapshot(new HabitId("id-" + index), "Habit " + index,
                    List.of(), false, Optional.empty(), Optional.empty(), HabitPriority.NORMAL, Optional.empty()));
        }
        return habits;
    }

    private static List<LocalDate> completedDates(HabitHistoryCalendar calendar) throws Exception {
        return runOnFxThread(() -> calendar.monthsForTesting().lookupAll(".calendar-day.completed").stream()
                .map(node -> (LocalDate) node.getUserData())
                .sorted()
                .toList());
    }

    private static HabitHistory history(LocalDate... dates) {
        return new HabitHistory(new HabitId("history-id"), "Read",
                java.util.Arrays.stream(dates).map(CompletionLog::new).toList());
    }

    private static <T> T runOnFxThread(java.util.concurrent.Callable<T> action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return action.call();
        }
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get();
    }
}
