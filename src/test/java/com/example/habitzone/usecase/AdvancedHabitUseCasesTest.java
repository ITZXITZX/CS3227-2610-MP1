package com.example.habitzone.usecase;

import com.example.habitzone.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedHabitUseCasesTest {
    @Test
    void streakCountsOnlyConsecutiveDaysEndingToday() {
        FakeHabitRepository repository = seeded("Read");
        Habit habit = repository.loadAll().getFirst();
        LocalDate today = LocalDate.of(2026, 8, 20);
        habit.markComplete(today);
        habit.markComplete(today.minusDays(1));
        habit.markComplete(today.minusDays(3));

        ViewHabitStreakUseCase useCase = new ViewHabitStreakUseCase(repository, () -> today);
        assertEquals(2, useCase.execute(" read ").data());
        assertEquals(UseCaseError.HABIT_NOT_FOUND, useCase.execute("Write").error());
    }

    private static FakeHabitRepository seeded(String name) {
        FakeHabitRepository repository = new FakeHabitRepository();
        repository.seed(new Habit(new HabitId("id"), name));
        return repository;
    }
}
