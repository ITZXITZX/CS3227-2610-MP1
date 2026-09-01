package com.example.habitzone.command;
import com.example.habitzone.usecase.ViewHabitStreakUseCase; import java.util.*;
/** Handles the {@code streak} command by reporting a habit's current completion streak. */
public final class StreakCommand implements Command { private final ViewHabitStreakUseCase useCase; public StreakCommand(ViewHabitStreakUseCase useCase){this.useCase=Objects.requireNonNull(useCase);} public String name(){return "streak";} public String usage(){return "streak HABIT_NAME";} public CommandResult execute(String a){if(CommandSupport.isBlank(a))return CommandResult.failure(CommandMessages.requiredInput(usage()));return CommandSupport.executeUseCase(()->useCase.execute(a),n->CommandResult.success("Current streak: "+n+" day(s)."));}}
