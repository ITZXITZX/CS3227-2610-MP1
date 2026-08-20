package com.example.habitzone.ui;

import com.example.habitzone.command.CommandRegistry;
import com.example.habitzone.domain.Habit;
import com.example.habitzone.port.ClockProvider;
import com.example.habitzone.port.HabitRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRegistryExecutorTest {
    @Test
    void supportsTheMvpFlowAndRefreshesTheHabitSnapshot() {
        FakeHabitRepository repository = new FakeHabitRepository();
        CommandRegistryExecutor executor = new CommandRegistryExecutor(
                CommandRegistry.withRepository(repository, (ClockProvider) () -> LocalDate.of(2026, 8, 19))
        );

        assertFalse(executor.execute("add exercise").error());
        assertEquals(1, executor.execute("done exercise 2026-08-19").habits().size());
        assertTrue(executor.execute("history exercise").history().isPresent());
        assertFalse(executor.execute("undone exercise 2026-08-19").error());

        var deleted = executor.execute("delete exercise");

        assertFalse(deleted.error());
        assertTrue(deleted.includesHabitList());
        assertTrue(deleted.habits().isEmpty());
    }

    private static final class FakeHabitRepository implements HabitRepository {
        private List<Habit> habits = new ArrayList<>();

        @Override public List<Habit> loadAll() { return new ArrayList<>(habits); }
        @Override public void saveAll(List<Habit> habits) { this.habits = new ArrayList<>(habits); }
    }
}
