package com.example.habitzone.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

public class Habit {
    private final HabitId id;
    private final String name;
    private final NavigableSet<LocalDate> completionDates;
    private final LocalDate expiryDate;
    private final HabitCategory category;
    private final HabitPriority priority;
    private final LocalTime reminderTime;

    public Habit(HabitId id, String name) {
        this(id, name, List.of(), null, null, HabitPriority.NORMAL, null);
    }

    public Habit(
            HabitId id,
            String name,
            List<LocalDate> completionDates,
            LocalDate expiryDate,
            HabitCategory category,
            HabitPriority priority,
            LocalTime reminderTime
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = normalizeName(name);
        this.completionDates = new TreeSet<>(Objects.requireNonNull(completionDates, "completionDates"));
        this.expiryDate = expiryDate;
        this.category = category;
        this.priority = Objects.requireNonNullElse(priority, HabitPriority.NORMAL);
        this.reminderTime = reminderTime;
    }

    private static String normalizeName(String name) {
        Objects.requireNonNull(name, "name");
        String normalized = name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Habit name cannot be blank");
        }
        return normalized;
    }

    public HabitId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Optional<LocalDate> expiryDate() {
        return Optional.ofNullable(expiryDate);
    }

    public Optional<HabitCategory> category() {
        return Optional.ofNullable(category);
    }

    public HabitPriority priority() {
        return priority;
    }

    public Optional<LocalTime> reminderTime() {
        return Optional.ofNullable(reminderTime);
    }

    public void markComplete(LocalDate date) {
        completionDates.add(Objects.requireNonNull(date, "date"));
    }

    public void unmarkComplete(LocalDate date) {
        completionDates.remove(Objects.requireNonNull(date, "date"));
    }

    public boolean isCompleteOn(LocalDate date) {
        return completionDates.contains(Objects.requireNonNull(date, "date"));
    }

    public List<LocalDate> completionDatesAscending() {
        return Collections.unmodifiableList(new ArrayList<>(completionDates));
    }

    public List<CompletionLog> completionHistoryDescending() {
        return completionDates.descendingSet().stream()
                .map(CompletionLog::new)
                .toList();
    }
}
