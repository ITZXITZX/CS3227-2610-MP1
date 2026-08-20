package com.example.habitzone.port;

import com.example.habitzone.domain.Habit;

import java.util.List;

/**
 * Boundary for persisting the single user's complete collection of habits.
 * Implementations belong to infrastructure; application code depends only on this contract.
 */
public interface HabitRepository {
    /** Returns every saved habit for the user. */
    List<Habit> loadAll();

    /** Replaces the user's saved habit collection with {@code habits}. */
    void saveAll(List<Habit> habits);
}
