package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Provides shared name/index resolution and display ordering for habit use cases. */
final class HabitLookup {
    private static final Comparator<Habit> DISPLAY_ORDER = Comparator
            .comparing((Habit habit) -> habit.name().toLowerCase(java.util.Locale.ROOT))
            .thenComparing(Habit::name)
            .thenComparing(habit -> habit.id().value());

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

    /** Resolves a one-based displayed index, or falls back to a case-insensitive name. */
    static Optional<Habit> findBySelector(List<Habit> habits, String selector) {
        String normalized = normalizeName(selector);
        if (normalized.matches("[1-9]\\d*")) {
            try {
                int index = Integer.parseInt(normalized);
                List<Habit> ordered = sortedByDisplayOrder(habits);
                return index <= ordered.size() ? Optional.of(ordered.get(index - 1)) : Optional.empty();
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return findByName(habits, normalized);
    }

    static List<Habit> sortedByDisplayOrder(List<Habit> habits) {
        return habits.stream().sorted(DISPLAY_ORDER).toList();
    }

    static boolean isInvalidName(String habitName) {
        return !Habit.isValidName(habitName);
    }

    static String normalizeName(String habitName) {
        return habitName == null ? "" : habitName.trim();
    }
}
