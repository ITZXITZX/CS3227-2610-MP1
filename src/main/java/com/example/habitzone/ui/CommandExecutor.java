package com.example.habitzone.ui;

import com.example.habitzone.command.CommandResult;

/** Boundary used by the main window to submit command text. */
@FunctionalInterface
public interface CommandExecutor {
    CommandResult execute(String input);
}
