package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.port.HabitRepository;

import java.util.List;
import java.util.Objects;

public final class ClearHabitExpiryUseCase {
    private final HabitRepository repository;

    public ClearHabitExpiryUseCase(HabitRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public UseCaseResult<HabitSnapshot> execute(String habitName) {
        if (HabitLookup.isInvalidName(habitName)) {
            return UseCaseResult.failure(UseCaseError.INVALID_HABIT_NAME);
        }
        List<Habit> habits = repository.loadAll();
        return HabitLookup.findByName(habits, habitName)
                .map(habit -> {
                    habit.clearExpiryDate();
                    repository.saveAll(habits);
                    return UseCaseResult.success(HabitSnapshot.from(habit));
                })
                .orElseGet(() -> UseCaseResult.failure(UseCaseError.HABIT_NOT_FOUND));
    }
}
