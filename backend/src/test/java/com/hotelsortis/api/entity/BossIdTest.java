package com.hotelsortis.api.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BossId enum
 */
class BossIdTest {

    @Test
    void testBossIdValues() {
        // Test all three boss IDs exist
        assertEquals(3, BossId.values().length);

        // Test each boss ID value
        assertEquals("mammon", BossId.MAMMON.getValue());
        assertEquals("eligor", BossId.ELIGOR.getValue());
        assertEquals("lucifuge", BossId.LUCIFUGE.getValue());
    }

    @Test
    void testFromValue() {
        // Test valid conversions
        assertEquals(BossId.MAMMON, BossId.fromValue("mammon"));
        assertEquals(BossId.ELIGOR, BossId.fromValue("eligor"));
        assertEquals(BossId.LUCIFUGE, BossId.fromValue("lucifuge"));
    }

    @Test
    void testFromValueInvalid() {
        // Test invalid boss ID
        assertThrows(IllegalArgumentException.class, () -> {
            BossId.fromValue("invalid_boss");
        });
    }

    @Test
    void testFromValueNull() {
        // Test null value
        assertThrows(IllegalArgumentException.class, () -> {
            BossId.fromValue(null);
        });
    }

    @Test
    void testGetFloor() {
        // Test floor numbers
        assertEquals(5, BossId.MAMMON.getFloor());
        assertEquals(10, BossId.ELIGOR.getFloor());
        assertEquals(15, BossId.LUCIFUGE.getFloor());
    }

    @Test
    void testGetTotalPhases() {
        // Test phase counts
        assertEquals(2, BossId.MAMMON.getTotalPhases());
        assertEquals(2, BossId.ELIGOR.getTotalPhases());
        assertEquals(3, BossId.LUCIFUGE.getTotalPhases());
    }

    @Test
    void testToString() {
        // Test toString returns lowercase value
        assertEquals("mammon", BossId.MAMMON.toString());
        assertEquals("eligor", BossId.ELIGOR.toString());
        assertEquals("lucifuge", BossId.LUCIFUGE.toString());
    }
}
