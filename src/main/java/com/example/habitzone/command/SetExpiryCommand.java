package com.example.habitzone.command;

import com.example.habitzone.usecase.SetHabitExpiryUseCase;

import java.time.LocalDate;
import java.util.Objects;

public final class SetExpiryCommand implements Command {
    private final SetHabitExpiryUseCase setHabitExpiryUseCase;

    public SetExpiryCommand(SetHabitExpiryUseCase setHabitExpiryUseCase) {
        this.setHabitExpiryUseCase = Objects.requireNonNull(setHabitExpiryUseCase, "setHabitExpiryUseCase");
    }

    @Override public String name() { return "set-expiry"; }
    @Override public String usage() { return "set-expiry HABIT_NAME YYYY-MM-DD"; }

    @Override
    public CommandResult execute(String arguments) {
        MarkCompleteCommand.ParsedDatedHabit parsed = MarkCompleteCommand.ParsedDatedHabit.from(arguments);
        if (parsed.missingName()) return CommandResult.failure(CommandMessages.requiredInput(usage()));
        if (parsed.date().isEmpty()) return CommandResult.failure(CommandMessages.INVALID_DATE);
        LocalDate date = parsed.date().get();
        return CommandSupport.executeUseCase(
                () -> setHabitExpiryUseCase.execute(parsed.habitName(), date),
                habit -> CommandResult.success("Set expiry for '" + habit.name() + "' to " + date + ".")
        );
    }
}
