package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.domain.HabitId;
import com.example.habitzone.port.HabitRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddHabitUseCase {
    private final HabitRepository repository;

    public AddHabitUseCase(HabitRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public UseCaseResult<HabitSnapshot> execute(String habitName) {
        if (HabitLookup.isInvalidName(habitName)) {
            return UseCaseResult.failure(UseCaseError.INVALID_HABIT_NAME);
        }

        List<Habit> habits = new ArrayList<>(repository.loadAll());
        if (HabitLookup.findByName(habits, habitName).isPresent()) {
            return UseCaseResult.failure(UseCaseError.DUPLICATE_HABIT);
        }

        Habit habit = new Habit(HabitId.newId(), habitName);
        habits.add(habit);
        repository.saveAll(habits);
        return UseCaseResult.success(HabitSnapshot.from(habit));
    }
}
