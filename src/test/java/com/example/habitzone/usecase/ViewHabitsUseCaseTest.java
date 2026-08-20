package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewHabitsUseCaseTest {
    @Test
    void viewsEmptyHabitList() {
        FakeHabitRepository repository = new FakeHabitRepository();

        UseCaseResult<List<HabitSnapshot>> result = new ViewHabitsUseCase(repository).execute();

        assertTrue(result.success());
        assertTrue(result.data().isEmpty());
    }

    @Test
    void viewsHabitsSortedByName() {
        FakeHabitRepository repository = new FakeHabitRepository();
        repository.seed(new Habit(new HabitId("habit-1"), "Write"));
        repository.seed(new Habit(new HabitId("habit-2"), "Read"));

        UseCaseResult<List<HabitSnapshot>> result = new ViewHabitsUseCase(repository).execute();

        assertEquals(List.of("Read", "Write"), result.data().stream().map(HabitSnapshot::name).toList());
    }
}
