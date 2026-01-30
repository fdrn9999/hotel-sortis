package com.hotelsortis.api.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Evaluates dice hands according to Chinchiro rules.
 * Hand Rankings (highest to lowest):
 * 1. Ace [1-1-1]: 180 power
 * 2. Triple [X-X-X]: X * 30 power
 * 3. Straight [4-5-6]: 180 power
 * 4. Storm [1-2-3]: 150 power
 * 5. Pair [X-X-Y]: X * 15 power
 * 6. No Hand: sum of dice
 */
@Component
public class HandEvaluator {

    @Data
    @AllArgsConstructor
    public static class HandResult {
        private HandRank rank;
        private String rankKR;
        private int power;
    }

    public enum HandRank {
        ACE("Ace", "에이스"),
        TRIPLE("Triple", "트리플"),
        STRAIGHT("Straight", "스트레이트"),
        STORM("Storm", "스톰"),
        PAIR("Pair", "페어"),
        NO_HAND("NoHand", "노 핸드");

        private final String nameEn;
        private final String nameKr;

        HandRank(String nameEn, String nameKr) {
            this.nameEn = nameEn;
            this.nameKr = nameKr;
        }

        public String getNameEn() { return nameEn; }
        public String getNameKr() { return nameKr; }
    }

    /**
     * Evaluates a hand of 3 dice.
     * @param dice Array of 3 dice values (1-6)
     * @return HandResult containing rank and power
     */
    public HandResult evaluate(int[] dice) {
        if (dice == null || dice.length != 3) {
            throw new IllegalArgumentException("Dice array must contain exactly 3 values");
        }

        int[] sorted = dice.clone();
        Arrays.sort(sorted);
        int a = sorted[0];
        int b = sorted[1];
        int c = sorted[2];

        // 1. Ace: [1-1-1]
        if (a == 1 && b == 1 && c == 1) {
            return new HandResult(HandRank.ACE, "에이스", 180);
        }

        // 2. Triple: Same 3 [2-6]
        if (a == b && b == c && a >= 2) {
            return new HandResult(HandRank.TRIPLE, "트리플", a * 30);
        }

        // 3. Straight: [4-5-6]
        if (a == 4 && b == 5 && c == 6) {
            return new HandResult(HandRank.STRAIGHT, "스트레이트", 180);
        }

        // 4. Storm: [1-2-3]
        if (a == 1 && b == 2 && c == 3) {
            return new HandResult(HandRank.STORM, "스톰", 150);
        }

        // 5. Pair: Same 2
        if (a == b || b == c) {
            int pairValue = (a == b) ? a : b;
            return new HandResult(HandRank.PAIR, "페어", pairValue * 15);
        }

        // 6. No Hand: Sum
        return new HandResult(HandRank.NO_HAND, "노 핸드", a + b + c);
    }

    /**
     * Rolls 3 dice using secure random.
     * @return Array of 3 random dice values (1-6)
     */
    public int[] rollDice() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        return new int[] {
            random.nextInt(6) + 1,
            random.nextInt(6) + 1,
            random.nextInt(6) + 1
        };
    }
}
