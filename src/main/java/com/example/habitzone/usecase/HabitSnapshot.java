package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;

import java.time.LocalDate;
import java.util.List;

public record HabitSnapshot(
        HabitId id,
        String name,
        List<LocalDate> completionDates,
        boolean completedToday
) {
    public static HabitSnapshot from(Habit habit) {
        return from(habit, false);
    }

    public static HabitSnapshot from(Habit habit, boolean completedToday) {
        return new HabitSnapshot(
                habit.id(),
                habit.name(),
                habit.completionDatesAscending(),
                completedToday
        );
    }
}
