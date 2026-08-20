package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;

import java.util.List;
import java.util.Optional;

final class HabitLookup {
    private HabitLookup() {
    }

    static Optional<Habit> findByName(List<Habit> habits, String habitName) {
        String normalized = normalizeName(habitName);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        return habits.stream()
                .filter(habit -> habit.name().equalsIgnoreCase(normalized))
                .findFirst();
    }

    static boolean isInvalidName(String habitName) {
        return normalizeName(habitName).isEmpty();
    }

    static String normalizeName(String habitName) {
        return habitName == null ? "" : habitName.trim();
    }
}
