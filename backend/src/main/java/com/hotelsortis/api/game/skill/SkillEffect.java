package com.hotelsortis.api.game.skill;

/**
 * 스킬 효과 인터페이스
 * 모든 스킬 효과 클래스는 이 인터페이스를 구현합니다.
 *
 * CLAUDE.md 1.2.2절 참조
 */
public interface SkillEffect {
    /**
     * 스킬 효과 실행
     *
     * @param state 현재 게임 상태 (읽기/쓰기 가능)
     * @return 수정된 게임 상태
     * @throws SkillEffectException 스킬 실행 중 오류 발생 시
     */
    GameState apply(GameState state) throws SkillEffectException;

    /**
     * 스킬 ID 반환
     * DB의 skills 테이블 id와 일치해야 함
     */
    Long getSkillId();

    /**
     * 스킬 이름 반환 (디버깅용)
     */
    String getSkillName();

    /**
     * 스킬 트리거 타입 반환
     */
    SkillTrigger getTrigger();

    /**
     * 스킬 발동 가능 여부 확인
     * 기본적으로 항상 true이지만, 특정 조건에서만 발동하는 스킬은 override
     *
     * @param state 현재 게임 상태
     * @return 발동 가능하면 true
     */
    default boolean canApply(GameState state) {
        return true;
    }

    /**
     * 스킬 효과 설명 반환 (로그용)
     *
     * @param state 현재 게임 상태
     * @return 스킬이 어떤 효과를 발휘했는지 설명
     */
    default String getEffectDescription(GameState state) {
        return getSkillName() + " activated";
    }
}
