package com.example.habitzone.ui;

import com.example.habitzone.command.CommandResult;
import com.example.habitzone.usecase.HabitHistory;
import com.example.habitzone.usecase.HabitSnapshot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Presentation coordinator with no JavaFX or habit business rules. */
public final class MainWindowController {
    private final CommandExecutor commandExecutor;
    private final Runnable exitAction;
    private List<HabitSnapshot> habits = List.of();
    private Optional<HabitHistory> history = Optional.empty();
    private String feedback = "Type 'help' to see available commands.";
    private boolean feedbackIsError;

    public MainWindowController(CommandExecutor commandExecutor, Runnable exitAction) {
        this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor");
        this.exitAction = Objects.requireNonNull(exitAction, "exitAction");
    }

    public void submit(String input) {
        CommandResult result = commandExecutor.execute(input);
        feedback = result.message();
        feedbackIsError = result.error();
        if (result.includesHabitList()) {
            habits = result.habits();
            history = Optional.empty();
        }
        if (result.history().isPresent()) {
            history = result.history();
        }
        if (result.exit()) {
            exitAction.run();
        }
    }

    public List<HabitSnapshot> habits() { return habits; }
    public Optional<HabitHistory> history() { return history; }
    public String feedback() { return feedback; }
    public boolean feedbackIsError() { return feedbackIsError; }
}
