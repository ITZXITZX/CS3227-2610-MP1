package com.example.habitzone.command;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.port.ClockProvider;
import com.example.habitzone.port.HabitRepository;
import com.example.habitzone.usecase.AddHabitUseCase;
import com.example.habitzone.usecase.DeleteHabitUseCase;
import com.example.habitzone.usecase.MarkHabitCompleteUseCase;
import com.example.habitzone.usecase.UnmarkHabitCompleteUseCase;
import com.example.habitzone.usecase.ViewHabitHistoryUseCase;
import com.example.habitzone.usecase.ViewHabitsUseCase;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandHandlerTest {
    private static final LocalDate DATE = LocalDate.of(2026, 8, 19);

    @Test
    void addHandlerAddsHabitAndRejectsMissingName() {
        InMemoryHabitRepository repository = new InMemoryHabitRepository();
        AddHabitCommand command = new AddHabitCommand(new AddHabitUseCase(repository));

        assertFalse(command.execute("Morning Run").error());
        assertEquals("Morning Run", repository.loadAll().getFirst().name());
        assertEquals(CommandMessages.MISSING_HABIT_NAME, command.execute(" ").message());
    }

    @Test
    void deleteHandlerDeletesHabitAndReportsMissingHabit() {
        InMemoryHabitRepository repository = repositoryWith("Read");
        DeleteHabitCommand command = new DeleteHabitCommand(new DeleteHabitUseCase(repository));

        assertFalse(command.execute("Read").error());
        assertTrue(repository.loadAll().isEmpty());
        assertEquals("I could not find that habit.", command.execute("Read").message());
    }

    @Test
    void listHandlerReturnsStructuredHabitSnapshots() {
        ListHabitsCommand command = new ListHabitsCommand(new ViewHabitsUseCase(repositoryWith("Read")));

        CommandResult result = command.execute("");

        assertFalse(result.error());
        assertEquals("Found 1 habit.", result.message());
        assertEquals("Read", result.habits().getFirst().name());
    }

    @Test
    void markHandlerCompletesHabitAndRejectsInvalidDate() {
        InMemoryHabitRepository repository = repositoryWith("Read");
        MarkCompleteCommand command = new MarkCompleteCommand(new MarkHabitCompleteUseCase(repository, fixedClock()));

        assertFalse(command.execute("Read 2026-08-19").error());
        assertTrue(repository.loadAll().getFirst().isCompleteOn(DATE));
        assertEquals(CommandMessages.INVALID_DATE, command.execute("Read yesterday").message());
    }

    @Test
    void unmarkHandlerRemovesCompletionAndRejectsMissingName() {
        InMemoryHabitRepository repository = repositoryWith("Read");
        repository.loadAll().getFirst().markComplete(DATE);
        UnmarkCompleteCommand command = new UnmarkCompleteCommand(new UnmarkHabitCompleteUseCase(repository, fixedClock()));

        assertFalse(command.execute("Read 2026-08-19").error());
        assertFalse(repository.loadAll().getFirst().isCompleteOn(DATE));
        assertEquals(CommandMessages.MISSING_HABIT_NAME, command.execute("2026-08-19").message());
    }

    @Test
    void historyHandlerReturnsHistoryAndReportsMissingHabit() {
        InMemoryHabitRepository repository = repositoryWith("Read");
        repository.loadAll().getFirst().markComplete(DATE);
        ViewHistoryCommand command = new ViewHistoryCommand(new ViewHabitHistoryUseCase(repository));

        assertEquals(DATE, command.execute("Read").history().orElseThrow().completions().getFirst().date());
        assertEquals("I could not find that habit.", command.execute("Write").message());
    }

    @Test
    void helpHandlerListsRegisteredCommands() {
        ExitCommand exit = new ExitCommand();
        HelpCommand command = new HelpCommand(() -> List.of(exit));

        assertEquals("Available commands:" + System.lineSeparator() + "exit", command.execute("").message());
    }

    @Test
    void exitHandlerReturnsSignalWithoutClosingTheApplication() {
        CommandResult result = new ExitCommand().execute("");

        assertTrue(result.exit());
        assertFalse(result.error());
    }

    private static ClockProvider fixedClock() {
        return () -> DATE;
    }

    private static InMemoryHabitRepository repositoryWith(String name) {
        InMemoryHabitRepository repository = new InMemoryHabitRepository();
        repository.saveAll(List.of(new Habit(new HabitId("habit-1"), name)));
        return repository;
    }

    private static final class InMemoryHabitRepository implements HabitRepository {
        private List<Habit> habits = new ArrayList<>();

        @Override
        public List<Habit> loadAll() {
            return new ArrayList<>(habits);
        }

        @Override
        public void saveAll(List<Habit> habits) {
            this.habits = new ArrayList<>(habits);
        }
    }
}
