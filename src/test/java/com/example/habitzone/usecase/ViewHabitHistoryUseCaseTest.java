package com.example.habitzone.usecase;

import com.example.habitzone.domain.CompletionLog;
import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewHabitHistoryUseCaseTest {
    @Test
    void viewsHistoryDescendingWithMostRecentFirst() {
        FakeHabitRepository repository = new FakeHabitRepository();
        Habit habit = new Habit(new HabitId("habit-1"), "Read");
        LocalDate oldDate = LocalDate.of(2026, 8, 17);
        LocalDate recentDate = LocalDate.of(2026, 8, 19);
        habit.markComplete(oldDate);
        habit.markComplete(recentDate);
        repository.seed(habit);

        UseCaseResult<HabitHistory> result = new ViewHabitHistoryUseCase(repository).execute("Read");

        assertTrue(result.success());
        assertEquals("Read", result.data().habitName());
        assertEquals(List.of(new CompletionLog(recentDate), new CompletionLog(oldDate)), result.data().completions());
    }

    @Test
    void rejectsMissingHabit() {
        FakeHabitRepository repository = new FakeHabitRepository();

        UseCaseResult<HabitHistory> result = new ViewHabitHistoryUseCase(repository).execute("Read");

        assertFalse(result.success());
        assertEquals(UseCaseError.HABIT_NOT_FOUND, result.error());
    }
}
