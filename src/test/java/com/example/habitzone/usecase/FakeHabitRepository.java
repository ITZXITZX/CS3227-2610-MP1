package com.example.habitzone.usecase;

import com.example.habitzone.domain.Habit;
import com.example.habitzone.port.HabitRepository;

import java.util.ArrayList;
import java.util.List;

class FakeHabitRepository implements HabitRepository {
    private List<Habit> habits = new ArrayList<>();
    private int saveCount;

    @Override
    public List<Habit> loadAll() {
        return new ArrayList<>(habits);
    }

    @Override
    public void saveAll(List<Habit> habits) {
        this.habits = new ArrayList<>(habits);
        saveCount++;
    }

    void seed(Habit habit) {
        habits.add(habit);
    }

    int saveCount() {
        return saveCount;
    }
}
