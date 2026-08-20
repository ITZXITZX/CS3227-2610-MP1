package com.example.habitzone.port;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortContractsTest {
    @Test
    void repositoryPortCanBeImplementedByAnInMemoryFake() {
        HabitRepository repository = new InMemoryHabitRepository();
        List<Habit> habits = List.of(new Habit(new HabitId("habit-1"), "Read"));

        repository.saveAll(habits);

        assertEquals(habits, repository.loadAll());
    }

    @Test
    void clockPortCanBeImplementedByAFixedDateFake() {
        LocalDate expectedDate = LocalDate.of(2026, 8, 20);
        ClockProvider clock = () -> expectedDate;

        assertEquals(expectedDate, clock.currentDate());
    }

    private static final class InMemoryHabitRepository implements HabitRepository {
        private List<Habit> habits = List.of();

        @Override
        public List<Habit> loadAll() {
            return List.copyOf(habits);
        }

        @Override
        public void saveAll(List<Habit> habits) {
            this.habits = new ArrayList<>(habits);
        }
    }
}
