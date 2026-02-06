package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.Friend;
import com.hotelsortis.api.entity.Friend.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Friend entity.
 */
@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * Find all friendships where player is the initiator, filtered by status.
     * Usage: Get accepted friends or sent pending requests.
     */
    List<Friend> findByPlayerIdAndStatus(Long playerId, FriendStatus status);

    /**
     * Find pending friend requests received by a player.
     * Usage: Get incoming friend requests.
     */
    List<Friend> findByFriendIdAndStatus(Long friendId, FriendStatus status);

    /**
     * Find specific friendship relationship.
     * Usage: Check if A→B friendship exists.
     */
    Optional<Friend> findByPlayerIdAndFriendId(Long playerId, Long friendId);

    /**
     * Check if any friendship exists between two players (any direction, any status).
     * Usage: Validate before sending new friend request.
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friend f " +
           "WHERE (f.player.id = :playerId AND f.friend.id = :friendId) " +
           "OR (f.player.id = :friendId AND f.friend.id = :playerId)")
    boolean existsBetweenPlayers(
        @Param("playerId") Long playerId,
        @Param("friendId") Long friendId
    );

    /**
     * Delete friendship by player and friend IDs.
     * Usage: Unfriend or cancel friend request.
     */
    @Modifying
    void deleteByPlayerIdAndFriendId(Long playerId, Long friendId);

    /**
     * Count accepted friends for a player.
     * Usage: Display friend count in profile.
     */
    @Query("SELECT COUNT(f) FROM Friend f WHERE f.player.id = :playerId AND f.status = 'ACCEPTED'")
    int countAcceptedFriends(@Param("playerId") Long playerId);

    /**
     * Count pending requests received by a player.
     * Usage: Display notification badge.
     */
    @Query("SELECT COUNT(f) FROM Friend f WHERE f.friend.id = :playerId AND f.status = 'PENDING'")
    int countPendingRequestsReceived(@Param("playerId") Long playerId);
}
