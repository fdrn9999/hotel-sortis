package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
    List<Battle> findByPlayerIdAndStatus(Long playerId, Battle.Status status);
    Optional<Battle> findByIdAndPlayerId(Long id, Long playerId);
    int countByPlayerIdAndFloorAndStatus(Long playerId, Integer floor, Battle.Status status);

    /**
     * 진행 중인 PvP 전투 찾기 (재접속 처리용)
     *
     * player1 또는 player2로 참여 중인 ONGOING 상태의 PvP 전투를 찾습니다.
     */
    @Query("SELECT b FROM Battle b WHERE " +
           "(b.player.id = :playerId OR b.enemyId = :playerId) " +
           "AND b.type = :type " +
           "AND b.status = :status")
    Optional<Battle> findOngoingPvPBattle(
        @Param("playerId") Long playerId,
        @Param("type") Battle.Type type,
        @Param("status") Battle.Status status
    );
}
