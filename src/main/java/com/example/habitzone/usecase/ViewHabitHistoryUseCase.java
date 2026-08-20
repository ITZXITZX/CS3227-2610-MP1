package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.port.HabitRepository;

import java.util.Objects;

public class ViewHabitHistoryUseCase {
    private final HabitRepository repository;

    public ViewHabitHistoryUseCase(HabitRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public UseCaseResult<HabitHistory> execute(String habitName) {
        Habit habit = HabitLookup.findByName(repository.loadAll(), habitName).orElse(null);
        if (habit == null) {
            return UseCaseResult.failure(UseCaseError.HABIT_NOT_FOUND);
        }

        return UseCaseResult.success(new HabitHistory(
                habit.id(),
                habit.name(),
                habit.completionHistoryDescending()
        ));
    }
}
