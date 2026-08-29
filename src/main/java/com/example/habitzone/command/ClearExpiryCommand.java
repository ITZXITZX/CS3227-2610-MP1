package com.example.habitzone.command;

import com.example.habitzone.usecase.ClearHabitExpiryUseCase;

import java.util.Objects;

public final class ClearExpiryCommand implements Command {
    private final ClearHabitExpiryUseCase clearHabitExpiryUseCase;

    public ClearExpiryCommand(ClearHabitExpiryUseCase clearHabitExpiryUseCase) {
        this.clearHabitExpiryUseCase = Objects.requireNonNull(clearHabitExpiryUseCase, "clearHabitExpiryUseCase");
    }

    @Override public String name() { return "clear-expiry"; }
    @Override public String usage() { return "clear-expiry HABIT_NAME"; }

    @Override
    public CommandResult execute(String arguments) {
        if (CommandSupport.isBlank(arguments)) return CommandResult.failure(CommandMessages.requiredInput(usage()));
        return CommandSupport.executeUseCase(
                () -> clearHabitExpiryUseCase.execute(arguments),
                habit -> CommandResult.success("Cleared expiry for '" + habit.name() + "'.")
        );
    }
}
