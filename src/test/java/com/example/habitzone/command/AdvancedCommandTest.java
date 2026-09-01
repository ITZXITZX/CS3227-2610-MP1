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

        assertEquals("Current streak: 0 day(s).", registry.execute("streak Morning Run").message());
        String help = registry.execute("help").message();
        assertTrue(help.contains("streak HABIT_INDEX_OR_NAME"));
    }

    @Test
    void advancedCommandsRejectMissingOrInvalidArgumentsWithoutMutation() {
        MemoryRepository repository = new MemoryRepository();
        repository.habits.add(new Habit(new HabitId("id"), "Read"));
        CommandRegistry registry = CommandRegistry.withRepository(repository, () -> LocalDate.now());

        assertEquals("Please input: streak HABIT_INDEX_OR_NAME", registry.execute("streak").message());
    }

    @Test
    void habitCommandsResolveOneBasedIndicesFromTheAlphabeticalList() {
        MemoryRepository repository = new MemoryRepository();
        repository.habits.add(new Habit(new HabitId("z-id"), "Zumba"));
        repository.habits.add(new Habit(new HabitId("a-id"), "Art"));
        CommandRegistry registry = CommandRegistry.withRepository(
                repository, () -> LocalDate.of(2026, 8, 20));

        assertEquals(List.of("Art", "Zumba"), registry.execute("list").habits().stream()
                .map(snapshot -> snapshot.name()).toList());
        assertEquals("Marked 'Art' complete today.", registry.execute("done 1").message());
        assertEquals("Showing history for 'Art'.", registry.execute("history 1").message());
        assertEquals("Current streak: 1 day(s).", registry.execute("streak 1").message());
        assertEquals("Unmarked 'Art' complete today.", registry.execute("undone 1").message());
        assertEquals("Deleted habit 'Zumba'.", registry.execute("delete 2").message());
        assertEquals("I could not find that habit.", registry.execute("history 2").message());
    }

    private static final class MemoryRepository implements HabitRepository {
        private List<Habit> habits = new ArrayList<>();
        public List<Habit> loadAll() { return new ArrayList<>(habits); }
        public void saveAll(List<Habit> habits) { this.habits = new ArrayList<>(habits); }
    }
}
