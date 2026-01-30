package com.hotelsortis.api.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Evaluates dice hands according to Chinchiro rules (System A - PROJECTPLAN.md).
 * Hand Rankings (highest to lowest):
 * 1. Ace [1-1-1]: 60 power
 * 2. Triple [X-X-X]: 10 + (X * 5) power (20-40)
 * 3. Straight [4-5-6]: 50 power
 * 4. Strike [3-4-5]: 40 power
 * 5. Slash [2-3-4]: 30 power
 * 6. Storm [1-2-3]: 20 power
 * 7. Pair [X-X-Y]: 5 + (X * 2) power (7-17)
 * 8. No Hand: sum of dice (3-16)
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
        STRIKE("Strike", "스트라이크"),
        SLASH("Slash", "슬래시"),
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

        // 1. Ace: [1-1-1] → 60
        if (a == 1 && b == 1 && c == 1) {
            return new HandResult(HandRank.ACE, "에이스", 60);
        }

        // 2. Triple: Same 3 [2-6] → 10 + (N*5)
        if (a == b && b == c && a >= 2) {
            return new HandResult(HandRank.TRIPLE, "트리플", 10 + (a * 5));
        }

        // 3. Straight: [4-5-6] → 50
        if (a == 4 && b == 5 && c == 6) {
            return new HandResult(HandRank.STRAIGHT, "스트레이트", 50);
        }

        // 4. Strike: [3-4-5] → 40
        if (a == 3 && b == 4 && c == 5) {
            return new HandResult(HandRank.STRIKE, "스트라이크", 40);
        }

        // 5. Slash: [2-3-4] → 30
        if (a == 2 && b == 3 && c == 4) {
            return new HandResult(HandRank.SLASH, "슬래시", 30);
        }

        // 6. Storm: [1-2-3] → 20
        if (a == 1 && b == 2 && c == 3) {
            return new HandResult(HandRank.STORM, "스톰", 20);
        }

        // 7. Pair: Same 2 → 5 + (N*2)
        if (a == b || b == c) {
            int pairValue = (a == b) ? a : b;
            return new HandResult(HandRank.PAIR, "페어", 5 + (pairValue * 2));
        }

        // 8. No Hand: Sum (3-16)
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
