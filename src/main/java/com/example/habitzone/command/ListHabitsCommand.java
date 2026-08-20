package com.example.habitzone.command;

import com.example.habitzone.usecase.ViewHabitsUseCase;

import java.util.Objects;

public class ListHabitsCommand implements Command {
    private final ViewHabitsUseCase viewHabitsUseCase;

    public ListHabitsCommand(ViewHabitsUseCase viewHabitsUseCase) {
        this.viewHabitsUseCase = Objects.requireNonNull(viewHabitsUseCase, "viewHabitsUseCase");
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String usage() {
        return "list";
    }

    @Override
    public CommandResult execute(String arguments) {
        return CommandSupport.executeUseCase(
                viewHabitsUseCase::execute,
                habits -> {
                    String message = habits.isEmpty()
                            ? "You do not have any habits yet."
                            : "Found " + habits.size() + " habit" + (habits.size() == 1 ? "." : "s.");
                    return CommandResult.habits(message, habits);
                }
        );
    }
}
