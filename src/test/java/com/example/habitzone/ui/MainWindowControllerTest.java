package com.example.habitzone.ui;

import com.example.habitzone.command.CommandResult;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.domain.HabitPriority;
import com.example.habitzone.usecase.HabitSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class MainWindowControllerTest {
    @Test
    void submitsInputThenRendersFeedbackAndReturnedHabits() {
        HabitSnapshot exercise = new HabitSnapshot(new HabitId("id"), "Exercise", List.of(), Optional.empty(), Optional.empty(), HabitPriority.NORMAL, Optional.empty());
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
    void performsExitOnlyWhenSignalled() {
        AtomicBoolean exited = new AtomicBoolean();
        MainWindowController controller = new MainWindowController(input -> CommandResult.exit("Goodbye."), () -> exited.set(true));

        controller.submit("exit");

        assertEquals("Goodbye.", controller.feedback());
        assertTrue(exited.get());
    }

    @Test
    void clearsTheHabitListWhenExecutorReturnsAnEmptySnapshot() {
        HabitSnapshot exercise = new HabitSnapshot(new HabitId("id"), "Exercise", List.of(), Optional.empty(), Optional.empty(), HabitPriority.NORMAL, Optional.empty());
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
}
