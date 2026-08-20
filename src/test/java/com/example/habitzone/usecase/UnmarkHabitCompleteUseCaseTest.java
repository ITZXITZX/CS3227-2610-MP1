package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnmarkHabitCompleteUseCaseTest {
    @Test
    void unmarksHabitCompleteAndSaves() {
        FakeHabitRepository repository = new FakeHabitRepository();
        Habit habit = new Habit(new HabitId("habit-1"), "Read");
        LocalDate date = LocalDate.of(2026, 8, 19);
        habit.markComplete(date);
        repository.seed(habit);

        UseCaseResult<HabitSnapshot> result = new UnmarkHabitCompleteUseCase(
                repository,
                new FixedClockProvider(LocalDate.of(2026, 1, 1))
        ).execute("Read", date);

        assertTrue(result.success());
        assertFalse(result.data().completionDates().contains(date));
        assertEquals(1, repository.saveCount());
    }

    @Test
    void rejectsMissingHabitWithoutSaving() {
        FakeHabitRepository repository = new FakeHabitRepository();

        UseCaseResult<HabitSnapshot> result = new UnmarkHabitCompleteUseCase(
                repository,
                new FixedClockProvider(LocalDate.of(2026, 8, 19))
        ).execute("Read");

        assertFalse(result.success());
        assertEquals(UseCaseError.HABIT_NOT_FOUND, result.error());
        assertEquals(0, repository.saveCount());
    }
}
