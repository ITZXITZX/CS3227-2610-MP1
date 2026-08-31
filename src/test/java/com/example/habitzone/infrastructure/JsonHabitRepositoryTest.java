package com.example.habitzone.infrastructure;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonHabitRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndReloadsHabit() {
        Path path = tempDir.resolve("nested").resolve("habits.json");
        JsonHabitRepository repository = new JsonHabitRepository(path);
        Habit habit = new Habit(new HabitId("habit-1"), "Read");

        repository.saveAll(List.of(habit));

        List<Habit> loaded = new JsonHabitRepository(path).loadAll();
        assertEquals(1, loaded.size());
        Habit reloaded = loaded.getFirst();
        assertEquals(habit.id(), reloaded.id());
        assertEquals(habit.name(), reloaded.name());
        assertTrue(Files.exists(path));
    }

    @Test
    void savesAndReloadsCompletionDatesAscending() {
        Path path = tempDir.resolve("habits.json");
        JsonHabitRepository repository = new JsonHabitRepository(path);
        Habit habit = new Habit(
                new HabitId("habit-1"),
                "Run",
                List.of(LocalDate.of(2026, 8, 19), LocalDate.of(2026, 8, 17))
        );

        repository.saveAll(List.of(habit));

        List<Habit> loaded = new JsonHabitRepository(path).loadAll();
        assertEquals(
                List.of(LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 19)),
                loaded.getFirst().completionDatesAscending()
        );
    }

    @Test
    void loadsEmptyListWhenFileIsMissing() {
        Path path = tempDir.resolve("missing").resolve("habits.json");
        JsonHabitRepository repository = new JsonHabitRepository(path);

        assertTrue(repository.loadAll().isEmpty());
        assertTrue(Files.exists(path));
    }

    @Test
    void loadsEmptyListWhenFileIsBlank() throws IOException {
        Path path = tempDir.resolve("habits.json");
        Files.writeString(path, " \n\t ", StandardCharsets.UTF_8);
        JsonHabitRepository repository = new JsonHabitRepository(path);

        assertTrue(repository.loadAll().isEmpty());
    }

    @Test
    void wrapsCorruptJsonInStorageException() throws IOException {
        Path path = tempDir.resolve("habits.json");
        Files.writeString(path, "[{\"id\": \"habit-1\", ", StandardCharsets.UTF_8);
        JsonHabitRepository repository = new JsonHabitRepository(path);

        assertThrows(StorageException.class, repository::loadAll);
    }

    @Test
    void roundTripsEscapedTextAndMultipleHabits() {
        Path path = tempDir.resolve("habits.json");
        JsonHabitRepository repository = new JsonHabitRepository(path);
        repository.saveAll(List.of(
                new Habit(new HabitId("one"), "Read \"docs\"\\notes"),
                new Habit(new HabitId("two"), "Run")
        ));

        List<Habit> loaded = repository.loadAll();
        assertEquals(2, loaded.size());
        assertEquals("Read \"docs\"\\notes", loaded.getFirst().name());
    }

    @Test
    void loadsLegacyDataAndDropsRemovedFieldsOnNextSave() throws IOException {
        Path path = tempDir.resolve("habits.json");
        Files.writeString(path, "[{\"id\":\"one\",\"name\":\"Read\",\"expiryDate\":\"2026-12-31\",\"category\":\"Learning\",\"priority\":\"HIGH\",\"reminderTime\":\"08:30\"}]", StandardCharsets.UTF_8);

        JsonHabitRepository repository = new JsonHabitRepository(path);
        Habit habit = repository.loadAll().getFirst();
        assertEquals("Read", habit.name());

        repository.saveAll(List.of(habit));
        String rewritten = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(!rewritten.contains("expiryDate"));
        assertTrue(!rewritten.contains("category"));
        assertTrue(!rewritten.contains("priority"));
        assertTrue(!rewritten.contains("reminderTime"));
    }

    @Test
    void wrapsInvalidShapeAndFieldValuesInStorageException() throws IOException {
        Path path = tempDir.resolve("habits.json");
        Files.writeString(path, "{\"id\":\"one\"}", StandardCharsets.UTF_8);
        assertThrows(StorageException.class, () -> new JsonHabitRepository(path).loadAll());

        Files.writeString(path, "[{\"id\":\"one\",\"name\":\"Read\",\"completionDates\":\"today\"}]", StandardCharsets.UTF_8);
        assertThrows(StorageException.class, () -> new JsonHabitRepository(path).loadAll());
    }
}
