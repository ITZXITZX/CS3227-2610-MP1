package com.example.habitzone.usecase;

import com.example.habitzone.port.ClockProvider;
import com.example.habitzone.port.HabitRepository;

import java.util.List;
import java.util.Objects;

/** Retrieves the user's habits as sorted presentation snapshots. */
public class ViewHabitsUseCase {
    private final HabitRepository repository;
    private final ClockProvider clockProvider;

    public ViewHabitsUseCase(HabitRepository repository, ClockProvider clockProvider) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.clockProvider = Objects.requireNonNull(clockProvider, "clockProvider");
    }

    public UseCaseResult<List<HabitSnapshot>> execute() {
        java.time.LocalDate today = clockProvider.currentDate();
        List<HabitSnapshot> snapshots = HabitLookup.sortedByDisplayOrder(repository.loadAll()).stream()
                .map(habit -> HabitSnapshot.from(habit, habit.isCompleteOn(today)))
                .toList();
        return UseCaseResult.success(snapshots);
    }
}
