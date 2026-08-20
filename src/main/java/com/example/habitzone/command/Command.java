package com.example.habitzone.command;

public interface Command {
    String name();

    String usage();

    CommandResult execute(String arguments);
}
