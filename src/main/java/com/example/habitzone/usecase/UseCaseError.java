package com.example.habitzone.usecase;

/** Enumerates expected business-rule failures reported by use cases. */
public enum UseCaseError {
    DUPLICATE_HABIT,
    HABIT_NOT_FOUND,
    INVALID_HABIT_NAME
}
