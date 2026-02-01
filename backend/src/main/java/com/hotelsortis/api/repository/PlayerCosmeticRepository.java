package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.PlayerCosmetic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 플레이어 코스메틱 리포지토리
 */
@Repository
public interface PlayerCosmeticRepository extends JpaRepository<PlayerCosmetic, Long> {

    /**
     * 플레이어가 소유한 모든 코스메틱 조회
     */
    List<PlayerCosmetic> findByPlayerId(Long playerId);

    /**
     * 플레이어가 소유한 특정 타입 코스메틱 조회
     */
    List<PlayerCosmetic> findByPlayerIdAndCosmeticType(Long playerId, PlayerCosmetic.CosmeticType cosmeticType);

    /**
     * 특정 코스메틱 소유 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM PlayerCosmetic pc " +
            "WHERE pc.player.id = :playerId AND pc.cosmeticType = :cosmeticType " +
            "AND pc.cosmeticId = :cosmeticId")
    boolean existsByPlayerIdAndCosmeticTypeAndCosmeticId(
            @Param("playerId") Long playerId,
            @Param("cosmeticType") PlayerCosmetic.CosmeticType cosmeticType,
            @Param("cosmeticId") Long cosmeticId
    );

    /**
     * 특정 코스메틱 조회
     */
    Optional<PlayerCosmetic> findByPlayerIdAndCosmeticTypeAndCosmeticId(
            Long playerId,
            PlayerCosmetic.CosmeticType cosmeticType,
            Long cosmeticId
    );
}
