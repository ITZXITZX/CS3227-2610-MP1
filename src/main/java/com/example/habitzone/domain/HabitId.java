package com.example.habitzone.domain;

import java.util.Objects;
import java.util.UUID;

public record HabitId(String value) {
    public HabitId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Habit id cannot be blank");
        }
    }

    public static HabitId newId() {
        return new HabitId(UUID.randomUUID().toString());
    }
}
