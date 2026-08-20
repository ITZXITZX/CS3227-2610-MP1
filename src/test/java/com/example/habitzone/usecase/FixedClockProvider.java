package com.example.habitzone.usecase;

import com.example.habitzone.port.ClockProvider;

import java.time.LocalDate;

class FixedClockProvider implements ClockProvider {
    private final LocalDate currentDate;

    FixedClockProvider(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    @Override
    public LocalDate currentDate() {
        return currentDate;
    }
}
