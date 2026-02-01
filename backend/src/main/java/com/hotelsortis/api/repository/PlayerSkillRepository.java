package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.PlayerSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerSkillRepository extends JpaRepository<PlayerSkill, Long> {

    List<PlayerSkill> findByPlayerId(Long playerId);

    boolean existsByPlayerIdAndSkillId(Long playerId, Long skillId);

    @Query("SELECT ps.skillId FROM PlayerSkill ps WHERE ps.playerId = :playerId")
    List<Long> findSkillIdsByPlayerId(@Param("playerId") Long playerId);
}
