package com.example.habitzone.ui;

import com.example.habitzone.command.CommandRegistry;
import com.example.habitzone.command.CommandResult;

import java.util.Objects;

/** Adapts the command registry for the UI and attaches a fresh habit-list snapshot. */
public final class CommandRegistryExecutor implements CommandExecutor {
    private final CommandRegistry commandRegistry;

    public CommandRegistryExecutor(CommandRegistry commandRegistry) {
        this.commandRegistry = Objects.requireNonNull(commandRegistry, "commandRegistry");
    }

    @Override
    public CommandResult execute(String input) {
        CommandResult result = commandRegistry.execute(input);
        if (result.error() || result.exit()) {
            return result;
        }

        CommandResult habitList = commandRegistry.execute("list");
        return result.withHabitList(habitList.habits());
    }
}
