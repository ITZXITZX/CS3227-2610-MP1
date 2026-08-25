package com.example.habitzone.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectTest {
    @Test
    void habitIdRejectsNullAndBlankAndGeneratedIdsAreUsable() {
        assertThrows(NullPointerException.class, () -> new HabitId(null));
        assertThrows(IllegalArgumentException.class, () -> new HabitId(" \t"));
        assertNotEquals(HabitId.newId(), HabitId.newId());
    }

    @Test
    void categoryTrimsAndRejectsInvalidNames() {
        assertEquals("Health", new HabitCategory(" Health ").name());
        assertThrows(NullPointerException.class, () -> new HabitCategory(null));
        assertThrows(IllegalArgumentException.class, () -> new HabitCategory(" "));
    }
}
