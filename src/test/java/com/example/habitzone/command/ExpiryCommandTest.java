package com.example.habitzone.command;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.port.ClockProvider;
import com.example.habitzone.port.HabitRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpiryCommandTest {
    @Test
    void setsAndClearsExpiryThroughRegistry() {
        InMemoryRepository repository = new InMemoryRepository();
        repository.habits.add(new Habit(new HabitId("habit-1"), "Read"));
        CommandRegistry registry = CommandRegistry.withRepository(repository, (ClockProvider) () -> LocalDate.of(2026, 8, 20));

        assertFalse(registry.execute("set-expiry Read 2026-12-31").error());
        assertEquals(LocalDate.of(2026, 12, 31), repository.loadAll().getFirst().expiryDate().orElseThrow());
        assertFalse(registry.execute("clear-expiry Read").error());
        assertTrue(repository.loadAll().getFirst().expiryDate().isEmpty());
    }

    private static final class InMemoryRepository implements HabitRepository {
        private List<Habit> habits = new ArrayList<>();
        @Override public List<Habit> loadAll() { return new ArrayList<>(habits); }
        @Override public void saveAll(List<Habit> habits) { this.habits = new ArrayList<>(habits); }
    }
}
