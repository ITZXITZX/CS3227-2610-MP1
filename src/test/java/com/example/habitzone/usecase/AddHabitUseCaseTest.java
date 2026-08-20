package com.example.habitzone.usecase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddHabitUseCaseTest {
    @Test
    void addsHabitAndSaves() {
        FakeHabitRepository repository = new FakeHabitRepository();

        UseCaseResult<HabitSnapshot> result = new AddHabitUseCase(repository).execute("Read");

        assertTrue(result.success());
        assertEquals("Read", result.data().name());
        assertEquals(1, repository.loadAll().size());
        assertEquals(1, repository.saveCount());
    }

    @Test
    void rejectsDuplicateHabitNameCaseInsensitively() {
        FakeHabitRepository repository = new FakeHabitRepository();
        new AddHabitUseCase(repository).execute("Read");

        UseCaseResult<HabitSnapshot> result = new AddHabitUseCase(repository).execute(" read ");

        assertFalse(result.success());
        assertEquals(UseCaseError.DUPLICATE_HABIT, result.error());
        assertEquals(1, repository.loadAll().size());
        assertEquals(1, repository.saveCount());
    }

    @Test
    void rejectsBlankHabitName() {
        FakeHabitRepository repository = new FakeHabitRepository();

        UseCaseResult<HabitSnapshot> result = new AddHabitUseCase(repository).execute(" ");

        assertFalse(result.success());
        assertEquals(UseCaseError.INVALID_HABIT_NAME, result.error());
        assertEquals(0, repository.saveCount());
    }
}
