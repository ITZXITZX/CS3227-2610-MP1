package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewHabitsUseCaseTest {
    @Test
    void viewsEmptyHabitList() {
        FakeHabitRepository repository = new FakeHabitRepository();

        UseCaseResult<List<HabitSnapshot>> result = new ViewHabitsUseCase(repository, fixedClock()).execute();

        assertTrue(result.success());
        assertTrue(result.data().isEmpty());
    }

    @Test
    void viewsHabitsSortedByName() {
        FakeHabitRepository repository = new FakeHabitRepository();
        repository.seed(new Habit(new HabitId("habit-1"), "Write"));
        repository.seed(new Habit(new HabitId("habit-2"), "Read"));

        UseCaseResult<List<HabitSnapshot>> result = new ViewHabitsUseCase(repository, fixedClock()).execute();

        assertEquals(List.of("Read", "Write"), result.data().stream().map(HabitSnapshot::name).toList());
    }

    @Test
    void includesWhetherEachHabitWasCompletedToday() {
        FakeHabitRepository repository = new FakeHabitRepository();
        Habit completed = new Habit(new HabitId("habit-1"), "Read");
        completed.markComplete(LocalDate.of(2026, 8, 19));
        repository.seed(completed);
        repository.seed(new Habit(new HabitId("habit-2"), "Write"));

        UseCaseResult<List<HabitSnapshot>> result = new ViewHabitsUseCase(repository, fixedClock()).execute();

        assertEquals(List.of(true, false), result.data().stream().map(HabitSnapshot::completedToday).toList());
    }

    private static FixedClockProvider fixedClock() {
        return new FixedClockProvider(LocalDate.of(2026, 8, 19));
    }
}
