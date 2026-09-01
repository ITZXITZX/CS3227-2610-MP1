package com.example.habitzone.domain;

import java.time.LocalDate;
import java.util.Objects;

/** Represents one recorded completion date for a habit. */
public record CompletionLog(LocalDate date) {
    public CompletionLog {
        Objects.requireNonNull(date, "date");
    }
}
