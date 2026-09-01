package com.example.habitzone.command;

import com.example.habitzone.usecase.AddHabitUseCase;

import java.util.Objects;

/**
 * Handles the {@code add} command by delegating habit creation to the add-habit use case.
 */
public class AddHabitCommand implements Command {
    private final AddHabitUseCase addHabitUseCase;

    /**
     * Creates an add-habit command.
     *
     * @param addHabitUseCase use case that creates the requested habit
     */
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
            return CommandResult.failure(CommandMessages.requiredInput(usage()));
        }

        return CommandSupport.executeUseCase(
                () -> addHabitUseCase.execute(arguments),
                habit -> CommandResult.success("Added habit '" + habit.name() + "'.")
        );
    }
}
