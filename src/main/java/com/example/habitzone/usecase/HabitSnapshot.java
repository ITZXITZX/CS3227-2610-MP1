package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitCategory;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.domain.HabitPriority;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public record HabitSnapshot(
        HabitId id,
        String name,
        List<LocalDate> completionDates,
        boolean completedToday,
        Optional<LocalDate> expiryDate,
        Optional<HabitCategory> category,
        HabitPriority priority,
        Optional<LocalTime> reminderTime
) {
    public static HabitSnapshot from(Habit habit) {
        return from(habit, false);
    }

    public static HabitSnapshot from(Habit habit, boolean completedToday) {
        return new HabitSnapshot(
                habit.id(),
                habit.name(),
                habit.completionDatesAscending(),
                completedToday,
                habit.expiryDate(),
                habit.category(),
                habit.priority(),
                habit.reminderTime()
        );
    }
}
