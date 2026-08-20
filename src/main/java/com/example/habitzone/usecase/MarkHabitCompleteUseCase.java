package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.port.ClockProvider;
import com.example.habitzone.port.HabitRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MarkHabitCompleteUseCase {
    private final HabitRepository repository;
    private final ClockProvider clockProvider;

    public MarkHabitCompleteUseCase(HabitRepository repository, ClockProvider clockProvider) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.clockProvider = Objects.requireNonNull(clockProvider, "clockProvider");
    }

    public UseCaseResult<HabitSnapshot> execute(String habitName) {
        return execute(habitName, clockProvider.currentDate());
    }

    public UseCaseResult<HabitSnapshot> execute(String habitName, LocalDate date) {
        List<Habit> habits = new ArrayList<>(repository.loadAll());
        Habit habit = HabitLookup.findByName(habits, habitName).orElse(null);
        if (habit == null) {
            return UseCaseResult.failure(UseCaseError.HABIT_NOT_FOUND);
        }

        habit.markComplete(date);
        repository.saveAll(habits);
        return UseCaseResult.success(HabitSnapshot.from(habit));
    }
}
