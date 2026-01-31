package com.hotelsortis.api.game.skill;

/**
 * 스킬 발동 트리거 타입
 *
 * PROJECTPLAN.md 4.2절 참조
 */
public enum SkillTrigger {
    /**
     * 전투 시작 시 1회 발동
     * 예: "Lucky Reroll" - 전투 시작 시 주사위 1개 재굴림
     */
    BATTLE_START,

    /**
     * 매 턴 시작 시 발동
     * 예: 턴마다 버프/디버프 적용
     */
    TURN_START,

    /**
     * 주사위를 굴린 직후 발동
     * 예: "Steady Hand" - 1을 2로 변경
     */
    DICE_ROLL,

    /**
     * 데미지 계산 전 발동 (족보 판정 직후)
     * 예: "Pair Master" - 페어일 때 데미지 +10
     */
    BEFORE_DAMAGE,

    /**
     * 데미지 처리 후 발동
     * 예: HP 변화 후 추가 효과
     */
    AFTER_DAMAGE,

    /**
     * 항상 적용되는 패시브 효과
     * 예: 최대 HP 증가, 주사위 개수 증가 (PROJECTPLAN에는 없지만 확장 가능)
     */
    PASSIVE
}
