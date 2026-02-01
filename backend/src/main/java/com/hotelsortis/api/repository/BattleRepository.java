package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
    List<Battle> findByPlayerIdAndStatus(Long playerId, Battle.Status status);
    Optional<Battle> findByIdAndPlayerId(Long id, Long playerId);
    int countByPlayerIdAndFloorAndStatus(Long playerId, Integer floor, Battle.Status status);
}
