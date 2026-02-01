package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 아바타 리포지토리
 */
@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    /**
     * 아바타 코드로 조회
     */
    Optional<Avatar> findByAvatarCode(String avatarCode);

    /**
     * 희귀도별 조회
     */
    List<Avatar> findByRarity(Avatar.Rarity rarity);

    /**
     * 구매 가능한 아바타 조회
     */
    List<Avatar> findByIsAvailableTrue();

    /**
     * 기본 아바타 조회
     */
    Optional<Avatar> findByIsDefaultTrue();

    /**
     * 플레이어가 보유하지 않은 아바타 조회
     */
    @Query("SELECT a FROM Avatar a WHERE a.isAvailable = true AND a.id NOT IN " +
            "(SELECT pc.cosmeticId FROM PlayerCosmetic pc WHERE pc.player.id = :playerId " +
            "AND pc.cosmeticType = 'AVATAR')")
    List<Avatar> findUnownedByPlayer(@Param("playerId") Long playerId);

    /**
     * 플레이어가 보유한 아바타 조회
     */
    @Query("SELECT a FROM Avatar a WHERE a.id IN " +
            "(SELECT pc.cosmeticId FROM PlayerCosmetic pc WHERE pc.player.id = :playerId " +
            "AND pc.cosmeticType = 'AVATAR')")
    List<Avatar> findOwnedByPlayer(@Param("playerId") Long playerId);
}
