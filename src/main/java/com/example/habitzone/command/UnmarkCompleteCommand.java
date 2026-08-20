package com.example.habitzone.command;

import com.example.habitzone.usecase.UnmarkHabitCompleteUseCase;

import java.time.LocalDate;
import java.util.Objects;

public class UnmarkCompleteCommand implements Command {
    private final UnmarkHabitCompleteUseCase unmarkHabitCompleteUseCase;

    public UnmarkCompleteCommand(UnmarkHabitCompleteUseCase unmarkHabitCompleteUseCase) {
        this.unmarkHabitCompleteUseCase = Objects.requireNonNull(unmarkHabitCompleteUseCase, "unmarkHabitCompleteUseCase");
    }

    @Override
    public String name() {
        return "undone";
    }

    @Override
    public String usage() {
        return "undone HABIT_NAME YYYY-MM-DD";
    }

    @Override
    public CommandResult execute(String arguments) {
        MarkCompleteCommand.ParsedDatedHabit parsed = MarkCompleteCommand.ParsedDatedHabit.from(arguments);
        if (parsed.missingName()) {
            return CommandResult.failure(CommandMessages.MISSING_HABIT_NAME);
        }
        if (parsed.date().isEmpty()) {
            return CommandResult.failure(CommandMessages.INVALID_DATE);
        }

        LocalDate date = parsed.date().get();
        return CommandSupport.executeUseCase(
                () -> unmarkHabitCompleteUseCase.execute(parsed.habitName(), date),
                habit -> CommandResult.success("Unmarked '" + habit.name() + "' complete on " + date + ".")
        );
    }
}
