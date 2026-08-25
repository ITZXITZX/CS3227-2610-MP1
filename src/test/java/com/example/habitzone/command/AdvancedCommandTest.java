package com.example.habitzone.command;

import com.example.habitzone.domain.*;
import com.example.habitzone.port.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AdvancedCommandTest {
    @Test
    void registryExecutesAdvancedCommandsAndPublishesThemInHelp() {
        MemoryRepository repository = new MemoryRepository();
        repository.habits.add(new Habit(new HabitId("id"), "Morning Run"));
        CommandRegistry registry = CommandRegistry.withRepository(repository, () -> LocalDate.of(2026, 8, 20));

        assertFalse(registry.execute("set-priority Morning Run high").error());
        assertEquals(HabitPriority.HIGH, repository.loadAll().getFirst().priority());
        assertFalse(registry.execute("set-category Morning Run Health").error());
        assertEquals("Health", repository.loadAll().getFirst().category().orElseThrow().name());
        assertEquals("Current streak: 0 day(s).", registry.execute("streak Morning Run").message());
        String help = registry.execute("help").message();
        assertTrue(help.contains("set-expiry HABIT_NAME YYYY-MM-DD"));
        assertTrue(help.contains("clear-expiry HABIT_NAME"));
        assertTrue(help.contains("set-priority HABIT_NAME low|normal|high"));
        assertTrue(help.contains("set-category HABIT_NAME CATEGORY"));
        assertTrue(help.contains("streak HABIT_NAME"));
    }

    @Test
    void advancedCommandsRejectMissingOrInvalidArgumentsWithoutMutation() {
        MemoryRepository repository = new MemoryRepository();
        repository.habits.add(new Habit(new HabitId("id"), "Read"));
        CommandRegistry registry = CommandRegistry.withRepository(repository, () -> LocalDate.now());

        assertTrue(registry.execute("set-expiry Read nope").error());
        assertEquals(CommandMessages.MISSING_HABIT_NAME, registry.execute("clear-expiry").message());
        assertEquals("Please provide a priority: low, normal, or high.", registry.execute("set-priority Read urgent").message());
        assertEquals(CommandMessages.MISSING_HABIT_NAME, registry.execute("set-category Read").message());
        assertEquals(CommandMessages.MISSING_HABIT_NAME, registry.execute("streak").message());
        assertEquals(HabitPriority.NORMAL, repository.loadAll().getFirst().priority());
    }

    private static final class MemoryRepository implements HabitRepository {
        private List<Habit> habits = new ArrayList<>();
        public List<Habit> loadAll() { return new ArrayList<>(habits); }
        public void saveAll(List<Habit> habits) { this.habits = new ArrayList<>(habits); }
    }
}
