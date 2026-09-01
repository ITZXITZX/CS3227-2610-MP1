package com.example.habitzone.infrastructure;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.port.HabitRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Persists the user's habits in a JSON file. */
public class JsonHabitRepository implements HabitRepository {
    private static final Path DEFAULT_PATH = Path.of("data", "habits.json");

    private final Path path;

    public JsonHabitRepository() {
        this(DEFAULT_PATH);
    }

    public JsonHabitRepository(Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    @Override
    public List<Habit> loadAll() {
        try {
            ensureDataFileExists();
            String json = Files.readString(path, StandardCharsets.UTF_8);
            if (json.isBlank()) {
                return List.of();
            }

            Object parsed = Json.parse(json);
            if (!(parsed instanceof List<?> values)) {
                throw new JsonParseException("Expected top-level array");
            }

            List<Habit> habits = new ArrayList<>();
            for (Object value : values) {
                habits.add(toHabit(value));
            }
            return habits;
        } catch (IOException | RuntimeException e) {
            if (e instanceof StorageException storageException) {
                throw storageException;
            }
            throw new StorageException("Failed to load habits from " + path, e);
        }
    }

    /** Creates the configured data file as an empty JSON array on first use. */
    private void ensureDataFileExists() throws IOException {
        if (Files.exists(path)) {
            return;
        }

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, "[]\n", StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE_NEW);
    }

    @Override
    public void saveAll(List<Habit> habits) {
        Objects.requireNonNull(habits, "habits");

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, toJson(habits), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            if (e instanceof StorageException storageException) {
                throw storageException;
            }
            throw new StorageException("Failed to save habits to " + path, e);
        }
    }

    private static Habit toHabit(Object value) {
        if (!(value instanceof Map<?, ?> habitObject)) {
            throw new JsonParseException("Expected habit object");
        }

        String id = requiredString(habitObject, "id");
        String name = requiredString(habitObject, "name");
        List<LocalDate> completionDates = completionDates(habitObject.get("completionDates"));
        return new Habit(new HabitId(id), name, completionDates);
    }

    private static String requiredString(Map<?, ?> object, String key) {
        Object value = object.get(key);
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new JsonParseException("Expected string field: " + key);
    }

    private static List<LocalDate> completionDates(Object value) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> dateValues)) {
            throw new JsonParseException("Expected completionDates array");
        }

        List<LocalDate> dates = new ArrayList<>();
        for (Object dateValue : dateValues) {
            if (!(dateValue instanceof String dateString)) {
                throw new JsonParseException("Expected completion date string");
            }
            dates.add(LocalDate.parse(dateString));
        }
        return dates;
    }

    private static String toJson(List<Habit> habits) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        for (int i = 0; i < habits.size(); i++) {
            Habit habit = Objects.requireNonNull(habits.get(i), "habit");
            json.append("  {\n");
            appendStringField(json, "id", habit.id().value(), true);
            appendStringField(json, "name", habit.name(), true);
            appendCompletionDates(json, habit.completionDatesAscending());
            json.append("  }");
            if (i < habits.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("]\n");
        return json.toString();
    }

    private static void appendStringField(StringBuilder json, String name, String value, boolean trailingComma) {
        json.append("    \"").append(name).append("\": \"").append(escape(value)).append('"');
        if (trailingComma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void appendCompletionDates(StringBuilder json, List<LocalDate> dates) {
        json.append("    \"completionDates\": [");
        for (int i = 0; i < dates.size(); i++) {
            json.append('"').append(dates.get(i)).append('"');
            if (i < dates.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]\n");
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }

    /** Minimal JSON serializer and parser used for the repository's storage format. */
    private static final class Json {
        private final String input;
        private int index;

        private Json(String input) {
            this.input = input;
        }

        static Object parse(String input) {
            Json parser = new Json(input);
            Object value = parser.parseValue();
            parser.skipWhitespace();
            if (!parser.isAtEnd()) {
                throw parser.error("Unexpected trailing content");
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (isAtEnd()) {
                throw error("Expected JSON value");
            }

            char current = input.charAt(index);
            return switch (current) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 'n' -> parseNull();
                default -> throw error("Unexpected character");
            };
        }

        private Map<String, Object> parseObject() {
            expect('{');
            java.util.LinkedHashMap<String, Object> object = new java.util.LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                index++;
                return object;
            }

            while (true) {
                skipWhitespace();
                if (!peek('"')) {
                    throw error("Expected object key");
                }
                String key = parseString();
                skipWhitespace();
                expect(':');
                object.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> array = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                index++;
                return array;
            }

            while (true) {
                array.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    return array;
                }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder value = new StringBuilder();
            while (!isAtEnd()) {
                char current = input.charAt(index++);
                if (current == '"') {
                    return value.toString();
                }
                if (current == '\\') {
                    value.append(parseEscape());
                } else {
                    if (current < 0x20) {
                        throw error("Unescaped control character in string");
                    }
                    value.append(current);
                }
            }
            throw error("Unterminated string");
        }

        private char parseEscape() {
            if (isAtEnd()) {
                throw error("Unterminated escape sequence");
            }
            char escaped = input.charAt(index++);
            return switch (escaped) {
                case '"' -> '"';
                case '\\' -> '\\';
                case '/' -> '/';
                case 'b' -> '\b';
                case 'f' -> '\f';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                case 'u' -> parseUnicodeEscape();
                default -> throw error("Invalid escape sequence");
            };
        }

        private char parseUnicodeEscape() {
            if (index + 4 > input.length()) {
                throw error("Incomplete unicode escape");
            }
            String hex = input.substring(index, index + 4);
            try {
                index += 4;
                return (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException e) {
                throw error("Invalid unicode escape");
            }
        }

        private Object parseNull() {
            if (input.startsWith("null", index)) {
                index += 4;
                return null;
            }
            throw error("Invalid null literal");
        }

        private void expect(char expected) {
            skipWhitespace();
            if (isAtEnd() || input.charAt(index) != expected) {
                throw error("Expected '" + expected + "'");
            }
            index++;
        }

        private boolean peek(char expected) {
            return !isAtEnd() && input.charAt(index) == expected;
        }

        private void skipWhitespace() {
            while (!isAtEnd() && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
        }

        private boolean isAtEnd() {
            return index >= input.length();
        }

        private JsonParseException error(String message) {
            return new JsonParseException(message + " at character " + index);
        }
    }

    /** Signals malformed JSON encountered while reading persisted habit data. */
    private static final class JsonParseException extends RuntimeException {
        JsonParseException(String message) {
            super(message);
        }
    }
}
