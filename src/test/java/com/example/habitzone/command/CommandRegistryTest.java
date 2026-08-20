package com.example.habitzone.command;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.infrastructure.StorageException;
import com.example.habitzone.port.ClockProvider;
import com.example.habitzone.port.HabitRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRegistryTest {
    private final LocalDate today = LocalDate.of(2026, 8, 19);

    @Test
    void parsesCommandNameAndArguments() {
        CommandParser.ParsedCommand parsed = new CommandParser().parse("  done Morning Run 2026-08-19  ");

        assertEquals("done", parsed.name());
        assertEquals("Morning Run 2026-08-19", parsed.arguments());
    }

    @Test
    void parsesWhitespaceOtherThanSpacesBetweenCommandAndArguments() {
        CommandParser.ParsedCommand parsed = new CommandParser().parse("done\tMorning Run 2026-08-19");

        assertEquals("done", parsed.name());
        assertEquals("Morning Run 2026-08-19", parsed.arguments());
    }

    @Test
    void parserTreatsNullAndWhitespaceOnlyInputAsAnEmptyCommand() {
        CommandParser parser = new CommandParser();

        assertEquals(new CommandParser.ParsedCommand("", ""), parser.parse(null));
        assertEquals(new CommandParser.ParsedCommand("", ""), parser.parse(" \t\n "));
    }

    @Test
    void addCreatesHabitWithMultiWordName() {
        FakeHabitRepository repository = new FakeHabitRepository();
        CommandResult result = registry(repository).execute("add Morning Run");

        assertFalse(result.error());
        assertEquals("Added habit 'Morning Run'.", result.message());
        assertEquals("Morning Run", repository.loadAll().getFirst().name());
    }

    @Test
    void deleteRemovesExistingHabit() {
        FakeHabitRepository repository = new FakeHabitRepository();
        repository.seed(new Habit(new HabitId("habit-1"), "Morning Run"));

        CommandResult result = registry(repository).execute("delete Morning Run");

        assertFalse(result.error());
        assertTrue(repository.loadAll().isEmpty());
    }

    @Test
    void listReturnsHabitSnapshots() {
        FakeHabitRepository repository = new FakeHabitRepository();
        repository.seed(new Habit(new HabitId("habit-1"), "Read"));
        repository.seed(new Habit(new HabitId("habit-2"), "Exercise"));

        CommandResult result = registry(repository).execute("list");

        assertFalse(result.error());
        assertEquals(2, result.habits().size());
        assertEquals("Exercise", result.habits().getFirst().name());
    }

    @Test
    void doneTreatsFinalTokenAsDateAndRestAsHabitName() {
        FakeHabitRepository repository = new FakeHabitRepository();
        repository.seed(new Habit(new HabitId("habit-1"), "Morning Run"));

        CommandResult result = registry(repository).execute("done Morning Run 2026-08-19");

        assertFalse(result.error());
        assertTrue(repository.loadAll().getFirst().isCompleteOn(today));
    }

    @Test
    void undoneTreatsFinalTokenAsDateAndRestAsHabitName() {
        FakeHabitRepository repository = new FakeHabitRepository();
        Habit habit = new Habit(new HabitId("habit-1"), "Morning Run");
        habit.markComplete(today);
        repository.seed(habit);

        CommandResult result = registry(repository).execute("undone Morning Run 2026-08-19");

        assertFalse(result.error());
        assertFalse(repository.loadAll().getFirst().isCompleteOn(today));
    }

    @Test
    void historyReturnsStructuredHistory() {
        FakeHabitRepository repository = new FakeHabitRepository();
        Habit habit = new Habit(new HabitId("habit-1"), "Read");
        habit.markComplete(LocalDate.of(2026, 8, 18));
        repository.seed(habit);

        CommandResult result = registry(repository).execute("history Read");

        assertFalse(result.error());
        assertTrue(result.history().isPresent());
        assertEquals("Read", result.history().get().habitName());
        assertEquals(LocalDate.of(2026, 8, 18), result.history().get().completions().getFirst().date());
    }

    @Test
    void helpIncludesAllCommands() {
        CommandResult result = registry(new FakeHabitRepository()).execute("help");

        assertFalse(result.error());
        assertTrue(result.message().contains("add HABIT_NAME"));
        assertTrue(result.message().contains("delete HABIT_NAME"));
        assertTrue(result.message().contains("list"));
        assertTrue(result.message().contains("done HABIT_NAME YYYY-MM-DD"));
        assertTrue(result.message().contains("undone HABIT_NAME YYYY-MM-DD"));
        assertTrue(result.message().contains("history HABIT_NAME"));
        assertTrue(result.message().contains("help"));
        assertTrue(result.message().contains("exit"));
    }

    @Test
    void exitReturnsExitSignalOnly() {
        CommandResult result = registry(new FakeHabitRepository()).execute("exit");

        assertFalse(result.error());
        assertTrue(result.exit());
        assertTrue(result.habits().isEmpty());
        assertTrue(result.history().isEmpty());
    }

    @Test
    void unknownCommandReturnsFriendlyError() {
        CommandResult result = registry(new FakeHabitRepository()).execute("dance");

        assertTrue(result.error());
        assertEquals(CommandMessages.UNKNOWN_COMMAND, result.message());
    }

    @Test
    void emptyCommandReturnsFriendlyError() {
        CommandResult result = registry(new FakeHabitRepository()).execute("   ");

        assertTrue(result.error());
        assertEquals(CommandMessages.EMPTY_COMMAND, result.message());
    }

    @Test
    void invalidCommandsDoNotModifyExistingHabits() {
        FakeHabitRepository repository = new FakeHabitRepository();
        Habit read = new Habit(new HabitId("habit-1"), "Read");
        repository.seed(read);
        CommandRegistry registry = registry(repository);

        registry.execute("   ");
        registry.execute("dance Read");
        registry.execute("done Read not-a-date");
        registry.execute("add");

        assertEquals(1, repository.loadAll().size());
        assertEquals("Read", repository.loadAll().getFirst().name());
        assertFalse(repository.loadAll().getFirst().isCompleteOn(today));
    }

    @Test
    void invalidDateReturnsFriendlyError() {
        CommandResult result = registry(new FakeHabitRepository()).execute("done Read 19-08-2026");

        assertTrue(result.error());
        assertEquals(CommandMessages.INVALID_DATE, result.message());
    }

    @Test
    void doneAndUndoneWithHabitButNoDateReportAnInvalidDate() {
        CommandRegistry registry = registry(new FakeHabitRepository());

        assertEquals(CommandMessages.INVALID_DATE, registry.execute("done exercise").message());
        assertEquals(CommandMessages.INVALID_DATE, registry.execute("undone exercise").message());
    }

    @Test
    void missingArgumentsReturnFriendlyErrors() {
        CommandRegistry registry = registry(new FakeHabitRepository());

        assertEquals(CommandMessages.MISSING_HABIT_NAME, registry.execute("add").message());
        assertEquals(CommandMessages.MISSING_HABIT_NAME, registry.execute("delete").message());
        assertEquals(CommandMessages.MISSING_HABIT_NAME, registry.execute("history").message());
        assertEquals(CommandMessages.MISSING_HABIT_NAME, registry.execute("done 2026-08-19").message());
        assertEquals(CommandMessages.MISSING_HABIT_NAME, registry.execute("undone 2026-08-19").message());
    }

    @Test
    void duplicateHabitReturnsFriendlyError() {
        FakeHabitRepository repository = new FakeHabitRepository();
        registry(repository).execute("add Read");

        CommandResult result = registry(repository).execute("add read");

        assertTrue(result.error());
        assertEquals("That habit already exists.", result.message());
    }

    @Test
    void missingHabitReturnsFriendlyError() {
        CommandResult result = registry(new FakeHabitRepository()).execute("delete Read");

        assertTrue(result.error());
        assertEquals("I could not find that habit.", result.message());
    }

    @Test
    void storageFailureReturnsFriendlyError() {
        CommandResult result = registry(new ThrowingHabitRepository()).execute("list");

        assertTrue(result.error());
        assertEquals(CommandMessages.STORAGE_FAILURE, result.message());
    }

    private CommandRegistry registry(HabitRepository repository) {
        ClockProvider clockProvider = () -> today;
        return CommandRegistry.withRepository(repository, clockProvider);
    }

    private static class FakeHabitRepository implements HabitRepository {
        private List<Habit> habits = new ArrayList<>();

        @Override
        public List<Habit> loadAll() {
            return new ArrayList<>(habits);
        }

        @Override
        public void saveAll(List<Habit> habits) {
            this.habits = new ArrayList<>(habits);
        }

        void seed(Habit habit) {
            habits.add(habit);
        }
    }

    private static class ThrowingHabitRepository implements HabitRepository {
        @Override
        public List<Habit> loadAll() {
            throw new StorageException("storage unavailable", new IllegalStateException());
        }

        @Override
        public void saveAll(List<Habit> habits) {
            throw new StorageException("storage unavailable", new IllegalStateException());
        }
    }
}
