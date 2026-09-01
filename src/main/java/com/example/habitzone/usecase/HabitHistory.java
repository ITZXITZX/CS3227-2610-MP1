package com.example.habitzone.usecase;

import com.example.habitzone.domain.CompletionLog;
import com.example.habitzone.domain.HabitId;

import java.util.List;

/** Immutable completion history prepared for presentation. */
public record HabitHistory(HabitId habitId, String habitName, List<CompletionLog> completions) {
}
