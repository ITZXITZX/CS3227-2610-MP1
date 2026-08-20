package com.example.habitzone.command;

import com.example.habitzone.usecase.AddHabitUseCase;

import java.util.Objects;

public class AddHabitCommand implements Command {
    private final AddHabitUseCase addHabitUseCase;

    public AddHabitCommand(AddHabitUseCase addHabitUseCase) {
        this.addHabitUseCase = Objects.requireNonNull(addHabitUseCase, "addHabitUseCase");
    }

    @Override
    public String name() {
        return "add";
    }

    @Override
    public String usage() {
        return "add HABIT_NAME";
    }

    @Override
    public CommandResult execute(String arguments) {
        if (CommandSupport.isBlank(arguments)) {
            return CommandResult.failure(CommandMessages.MISSING_HABIT_NAME);
        }

        return CommandSupport.executeUseCase(
                () -> addHabitUseCase.execute(arguments),
                habit -> CommandResult.success("Added habit '" + habit.name() + "'.")
        );
    }
}
