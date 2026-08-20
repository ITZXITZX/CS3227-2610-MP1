package com.example.habitzone.command;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HelpCommand implements Command {
    private final Supplier<Collection<Command>> commands;

    public HelpCommand(Supplier<Collection<Command>> commands) {
        this.commands = Objects.requireNonNull(commands, "commands");
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String usage() {
        return "help";
    }

    @Override
    public CommandResult execute(String arguments) {
        String usages = commands.get().stream()
                .map(Command::usage)
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
        return CommandResult.success("Available commands:" + System.lineSeparator() + usages);
    }
}
