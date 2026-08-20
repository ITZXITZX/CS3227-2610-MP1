package com.example.habitzone.command;

import com.example.habitzone.usecase.HabitHistory;
import com.example.habitzone.usecase.HabitSnapshot;

import java.util.List;
import java.util.Optional;

public record CommandResult(
        String message,
        boolean error,
        boolean exit,
        List<HabitSnapshot> habits,
        Optional<HabitHistory> history,
        boolean includesHabitList
) {
    public CommandResult {
        habits = List.copyOf(habits);
        history = history == null ? Optional.empty() : history;
    }

    public static CommandResult success(String message) {
        return new CommandResult(message, false, false, List.of(), Optional.empty(), false);
    }

    public static CommandResult habits(String message, List<HabitSnapshot> habits) {
        return new CommandResult(message, false, false, habits, Optional.empty(), true);
    }

    public static CommandResult history(String message, HabitHistory history) {
        return new CommandResult(message, false, false, List.of(), Optional.of(history), false);
    }

    public static CommandResult failure(String message) {
        return new CommandResult(message, true, false, List.of(), Optional.empty(), false);
    }

    public static CommandResult exit(String message) {
        return new CommandResult(message, false, true, List.of(), Optional.empty(), false);
    }

    /** Returns this command outcome with the current habit-list snapshot for presentation. */
    public CommandResult withHabitList(List<HabitSnapshot> habits) {
        return new CommandResult(message, error, exit, habits, history, true);
    }
}
