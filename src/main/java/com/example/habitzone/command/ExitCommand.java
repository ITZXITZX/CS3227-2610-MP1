package com.example.habitzone.command;

/** Handles the {@code exit} command by returning an exit result. */
public class ExitCommand implements Command {
    @Override
    public String name() {
        return "exit";
    }

    @Override
    public String usage() {
        return "exit";
    }

    @Override
    public CommandResult execute(String arguments) {
        return CommandResult.exit("Goodbye.");
    }
}
