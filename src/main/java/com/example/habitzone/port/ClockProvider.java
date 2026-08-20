package com.example.habitzone.port;

import java.time.LocalDate;

/**
 * Boundary for obtaining the current date without coupling application code to the system clock.
 */
public interface ClockProvider {
    LocalDate currentDate();
}
