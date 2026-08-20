package com.example.habitzone.domain;

import java.util.Objects;

public record HabitCategory(String name) {
    public HabitCategory {
        Objects.requireNonNull(name, "name");
        name = name.trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Habit category cannot be blank");
        }
    }
}
