package com.example.habitzone.command;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Handles the {@code help} command by listing the registered command usages. */
public class HelpCommand implements Command {
    private static final String KEYBOARD_CONTROLS = String.join(System.lineSeparator(),
            "Keyboard controls:",
            "Up / Down (command field): navigate submitted commands",
            "Up / Down (habit list): select a habit and show its history",
            "Left / Right (habit list): scroll long habit names",
            "Shift+Left / Shift+Right: focus the habits / history panel",
            "Shift+Down: focus the command field",
            "Shift+Up: return to the last focused upper panel");

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
        String separator = System.lineSeparator();
        return CommandResult.success("Available commands:" + separator + usages
                + separator + separator + KEYBOARD_CONTROLS);
    }
}
