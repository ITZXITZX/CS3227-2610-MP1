package com.example.habitzone.command;

import com.example.habitzone.usecase.ViewHabitHistoryUseCase;

import java.util.Objects;

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
        return "history HABIT_NAME";
    }

    @Override
    public CommandResult execute(String arguments) {
        if (CommandSupport.isBlank(arguments)) {
            return CommandResult.failure(CommandMessages.MISSING_HABIT_NAME);
        }

        return CommandSupport.executeUseCase(
                () -> viewHabitHistoryUseCase.execute(arguments),
                history -> CommandResult.history("Showing history for '" + history.habitName() + "'.", history)
        );
    }
}
