package com.example.habitzone.usecase;

import com.example.habitzone.port.HabitRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ViewHabitsUseCase {
    private final HabitRepository repository;

    public ViewHabitsUseCase(HabitRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public UseCaseResult<List<HabitSnapshot>> execute() {
        List<HabitSnapshot> snapshots = repository.loadAll().stream()
                .sorted(Comparator.comparing(habit -> habit.name().toLowerCase()))
                .map(HabitSnapshot::from)
                .toList();
        return UseCaseResult.success(snapshots);
    }
}
