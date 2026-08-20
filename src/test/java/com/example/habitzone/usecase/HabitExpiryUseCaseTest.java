package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HabitExpiryUseCaseTest {
    @Test
    void setsThenClearsExpiryAndSavesEachMutation() {
        FakeHabitRepository repository = new FakeHabitRepository();
        repository.seed(new Habit(new HabitId("habit-1"), "Read"));
        LocalDate expiry = LocalDate.of(2026, 12, 31);

        var setResult = new SetHabitExpiryUseCase(repository).execute("Read", expiry);
        var clearResult = new ClearHabitExpiryUseCase(repository).execute("Read");

        assertEquals(expiry, setResult.data().expiryDate().orElseThrow());
        assertTrue(clearResult.data().expiryDate().isEmpty());
        assertEquals(2, repository.saveCount());
    }
}
