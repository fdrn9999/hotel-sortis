package com.hotelsortis.api.dto;

import com.hotelsortis.api.entity.BlockList;
import com.hotelsortis.api.entity.ChatMessage;
import com.hotelsortis.api.entity.Friend;
import com.hotelsortis.api.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Social system DTOs (Friends, Blocks, Chat)
 */
public class SocialDto {

    // ==================== Friend DTOs ====================

    /**
     * Friend request payload
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FriendRequest {
        private Long targetPlayerId;
    }

    /**
     * Friend response (for friend list display)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FriendResponse {
        private Long id;
        private Long playerId;
        private String username;
        private Integer elo;
        private Long avatarId;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime acceptedAt;

        public static FriendResponse fromEntity(Friend friend, Player friendPlayer) {
            return FriendResponse.builder()
                    .id(friend.getId())
                    .playerId(friendPlayer.getId())
                    .username(friendPlayer.getUsername())
                    .elo(friendPlayer.getElo())
                    .avatarId(friendPlayer.getEquippedAvatarId())
                    .status(friend.getStatus().name())
                    .createdAt(friend.getCreatedAt())
                    .acceptedAt(friend.getAcceptedAt())
                    .build();
        }
    }

    /**
     * Pending friend request response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FriendRequestResponse {
        private Long requestId;
        private Long playerId;
        private String username;
        private Integer elo;
        private Long avatarId;
        private LocalDateTime requestedAt;

        public static FriendRequestResponse fromEntity(Friend friend, Player requesterPlayer) {
            return FriendRequestResponse.builder()
                    .requestId(friend.getId())
                    .playerId(requesterPlayer.getId())
                    .username(requesterPlayer.getUsername())
                    .elo(requesterPlayer.getElo())
                    .avatarId(requesterPlayer.getEquippedAvatarId())
                    .requestedAt(friend.getCreatedAt())
                    .build();
        }
    }

    /**
     * Friend list response wrapper
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FriendListResponse {
        private List<FriendResponse> friends;
        private int totalCount;
    }

    /**
     * Pending requests response wrapper
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingRequestsResponse {
        private List<FriendRequestResponse> requests;
        private int totalCount;
    }

    // ==================== Block DTOs ====================

    /**
     * Block request payload
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockRequest {
        private Long targetPlayerId;
    }

    /**
     * Block list item response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockedPlayerResponse {
        private Long id;
        private Long playerId;
        private String username;
        private Long avatarId;
        private LocalDateTime blockedAt;

        public static BlockedPlayerResponse fromEntity(BlockList block, Player blockedPlayer) {
            return BlockedPlayerResponse.builder()
                    .id(block.getId())
                    .playerId(blockedPlayer.getId())
                    .username(blockedPlayer.getUsername())
                    .avatarId(blockedPlayer.getEquippedAvatarId())
                    .blockedAt(block.getCreatedAt())
                    .build();
        }
    }

    /**
     * Block list response wrapper
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockListResponse {
        private List<BlockedPlayerResponse> blockedPlayers;
        private int totalCount;
    }

    // ==================== Chat DTOs ====================

    /**
     * Send message request payload
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageRequest {
        private Long receiverId;  // null for GLOBAL
        private String messageType;  // GLOBAL or WHISPER
        private String content;
    }

    /**
     * Chat message response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageResponse {
        private Long id;
        private Long senderId;
        private String senderUsername;
        private Long receiverId;
        private String receiverUsername;
        private String messageType;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;

        public static ChatMessageResponse fromEntity(ChatMessage message) {
            return ChatMessageResponse.builder()
                    .id(message.getId())
                    .senderId(message.getSender().getId())
                    .senderUsername(message.getSenderUsername())
                    .receiverId(message.getReceiver() != null ? message.getReceiver().getId() : null)
                    .receiverUsername(message.getReceiverUsername())
                    .messageType(message.getMessageType().name())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .readAt(message.getReadAt())
                    .build();
        }
    }

    /**
     * Chat history response wrapper
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryResponse {
        private List<ChatMessageResponse> messages;
        private int totalCount;
        private boolean hasMore;
    }

    /**
     * Unread count response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreadCountResponse {
        private int unreadCount;
    }

    // ==================== Common Response ====================

    /**
     * Simple success response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuccessResponse {
        private boolean success;
        private String message;
    }
}
