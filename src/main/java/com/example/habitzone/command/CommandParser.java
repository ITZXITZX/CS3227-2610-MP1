package com.example.habitzone.command;

/** Splits command-line input into a command name and its arguments. */
public class CommandParser {
    public ParsedCommand parse(String input) {
        if (input == null || input.isBlank()) {
            return new ParsedCommand("", "");
        }

        String trimmed = input.trim();
        int argumentStart = 0;
        while (argumentStart < trimmed.length() && !Character.isWhitespace(trimmed.charAt(argumentStart))) {
            argumentStart++;
        }
        if (argumentStart == trimmed.length()) {
            return new ParsedCommand(trimmed.toLowerCase(), "");
        }

        String commandName = trimmed.substring(0, argumentStart).toLowerCase();
        String arguments = trimmed.substring(argumentStart).trim();
        return new ParsedCommand(commandName, arguments);
    }

    /** Holds the normalized command name and accompanying arguments parsed from input. */
    public record ParsedCommand(String name, String arguments) {
    }
}
