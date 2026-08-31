package com.example.habitzone.ui;

import com.example.habitzone.command.CommandResult;
import com.example.habitzone.domain.CompletionLog;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.usecase.HabitHistory;
import com.example.habitzone.usecase.HabitSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class MainWindowControllerTest {
    @Test
    void loadsHabitsForTheInitialView() {
        HabitSnapshot exercise = new HabitSnapshot(new HabitId("id"), "Exercise", List.of(), false);
        MainWindowController controller = new MainWindowController(input -> {
            assertEquals("list", input);
            return CommandResult.habits("1 habit.", List.of(exercise));
        }, () -> fail("exit should not run"));

        controller.loadInitialHabits();

        assertEquals(List.of(exercise), controller.habits());
        assertEquals("1 habit.", controller.feedback());
    }

    @Test
    void submitsInputThenRendersFeedbackAndReturnedHabits() {
        HabitSnapshot exercise = new HabitSnapshot(new HabitId("id"), "Exercise", List.of(), false);
        MainWindowController controller = new MainWindowController(input -> {
            assertEquals("list", input);
            return CommandResult.habits("1 habit.", List.of(exercise));
        }, () -> fail("exit should not run"));

        controller.submit("list");

        assertEquals("1 habit.", controller.feedback());
        assertFalse(controller.feedbackIsError());
        assertEquals(List.of(exercise), controller.habits());
    }

    @Test
    void showsHistoryForTheSelectedHabitUsingTheHistoryCommand() {
        HabitSnapshot exercise = new HabitSnapshot(new HabitId("id"), "Exercise", List.of(), false);
        MainWindowController controller = new MainWindowController(input -> {
            assertEquals("history Exercise", input);
            return CommandResult.history("Showing history for 'Exercise'.", new HabitHistory(
                    exercise.id(), exercise.name(), List.of(new CompletionLog(LocalDate.of(2026, 8, 18)))));
        }, () -> fail("exit should not run"));

        controller.showHabitHistory(exercise);

        assertEquals("Showing history for 'Exercise'.", controller.feedback());
        assertEquals(LocalDate.of(2026, 8, 18), controller.history().orElseThrow().completions().getFirst().date());
        assertEquals(exercise.id(), controller.displayedHistoryHabitId().orElseThrow());
    }

    @Test
    void performsExitOnlyWhenSignalled() {
        AtomicBoolean exited = new AtomicBoolean();
        MainWindowController controller = new MainWindowController(input -> CommandResult.exit("Goodbye."), () -> exited.set(true));

        controller.submit("exit");

        assertEquals("Goodbye.", controller.feedback());
        assertTrue(exited.get());
    }

    @Test
    void clearsTheHabitListWhenExecutorReturnsAnEmptySnapshot() {
        HabitSnapshot exercise = new HabitSnapshot(new HabitId("id"), "Exercise", List.of(), false);
        MainWindowController controller = new MainWindowController(
                input -> "list".equals(input)
                        ? CommandResult.habits("No habits.", List.of())
                        : CommandResult.habits("1 habit.", List.of(exercise)),
                () -> fail("exit should not run")
        );

        controller.submit("add exercise");
        controller.submit("list");

        assertTrue(controller.habits().isEmpty());
    }

    @Test
    void displaysCommandErrorsInFeedbackState() {
        MainWindowController controller = new MainWindowController(
                input -> CommandResult.failure("Please enter a command."),
                () -> fail("exit should not run")
        );

        controller.submit(" ");

        assertEquals("Please enter a command.", controller.feedback());
        assertTrue(controller.feedbackIsError());
    }

    @Test
    void keepsExistingHabitsWhenAResultDoesNotContainAHabitList() {
        HabitSnapshot exercise = new HabitSnapshot(new HabitId("id"), "Exercise", List.of(), false);
        MainWindowController controller = new MainWindowController(
                input -> "list".equals(input)
                        ? CommandResult.habits("1 habit.", List.of(exercise))
                        : CommandResult.success("Updated."),
                () -> fail("exit should not run")
        );

        controller.submit("list");
        controller.submit("done Exercise");

        assertEquals(List.of(exercise), controller.habits());
        assertEquals("Updated.", controller.feedback());
        assertFalse(controller.feedbackIsError());
    }
}
