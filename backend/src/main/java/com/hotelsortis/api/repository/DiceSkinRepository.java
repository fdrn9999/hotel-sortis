package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.DiceSkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 주사위 스킨 리포지토리
 */
@Repository
public interface DiceSkinRepository extends JpaRepository<DiceSkin, Long> {

    /**
     * 스킨 코드로 조회
     */
    Optional<DiceSkin> findBySkinCode(String skinCode);

    /**
     * 희귀도별 조회
     */
    List<DiceSkin> findByRarity(DiceSkin.Rarity rarity);

    /**
     * 구매 가능한 스킨 조회
     */
    List<DiceSkin> findByIsAvailableTrue();

    /**
     * 기본 스킨 조회
     */
    Optional<DiceSkin> findByIsDefaultTrue();

    /**
     * 플레이어가 보유하지 않은 스킨 조회
     */
    @Query("SELECT d FROM DiceSkin d WHERE d.isAvailable = true AND d.id NOT IN " +
            "(SELECT pc.cosmeticId FROM PlayerCosmetic pc WHERE pc.player.id = :playerId " +
            "AND pc.cosmeticType = 'DICE_SKIN')")
    List<DiceSkin> findUnownedByPlayer(@Param("playerId") Long playerId);

    /**
     * 플레이어가 보유한 스킨 조회
     */
    @Query("SELECT d FROM DiceSkin d WHERE d.id IN " +
            "(SELECT pc.cosmeticId FROM PlayerCosmetic pc WHERE pc.player.id = :playerId " +
            "AND pc.cosmeticType = 'DICE_SKIN')")
    List<DiceSkin> findOwnedByPlayer(@Param("playerId") Long playerId);
}
