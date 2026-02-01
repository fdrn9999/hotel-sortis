package com.hotelsortis.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ELO 등급 계산기
 *
 * - K-factor: 32
 * - 승리: 약 +25, 패배: 약 -25 (상대 ELO에 따라 변동)
 * - 무승부: ±0 (약간의 변동 가능)
 */
@Slf4j
@Component
public class EloCalculator {

    private static final int K_FACTOR = 32;

    /**
     * ELO 변화량 계산
     *
     * @param playerElo 플레이어 현재 ELO
     * @param opponentElo 상대 현재 ELO
     * @param result 게임 결과 (1.0 = 승리, 0.5 = 무승부, 0.0 = 패배)
     * @return ELO 변화량 (양수면 상승, 음수면 하락)
     */
    public int calculateEloChange(int playerElo, int opponentElo, double result) {
        // 예상 승률 계산
        double expectedScore = calculateExpectedScore(playerElo, opponentElo);

        // ELO 변화량
        double change = K_FACTOR * (result - expectedScore);

        int roundedChange = (int) Math.round(change);

        log.debug("ELO calculation: playerElo={}, opponentElo={}, result={}, expected={}, change={}",
            playerElo, opponentElo, result, expectedScore, roundedChange);

        return roundedChange;
    }

    /**
     * 예상 승률 계산
     *
     * @param playerElo 플레이어 ELO
     * @param opponentElo 상대 ELO
     * @return 예상 승률 (0.0 ~ 1.0)
     */
    private double calculateExpectedScore(int playerElo, int opponentElo) {
        return 1.0 / (1.0 + Math.pow(10, (opponentElo - playerElo) / 400.0));
    }

    /**
     * 승리 시 ELO 변화량
     */
    public int calculateWinChange(int playerElo, int opponentElo) {
        return calculateEloChange(playerElo, opponentElo, 1.0);
    }

    /**
     * 패배 시 ELO 변화량
     */
    public int calculateLossChange(int playerElo, int opponentElo) {
        return calculateEloChange(playerElo, opponentElo, 0.0);
    }

    /**
     * 무승부 시 ELO 변화량
     */
    public int calculateDrawChange(int playerElo, int opponentElo) {
        return calculateEloChange(playerElo, opponentElo, 0.5);
    }
}
