package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.BlockList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BlockList entity.
 */
@Repository
public interface BlockListRepository extends JpaRepository<BlockList, Long> {

    /**
     * Check if player A blocked player B.
     * Usage: Validate before allowing interaction (chat, friend request).
     */
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /**
     * Find specific block relationship.
     * Usage: Get block details for unblock operation.
     */
    Optional<BlockList> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /**
     * Get all players blocked by a player.
     * Usage: Display block list in settings.
     */
    List<BlockList> findByBlockerId(Long blockerId);

    /**
     * Get blocked player IDs for a player.
     * Usage: Filter out blocked players from lists.
     */
    @Query("SELECT b.blocked.id FROM BlockList b WHERE b.blocker.id = :blockerId")
    List<Long> findBlockedIdsByBlockerId(@Param("blockerId") Long blockerId);

    /**
     * Check if either player blocked the other (bidirectional check).
     * Usage: Prevent matchmaking between players who blocked each other.
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BlockList b " +
           "WHERE (b.blocker.id = :playerId1 AND b.blocked.id = :playerId2) " +
           "OR (b.blocker.id = :playerId2 AND b.blocked.id = :playerId1)")
    boolean existsBidirectional(
        @Param("playerId1") Long playerId1,
        @Param("playerId2") Long playerId2
    );

    /**
     * Delete block relationship.
     * Usage: Unblock player.
     */
    @Modifying
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /**
     * Count total blocks by player.
     * Usage: Rate limiting, abuse prevention.
     */
    int countByBlockerId(Long blockerId);
}
