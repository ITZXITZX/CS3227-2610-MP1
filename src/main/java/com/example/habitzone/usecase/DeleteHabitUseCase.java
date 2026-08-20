package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.port.HabitRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeleteHabitUseCase {
    private final HabitRepository repository;

    public DeleteHabitUseCase(HabitRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public UseCaseResult<HabitSnapshot> execute(String habitName) {
        List<Habit> habits = new ArrayList<>(repository.loadAll());
        Habit habit = HabitLookup.findByName(habits, habitName).orElse(null);
        if (habit == null) {
            return UseCaseResult.failure(UseCaseError.HABIT_NOT_FOUND);
        }

        habits.remove(habit);
        repository.saveAll(habits);
        return UseCaseResult.success(HabitSnapshot.from(habit));
    }
}
