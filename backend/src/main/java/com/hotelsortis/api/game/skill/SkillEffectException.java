package com.hotelsortis.api.game.skill;

/**
 * 스킬 효과 실행 중 발생하는 예외
 */
public class SkillEffectException extends RuntimeException {
    private final Long skillId;
    private final String skillName;

    public SkillEffectException(Long skillId, String skillName, String message) {
        super(String.format("Skill effect error [%d: %s]: %s", skillId, skillName, message));
        this.skillId = skillId;
        this.skillName = skillName;
    }

    public SkillEffectException(Long skillId, String skillName, String message, Throwable cause) {
        super(String.format("Skill effect error [%d: %s]: %s", skillId, skillName, message), cause);
        this.skillId = skillId;
        this.skillName = skillName;
    }

    public Long getSkillId() {
        return skillId;
    }

    public String getSkillName() {
        return skillName;
    }
}
