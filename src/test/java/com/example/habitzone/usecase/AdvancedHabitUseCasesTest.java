package com.example.habitzone.usecase;

import com.example.habitzone.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedHabitUseCasesTest {
    @Test
    void setsEveryPriorityAndCategoryWithNormalizedLookup() {
        FakeHabitRepository repository = seeded("Read");
        for (HabitPriority priority : HabitPriority.values()) {
            assertTrue(new SetHabitPriorityUseCase(repository).execute(" read ", priority).success());
            assertEquals(priority, repository.loadAll().getFirst().priority());
        }

        var result = new SetHabitCategoryUseCase(repository).execute(" READ ", " Learning ");
        assertTrue(result.success());
        assertEquals("Learning", result.data().category().orElseThrow().name());
        assertEquals(4, repository.saveCount());
    }

    @Test
    void advancedMutationsRejectInvalidOrMissingInputsWithoutSaving() {
        FakeHabitRepository repository = seeded("Read");
        int saves = repository.saveCount();

        assertEquals(UseCaseError.INVALID_HABIT_NAME, new SetHabitPriorityUseCase(repository).execute(" ", HabitPriority.HIGH).error());
        assertEquals(UseCaseError.INVALID_HABIT_NAME, new SetHabitCategoryUseCase(repository).execute("Read", " ").error());
        assertEquals(UseCaseError.HABIT_NOT_FOUND, new SetHabitCategoryUseCase(repository).execute("Write", "Learning").error());
        assertEquals(saves, repository.saveCount());
    }

    @Test
    void expiryFailuresDoNotSaveAndClearingAnUnsetExpiryIsSafe() {
        FakeHabitRepository repository = seeded("Read");
        ClearHabitExpiryUseCase clear = new ClearHabitExpiryUseCase(repository);

        assertTrue(clear.execute(" read ").success());
        assertEquals(1, repository.saveCount());
        assertEquals(UseCaseError.HABIT_NOT_FOUND, new SetHabitExpiryUseCase(repository).execute("Write", LocalDate.now()).error());
        assertEquals(UseCaseError.INVALID_HABIT_NAME, clear.execute(null).error());
        assertEquals(1, repository.saveCount());
    }

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
