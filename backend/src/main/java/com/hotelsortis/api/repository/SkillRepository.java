package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 스킬 리포지토리
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * 스킬 코드로 조회
     */
    Optional<Skill> findBySkillCode(String skillCode);

    /**
     * 희귀도별 조회
     */
    List<Skill> findByRarity(Skill.Rarity rarity);

    /**
     * 트리거 타입별 조회
     */
    List<Skill> findByTriggerType(Skill.TriggerType triggerType);

    /**
     * 여러 ID로 조회 (스킬 장착 시 사용)
     */
    @Query("SELECT s FROM Skill s WHERE s.id IN :ids")
    List<Skill> findByIds(@Param("ids") List<Long> ids);

    /**
     * 모든 스킬 조회 (ID 순서)
     */
    @Query("SELECT s FROM Skill s ORDER BY s.id ASC")
    List<Skill> findAllOrderById();
}
