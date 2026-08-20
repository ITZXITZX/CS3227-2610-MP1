package com.example.habitzone.infrastructure;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitCategory;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.domain.HabitPriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonHabitRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndReloadsHabitWithOptionalFields() {
        Path path = tempDir.resolve("nested").resolve("habits.json");
        JsonHabitRepository repository = new JsonHabitRepository(path);
        Habit habit = new Habit(
                new HabitId("habit-1"),
                "Read",
                List.of(),
                LocalDate.of(2026, 12, 31),
                new HabitCategory("Learning"),
                HabitPriority.HIGH,
                LocalTime.of(8, 30)
        );

        repository.saveAll(List.of(habit));

        List<Habit> loaded = new JsonHabitRepository(path).loadAll();
        assertEquals(1, loaded.size());
        Habit reloaded = loaded.getFirst();
        assertEquals(habit.id(), reloaded.id());
        assertEquals(habit.name(), reloaded.name());
        assertEquals(habit.expiryDate(), reloaded.expiryDate());
        assertEquals(habit.category(), reloaded.category());
        assertEquals(habit.priority(), reloaded.priority());
        assertEquals(habit.reminderTime(), reloaded.reminderTime());
        assertTrue(Files.exists(path));
    }

    @Test
    void savesAndReloadsCompletionDatesAscending() {
        Path path = tempDir.resolve("habits.json");
        JsonHabitRepository repository = new JsonHabitRepository(path);
        Habit habit = new Habit(
                new HabitId("habit-1"),
                "Run",
                List.of(LocalDate.of(2026, 8, 19), LocalDate.of(2026, 8, 17)),
                null,
                null,
                HabitPriority.NORMAL,
                null
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
}
