package com.example.habitzone.command;

import com.example.habitzone.usecase.UseCaseError;

final class CommandMessages {
    static final String EMPTY_COMMAND = "Please enter a command.";
    static final String UNKNOWN_COMMAND = "Unknown command. Type 'help' to see available commands.";
    static final String MISSING_HABIT_NAME = "Please provide a habit name.";
    static final String INVALID_DATE = "Please provide a valid date in YYYY-MM-DD format.";
    static final String STORAGE_FAILURE = "Sorry, HabitZone could not save or load your habits. Please try again.";

    private CommandMessages() {
    }

    static String fromUseCaseError(UseCaseError error) {
        return switch (error) {
            case DUPLICATE_HABIT -> "That habit already exists.";
            case HABIT_NOT_FOUND -> "I could not find that habit.";
            case INVALID_HABIT_NAME -> MISSING_HABIT_NAME;
        };
    }
}
