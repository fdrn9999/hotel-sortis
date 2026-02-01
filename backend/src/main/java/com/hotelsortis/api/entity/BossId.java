package com.hotelsortis.api.entity;

import lombok.Getter;

/**
 * Boss ID Enum
 *
 * Defines the three bosses in the campaign:
 * - MAMMON (Floor 5)
 * - ELIGOR (Floor 10)
 * - LUCIFUGE (Floor 15)
 *
 * Each boss has a lowercase string value for database compatibility.
 */
@Getter
public enum BossId {
    MAMMON("mammon"),
    ELIGOR("eligor"),
    LUCIFUGE("lucifuge");

    private final String value;

    BossId(String value) {
        this.value = value;
    }

    /**
     * Get BossId from string value
     *
     * @param value The lowercase boss ID string ("mammon", "eligor", "lucifuge")
     * @return The corresponding BossId enum
     * @throws IllegalArgumentException if value is not a valid boss ID
     */
    public static BossId fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Boss ID value cannot be null");
        }

        for (BossId bossId : values()) {
            if (bossId.value.equals(value)) {
                return bossId;
            }
        }

        throw new IllegalArgumentException("Invalid boss ID: " + value +
            ". Valid values are: mammon, eligor, lucifuge");
    }

    /**
     * Get floor number for this boss
     *
     * @return The floor number where this boss appears
     */
    public int getFloor() {
        switch (this) {
            case MAMMON:
                return 5;
            case ELIGOR:
                return 10;
            case LUCIFUGE:
                return 15;
            default:
                throw new IllegalStateException("Unknown boss: " + this);
        }
    }

    /**
     * Get total phases for this boss
     *
     * @return The number of phases this boss has
     */
    public int getTotalPhases() {
        switch (this) {
            case MAMMON:
                return 2;
            case ELIGOR:
                return 2;
            case LUCIFUGE:
                return 3;
            default:
                throw new IllegalStateException("Unknown boss: " + this);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
