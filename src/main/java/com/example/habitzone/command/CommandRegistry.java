package com.example.habitzone.command;

import com.example.habitzone.port.ClockProvider;
import com.example.habitzone.port.HabitRepository;
import com.example.habitzone.usecase.AddHabitUseCase;
import com.example.habitzone.usecase.DeleteHabitUseCase;
import com.example.habitzone.usecase.MarkHabitCompleteUseCase;
import com.example.habitzone.usecase.UnmarkHabitCompleteUseCase;
import com.example.habitzone.usecase.ViewHabitHistoryUseCase;
import com.example.habitzone.usecase.ViewHabitsUseCase;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CommandRegistry {
    private final CommandParser parser;
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandRegistry() {
        this(new CommandParser());
    }

    public CommandRegistry(CommandParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    public static CommandRegistry withUseCases(
            AddHabitUseCase addHabitUseCase,
            DeleteHabitUseCase deleteHabitUseCase,
            ViewHabitsUseCase viewHabitsUseCase,
            MarkHabitCompleteUseCase markHabitCompleteUseCase,
            UnmarkHabitCompleteUseCase unmarkHabitCompleteUseCase,
            ViewHabitHistoryUseCase viewHabitHistoryUseCase
    ) {
        CommandRegistry registry = new CommandRegistry();
        registry.register(new AddHabitCommand(addHabitUseCase));
        registry.register(new DeleteHabitCommand(deleteHabitUseCase));
        registry.register(new ListHabitsCommand(viewHabitsUseCase));
        registry.register(new MarkCompleteCommand(markHabitCompleteUseCase));
        registry.register(new UnmarkCompleteCommand(unmarkHabitCompleteUseCase));
        registry.register(new ViewHistoryCommand(viewHabitHistoryUseCase));
        registry.register(new ExitCommand());
        registry.register(new HelpCommand(registry::commands));
        return registry;
    }

    public static CommandRegistry withRepository(HabitRepository repository, ClockProvider clockProvider) {
        return withUseCases(
                new AddHabitUseCase(repository),
                new DeleteHabitUseCase(repository),
                new ViewHabitsUseCase(repository),
                new MarkHabitCompleteUseCase(repository, clockProvider),
                new UnmarkHabitCompleteUseCase(repository, clockProvider),
                new ViewHabitHistoryUseCase(repository)
        );
    }

    public void register(Command command) {
        commands.put(command.name(), command);
    }

    public CommandResult execute(String input) {
        CommandParser.ParsedCommand parsed = parser.parse(input);
        if (parsed.name().isBlank()) {
            return CommandResult.failure(CommandMessages.EMPTY_COMMAND);
        }

        Command command = commands.get(parsed.name());
        if (command == null) {
            return CommandResult.failure(CommandMessages.UNKNOWN_COMMAND);
        }
        return command.execute(parsed.arguments());
    }

    public Collection<Command> commands() {
        return commands.values();
    }
}
