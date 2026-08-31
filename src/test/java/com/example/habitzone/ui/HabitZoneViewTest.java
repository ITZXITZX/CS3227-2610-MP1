package com.example.habitzone.ui;

import com.example.habitzone.command.CommandResult;
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
    void doesNotScrollWhenClickedHabitIsAlreadyVisible() throws Exception {
        List<HabitSnapshot> habits = habits(60);
        HabitSnapshot clicked = habits.get(23);
        ListView<HabitSnapshot> list = showInteractiveView(habits, 280);

        int firstVisibleBeforeClick = runOnFxThread(() -> {
            list.scrollTo(20);
            list.getParent().layout();
            int firstVisible = firstVisibleIndex(list);
            list.getSelectionModel().select(clicked);
            list.getOnMouseClicked().handle(null);
            list.getParent().layout();
            return firstVisible;
        });

        assertEquals(firstVisibleBeforeClick, runOnFxThread(() -> firstVisibleIndex(list)));
        assertEquals(clicked, list.getSelectionModel().getSelectedItem());
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
            MainWindowController controller = new MainWindowController(input -> {
                if (input.equals("list")) {
                    return CommandResult.habits("Habits", habits);
                }
                String habitName = input.substring("history ".length());
                HabitSnapshot habit = habits.stream().filter(candidate -> candidate.name().equals(habitName))
                        .findFirst().orElseThrow();
                return CommandResult.history("History", new HabitHistory(habit.id(), habit.name(), List.of()));
            }, () -> { });
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

    private static <T> T runOnFxThread(java.util.concurrent.Callable<T> action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return action.call();
        }
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get();
    }
}
