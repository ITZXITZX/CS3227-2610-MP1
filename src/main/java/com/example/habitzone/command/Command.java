package com.example.habitzone.command;

/** Defines a command that can be invoked from the application's command interface. */
/** Defines a command that can be invoked from the application's command interface. */
public interface Command {
    String name();

    String usage();

    CommandResult execute(String arguments);
}
