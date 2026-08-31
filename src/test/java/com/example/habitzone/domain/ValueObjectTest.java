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
}
