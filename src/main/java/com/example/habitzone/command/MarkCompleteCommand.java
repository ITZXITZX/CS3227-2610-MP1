package com.example.habitzone.command;

import com.example.habitzone.usecase.MarkHabitCompleteUseCase;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class MarkCompleteCommand implements Command {
    private final MarkHabitCompleteUseCase markHabitCompleteUseCase;

    public MarkCompleteCommand(MarkHabitCompleteUseCase markHabitCompleteUseCase) {
        this.markHabitCompleteUseCase = Objects.requireNonNull(markHabitCompleteUseCase, "markHabitCompleteUseCase");
    }

    @Override
    public String name() {
        return "done";
    }

    @Override
    public String usage() {
        return "done HABIT_NAME YYYY-MM-DD";
    }

    @Override
    public CommandResult execute(String arguments) {
        ParsedDatedHabit parsed = ParsedDatedHabit.from(arguments);
        if (parsed.missingName()) {
            return CommandResult.failure(CommandMessages.MISSING_HABIT_NAME);
        }
        if (parsed.date().isEmpty()) {
            return CommandResult.failure(CommandMessages.INVALID_DATE);
        }

        LocalDate date = parsed.date().get();
        return CommandSupport.executeUseCase(
                () -> markHabitCompleteUseCase.execute(parsed.habitName(), date),
                habit -> CommandResult.success("Marked '" + habit.name() + "' complete on " + date + ".")
        );
    }

    record ParsedDatedHabit(String habitName, Optional<LocalDate> date) {
        static ParsedDatedHabit from(String arguments) {
            if (CommandSupport.isBlank(arguments)) {
                return new ParsedDatedHabit("", Optional.empty());
            }

            String trimmed = arguments.trim();
            int lastSpace = trimmed.lastIndexOf(' ');
            if (lastSpace < 0) {
                return new ParsedDatedHabit("", CommandSupport.parseIsoDate(trimmed));
            }

            String habitName = trimmed.substring(0, lastSpace).trim();
            String dateText = trimmed.substring(lastSpace + 1).trim();
            return new ParsedDatedHabit(habitName, CommandSupport.parseIsoDate(dateText));
        }

        boolean missingName() {
            return CommandSupport.isBlank(habitName);
        }
    }
}
