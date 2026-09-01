package com.example.habitzone.command;

import com.example.habitzone.usecase.ViewHabitHistoryUseCase;

import java.util.Objects;

/** Handles the {@code history} command by displaying a habit's completion history. */
public class ViewHistoryCommand implements Command {
    private final ViewHabitHistoryUseCase viewHabitHistoryUseCase;

    public ViewHistoryCommand(ViewHabitHistoryUseCase viewHabitHistoryUseCase) {
        this.viewHabitHistoryUseCase = Objects.requireNonNull(viewHabitHistoryUseCase, "viewHabitHistoryUseCase");
    }

    @Override
    public String name() {
        return "history";
    }

    @Override
    public String usage() {
        return "history HABIT_INDEX_OR_NAME";
    }

    @Override
    public CommandResult execute(String arguments) {
        if (CommandSupport.isBlank(arguments)) {
            return CommandResult.failure(CommandMessages.requiredInput(usage()));
        }

        return CommandSupport.executeUseCase(
                () -> viewHabitHistoryUseCase.execute(arguments),
                history -> CommandResult.history("Showing history for '" + history.habitName() + "'.", history)
        );
    }
}
