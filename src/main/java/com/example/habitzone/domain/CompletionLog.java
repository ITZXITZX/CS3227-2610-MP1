package com.example.habitzone.domain;

import java.time.LocalDate;
import java.util.Objects;

public record CompletionLog(LocalDate date) {
    public CompletionLog {
        Objects.requireNonNull(date, "date");
    }
}
