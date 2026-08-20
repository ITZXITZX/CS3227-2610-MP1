package com.example.habitzone.command;

import com.example.habitzone.usecase.DeleteHabitUseCase;

import java.util.Objects;

public class DeleteHabitCommand implements Command {
    private final DeleteHabitUseCase deleteHabitUseCase;

    public DeleteHabitCommand(DeleteHabitUseCase deleteHabitUseCase) {
        this.deleteHabitUseCase = Objects.requireNonNull(deleteHabitUseCase, "deleteHabitUseCase");
    }

    @Override
    public String name() {
        return "delete";
    }

    @Override
    public String usage() {
        return "delete HABIT_NAME";
    }

    @Override
    public CommandResult execute(String arguments) {
        if (CommandSupport.isBlank(arguments)) {
            return CommandResult.failure(CommandMessages.MISSING_HABIT_NAME);
        }

        return CommandSupport.executeUseCase(
                () -> deleteHabitUseCase.execute(arguments),
                habit -> CommandResult.success("Deleted habit '" + habit.name() + "'.")
        );
    }
}
