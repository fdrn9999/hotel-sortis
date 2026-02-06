package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.SocialDto;
import com.hotelsortis.api.entity.BlockList;
import com.hotelsortis.api.entity.Friend;
import com.hotelsortis.api.entity.Friend.FriendStatus;
import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.repository.BlockListRepository;
import com.hotelsortis.api.repository.FriendRepository;
import com.hotelsortis.api.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Friend and Block management service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {

    private final FriendRepository friendRepository;
    private final BlockListRepository blockListRepository;
    private final PlayerRepository playerRepository;

    // ==================== Friend Operations ====================

    /**
     * Send a friend request
     */
    @Transactional
    public SocialDto.FriendResponse sendFriendRequest(Long playerId, Long targetPlayerId) {
        log.info("Sending friend request from {} to {}", playerId, targetPlayerId);

        // Validation
        if (playerId.equals(targetPlayerId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        Player targetPlayer = playerRepository.findById(targetPlayerId)
                .orElseThrow(() -> new IllegalArgumentException("Target player not found: " + targetPlayerId));

        // Check if blocked
        if (blockListRepository.existsBidirectional(playerId, targetPlayerId)) {
            throw new IllegalArgumentException("Cannot send friend request to blocked player");
        }

        // Check if relationship already exists
        if (friendRepository.existsBetweenPlayers(playerId, targetPlayerId)) {
            throw new IllegalArgumentException("Friend relationship already exists");
        }

        // Create friend request
        Friend friendRequest = Friend.builder()
                .player(player)
                .friend(targetPlayer)
                .status(FriendStatus.PENDING)
                .build();

        friendRequest = friendRepository.save(friendRequest);
        log.info("Friend request created: {}", friendRequest.getId());

        return SocialDto.FriendResponse.fromEntity(friendRequest, targetPlayer);
    }

    /**
     * Accept a friend request (creates bidirectional relationship)
     */
    @Transactional
    public SocialDto.FriendResponse acceptFriendRequest(Long playerId, Long requestId) {
        log.info("Accepting friend request {} by player {}", requestId, playerId);

        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found: " + requestId));

        // Verify the request is for this player
        if (!friendRequest.getFriend().getId().equals(playerId)) {
            throw new IllegalArgumentException("This friend request is not for you");
        }

        // Verify status is PENDING
        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        LocalDateTime now = LocalDateTime.now();

        // Update original request to ACCEPTED
        friendRequest.setStatus(FriendStatus.ACCEPTED);
        friendRequest.setAcceptedAt(now);
        friendRepository.save(friendRequest);

        // Create reciprocal friendship (bidirectional)
        Friend reciprocal = Friend.builder()
                .player(friendRequest.getFriend())
                .friend(friendRequest.getPlayer())
                .status(FriendStatus.ACCEPTED)
                .acceptedAt(now)
                .build();
        friendRepository.save(reciprocal);

        log.info("Friend request accepted, bidirectional relationship created");

        return SocialDto.FriendResponse.fromEntity(friendRequest, friendRequest.getPlayer());
    }

    /**
     * Decline a friend request (deletes the request)
     */
    @Transactional
    public void declineFriendRequest(Long playerId, Long requestId) {
        log.info("Declining friend request {} by player {}", requestId, playerId);

        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found: " + requestId));

        // Verify the request is for this player
        if (!friendRequest.getFriend().getId().equals(playerId)) {
            throw new IllegalArgumentException("This friend request is not for you");
        }

        // Verify status is PENDING
        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        friendRepository.delete(friendRequest);
        log.info("Friend request declined and deleted");
    }

    /**
     * Remove a friend (removes bidirectional relationship)
     */
    @Transactional
    public void removeFriend(Long playerId, Long friendId) {
        log.info("Removing friend {} for player {}", friendId, playerId);

        // Delete both directions
        friendRepository.deleteByPlayerIdAndFriendId(playerId, friendId);
        friendRepository.deleteByPlayerIdAndFriendId(friendId, playerId);

        log.info("Friend relationship removed");
    }

    /**
     * Get accepted friends list
     */
    @Transactional(readOnly = true)
    public SocialDto.FriendListResponse getFriends(Long playerId) {
        log.info("Getting friends for player {}", playerId);

        List<Friend> friends = friendRepository.findByPlayerIdAndStatus(playerId, FriendStatus.ACCEPTED);

        List<SocialDto.FriendResponse> friendResponses = friends.stream()
                .map(friend -> SocialDto.FriendResponse.fromEntity(friend, friend.getFriend()))
                .collect(Collectors.toList());

        return SocialDto.FriendListResponse.builder()
                .friends(friendResponses)
                .totalCount(friendResponses.size())
                .build();
    }

    /**
     * Get pending friend requests received
     */
    @Transactional(readOnly = true)
    public SocialDto.PendingRequestsResponse getPendingRequests(Long playerId) {
        log.info("Getting pending friend requests for player {}", playerId);

        List<Friend> pendingRequests = friendRepository.findByFriendIdAndStatus(playerId, FriendStatus.PENDING);

        List<SocialDto.FriendRequestResponse> requestResponses = pendingRequests.stream()
                .map(request -> SocialDto.FriendRequestResponse.fromEntity(request, request.getPlayer()))
                .collect(Collectors.toList());

        return SocialDto.PendingRequestsResponse.builder()
                .requests(requestResponses)
                .totalCount(requestResponses.size())
                .build();
    }

    /**
     * Get friend count
     */
    @Transactional(readOnly = true)
    public int getFriendCount(Long playerId) {
        return friendRepository.countAcceptedFriends(playerId);
    }

    /**
     * Get pending request count
     */
    @Transactional(readOnly = true)
    public int getPendingRequestCount(Long playerId) {
        return friendRepository.countPendingRequestsReceived(playerId);
    }

    // ==================== Block Operations ====================

    /**
     * Block a player
     */
    @Transactional
    public SocialDto.BlockedPlayerResponse blockPlayer(Long blockerId, Long blockedId) {
        log.info("Player {} blocking player {}", blockerId, blockedId);

        // Validation
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("Cannot block yourself");
        }

        Player blocker = playerRepository.findById(blockerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + blockerId));
        Player blocked = playerRepository.findById(blockedId)
                .orElseThrow(() -> new IllegalArgumentException("Target player not found: " + blockedId));

        // Check if already blocked
        if (blockListRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new IllegalArgumentException("Player is already blocked");
        }

        // Remove any existing friendship
        friendRepository.deleteByPlayerIdAndFriendId(blockerId, blockedId);
        friendRepository.deleteByPlayerIdAndFriendId(blockedId, blockerId);

        // Create block
        BlockList block = BlockList.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        block = blockListRepository.save(block);
        log.info("Player blocked successfully");

        return SocialDto.BlockedPlayerResponse.fromEntity(block, blocked);
    }

    /**
     * Unblock a player
     */
    @Transactional
    public void unblockPlayer(Long blockerId, Long blockedId) {
        log.info("Player {} unblocking player {}", blockerId, blockedId);

        if (!blockListRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new IllegalArgumentException("Player is not blocked");
        }

        blockListRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
        log.info("Player unblocked successfully");
    }

    /**
     * Get blocked players list
     */
    @Transactional(readOnly = true)
    public SocialDto.BlockListResponse getBlockedPlayers(Long playerId) {
        log.info("Getting blocked players for player {}", playerId);

        List<BlockList> blocks = blockListRepository.findByBlockerId(playerId);

        List<SocialDto.BlockedPlayerResponse> blockedResponses = blocks.stream()
                .map(block -> SocialDto.BlockedPlayerResponse.fromEntity(block, block.getBlocked()))
                .collect(Collectors.toList());

        return SocialDto.BlockListResponse.builder()
                .blockedPlayers(blockedResponses)
                .totalCount(blockedResponses.size())
                .build();
    }

    /**
     * Check if blocked (bidirectional)
     */
    @Transactional(readOnly = true)
    public boolean isBlocked(Long playerId1, Long playerId2) {
        return blockListRepository.existsBidirectional(playerId1, playerId2);
    }
}
