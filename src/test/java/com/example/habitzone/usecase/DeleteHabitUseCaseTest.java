package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteHabitUseCaseTest {
    @Test
    void deletesExistingHabitAndSaves() {
        FakeHabitRepository repository = new FakeHabitRepository();
        repository.seed(new Habit(new HabitId("habit-1"), "Read"));

        UseCaseResult<HabitSnapshot> result = new DeleteHabitUseCase(repository).execute("Read");

        assertTrue(result.success());
        assertEquals("Read", result.data().name());
        assertTrue(repository.loadAll().isEmpty());
        assertEquals(1, repository.saveCount());
    }

    @Test
    void rejectsMissingHabitWithoutSaving() {
        FakeHabitRepository repository = new FakeHabitRepository();

        UseCaseResult<HabitSnapshot> result = new DeleteHabitUseCase(repository).execute("Read");

        assertFalse(result.success());
        assertEquals(UseCaseError.HABIT_NOT_FOUND, result.error());
        assertEquals(0, repository.saveCount());
    }
}
