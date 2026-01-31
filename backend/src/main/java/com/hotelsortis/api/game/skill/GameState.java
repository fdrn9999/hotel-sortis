package com.hotelsortis.api.game.skill;

import com.hotelsortis.api.game.HandEvaluator.HandResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임 상태를 나타내는 클래스
 * 스킬 효과가 이 상태를 읽고 수정합니다.
 *
 * CLAUDE.md 1.2.2절 참조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameState {
    /**
     * 현재 주사위 배열 [3개]
     * 예: [3, 5, 2]
     */
    @Builder.Default
    private int[] dice = new int[3];

    /**
     * 족보 판정 결과
     * null일 수 있음 (아직 판정 전)
     */
    private HandResult hand;

    /**
     * 계산된 데미지
     * BEFORE_DAMAGE 트리거에서 수정 가능
     */
    private int damage;

    /**
     * 현재 플레이어 HP
     */
    private int playerHp;

    /**
     * 현재 적 HP
     */
    private int enemyHp;

    /**
     * 현재 턴 수
     */
    private int turnCount;

    /**
     * 현재 턴 ("player" or "enemy")
     */
    private String currentTurn;

    /**
     * 전투 ID
     */
    private Long battleId;

    /**
     * 플레이어 ID
     */
    private Long playerId;

    /**
     * 적 ID
     */
    private Long enemyId;

    /**
     * 재굴림 가능 횟수 (스킬로 증가 가능)
     */
    @Builder.Default
    private int rerollsAvailable = 0;

    /**
     * 이번 턴에 발동된 스킬 ID 목록 (중복 발동 방지용)
     */
    @Builder.Default
    private List<Long> triggeredSkillIds = new ArrayList<>();

    /**
     * 추가 메타데이터 (확장용)
     */
    @Builder.Default
    private java.util.Map<String, Object> metadata = new java.util.HashMap<>();

    /**
     * 주사위 배열 깊은 복사
     */
    public int[] getDiceCopy() {
        return dice.clone();
    }

    /**
     * 특정 주사위 값 변경
     */
    public void setDice(int index, int value) {
        if (index < 0 || index >= dice.length) {
            throw new IllegalArgumentException("Invalid dice index: " + index);
        }
        if (value < 1 || value > 6) {
            throw new IllegalArgumentException("Invalid dice value: " + value);
        }
        dice[index] = value;
    }

    /**
     * 스킬이 이미 발동되었는지 확인
     */
    public boolean hasSkillTriggered(Long skillId) {
        return triggeredSkillIds.contains(skillId);
    }

    /**
     * 스킬 발동 기록
     */
    public void markSkillTriggered(Long skillId) {
        triggeredSkillIds.add(skillId);
    }

    /**
     * 턴 종료 시 초기화 (발동된 스킬 목록 등)
     */
    public void resetTurn() {
        triggeredSkillIds.clear();
        rerollsAvailable = 0;
    }
}
