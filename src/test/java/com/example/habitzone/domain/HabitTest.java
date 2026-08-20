package com.example.habitzone.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HabitTest {
    @Test
    void createsHabitWithStableIdAndName() {
        HabitId id = new HabitId("habit-1");
        Habit habit = new Habit(id, " Reading ");

        assertEquals(id, habit.id());
        assertEquals("Reading", habit.name());
    }

    @Test
    void rejectsBlankHabitName() {
        assertThrows(IllegalArgumentException.class, () -> new Habit(new HabitId("habit-1"), " "));
    }

    @Test
    void createsHabitWithoutOptionalFutureFields() {
        Habit habit = new Habit(new HabitId("habit-1"), "Read");

        assertEquals(Optional.empty(), habit.expiryDate());
        assertEquals(Optional.empty(), habit.category());
        assertEquals(HabitPriority.NORMAL, habit.priority());
        assertEquals(Optional.empty(), habit.reminderTime());
    }

    @Test
    void retainsSuppliedFutureFieldValues() {
        LocalDate expiryDate = LocalDate.of(2026, 12, 31);
        HabitCategory category = new HabitCategory("Learning");
        LocalTime reminderTime = LocalTime.of(9, 30);

        Habit habit = new Habit(
                new HabitId("habit-1"),
                "Read",
                List.of(),
                expiryDate,
                category,
                HabitPriority.HIGH,
                reminderTime
        );

        assertEquals(Optional.of(expiryDate), habit.expiryDate());
        assertEquals(Optional.of(category), habit.category());
        assertEquals(HabitPriority.HIGH, habit.priority());
        assertEquals(Optional.of(reminderTime), habit.reminderTime());
    }

    @Test
    void marksSameDateCompleteIdempotently() {
        Habit habit = new Habit(new HabitId("habit-1"), "Read");
        LocalDate date = LocalDate.of(2026, 8, 19);

        habit.markComplete(date);
        habit.markComplete(date);

        assertTrue(habit.isCompleteOn(date));
        assertEquals(List.of(date), habit.completionDatesAscending());
    }

    @Test
    void unmarksCompletedDate() {
        Habit habit = new Habit(new HabitId("habit-1"), "Read");
        LocalDate date = LocalDate.of(2026, 8, 19);

        habit.markComplete(date);
        habit.unmarkComplete(date);

        assertFalse(habit.isCompleteOn(date));
        assertTrue(habit.completionDatesAscending().isEmpty());
    }

    @Test
    void unmarkingMissingDateIsSafe() {
        Habit habit = new Habit(new HabitId("habit-1"), "Read");

        habit.unmarkComplete(LocalDate.of(2026, 8, 19));

        assertTrue(habit.completionDatesAscending().isEmpty());
    }

    @Test
    void returnsCompletionHistoryDescendingWithMostRecentFirst() {
        Habit habit = new Habit(new HabitId("habit-1"), "Read");
        LocalDate first = LocalDate.of(2026, 8, 17);
        LocalDate second = LocalDate.of(2026, 8, 19);
        LocalDate third = LocalDate.of(2026, 8, 18);

        habit.markComplete(first);
        habit.markComplete(second);
        habit.markComplete(third);

        assertEquals(
                List.of(new CompletionLog(second), new CompletionLog(third), new CompletionLog(first)),
                habit.completionHistoryDescending()
        );
    }
}
