package com.example.habitzone.command;

import com.example.habitzone.usecase.MarkHabitCompleteUseCase;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/** Handles the {@code done} command by marking a habit complete on a specified date. */
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
        return "done HABIT_NAME [YYYY-MM-DD]";
    }

    @Override
    public CommandResult execute(String arguments) {
        ParsedDatedHabit parsed = ParsedDatedHabit.from(arguments);
        if (parsed.missingName()) {
            return CommandResult.failure(CommandMessages.requiredInput(usage()));
        }
        if (parsed.invalidDate()) {
            return CommandResult.failure(CommandMessages.INVALID_DATE);
        }

        if (parsed.date().isEmpty()) {
            return CommandSupport.executeUseCase(
                    () -> markHabitCompleteUseCase.execute(parsed.habitName()),
                    habit -> CommandResult.success("Marked '" + habit.name() + "' complete today.")
            );
        }

        LocalDate date = parsed.date().get();
        return CommandSupport.executeUseCase(
                () -> markHabitCompleteUseCase.execute(parsed.habitName(), date),
                habit -> CommandResult.success("Marked '" + habit.name() + "' complete on " + date + ".")
        );
    }

    /** Holds a habit name and optional completion date parsed from command arguments. */
    record ParsedDatedHabit(String habitName, Optional<LocalDate> date, boolean invalidDate) {
        static ParsedDatedHabit from(String arguments) {
            if (CommandSupport.isBlank(arguments)) {
                return new ParsedDatedHabit("", Optional.empty(), false);
            }

            String trimmed = arguments.trim();
            int lastWhitespace = trimmed.length() - 1;
            while (lastWhitespace >= 0 && !Character.isWhitespace(trimmed.charAt(lastWhitespace))) {
                lastWhitespace--;
            }
            if (lastWhitespace < 0) {
                Optional<LocalDate> date = CommandSupport.parseIsoDate(trimmed);
                return date.isPresent()
                        ? new ParsedDatedHabit("", date, false)
                        : new ParsedDatedHabit(trimmed, Optional.empty(), looksLikeDate(trimmed));
            }

            String habitName = trimmed.substring(0, lastWhitespace).trim();
            String dateText = trimmed.substring(lastWhitespace + 1).trim();
            Optional<LocalDate> date = CommandSupport.parseIsoDate(dateText);
            if (date.isPresent()) {
                return new ParsedDatedHabit(habitName, date, false);
            }
            return looksLikeDate(dateText)
                    ? new ParsedDatedHabit(habitName, Optional.empty(), true)
                    : new ParsedDatedHabit(trimmed, Optional.empty(), false);
        }

        boolean missingName() {
            return CommandSupport.isBlank(habitName);
        }

        private static boolean looksLikeDate(String value) {
            return value.matches("\\d{1,4}[-/]\\d{1,2}[-/]\\d{1,4}");
        }
    }
}
