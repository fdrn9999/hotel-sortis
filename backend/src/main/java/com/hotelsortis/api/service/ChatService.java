package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.SocialDto;
import com.hotelsortis.api.entity.ChatMessage;
import com.hotelsortis.api.entity.ChatMessage.MessageType;
import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.repository.BlockListRepository;
import com.hotelsortis.api.repository.ChatMessageRepository;
import com.hotelsortis.api.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chat message service (GLOBAL and WHISPER)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final PlayerRepository playerRepository;
    private final BlockListRepository blockListRepository;

    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int DEFAULT_PAGE_SIZE = 50;

    // ==================== Send Messages ====================

    /**
     * Send a global chat message
     */
    @Transactional
    public SocialDto.ChatMessageResponse sendGlobalMessage(Long senderId, String content) {
        log.info("Sending global message from player {}", senderId);

        Player sender = playerRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + senderId));

        validateContent(content);

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .senderUsername(sender.getUsername())
                .receiver(null)
                .receiverUsername(null)
                .messageType(MessageType.GLOBAL)
                .content(content)
                .build();

        message = chatMessageRepository.save(message);
        log.info("Global message sent: {}", message.getId());

        return SocialDto.ChatMessageResponse.fromEntity(message);
    }

    /**
     * Send a whisper (private message)
     */
    @Transactional
    public SocialDto.ChatMessageResponse sendWhisper(Long senderId, Long receiverId, String content) {
        log.info("Sending whisper from player {} to {}", senderId, receiverId);

        // Validation
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send whisper to yourself");
        }

        Player sender = playerRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderId));
        Player receiver = playerRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found: " + receiverId));

        // Check if blocked
        if (blockListRepository.existsByBlockerIdAndBlockedId(receiverId, senderId)) {
            throw new IllegalArgumentException("You are blocked by this player");
        }

        validateContent(content);

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .senderUsername(sender.getUsername())
                .receiver(receiver)
                .receiverUsername(receiver.getUsername())
                .messageType(MessageType.WHISPER)
                .content(content)
                .build();

        message = chatMessageRepository.save(message);
        log.info("Whisper sent: {}", message.getId());

        return SocialDto.ChatMessageResponse.fromEntity(message);
    }

    // ==================== Get Messages ====================

    /**
     * Get global chat history
     */
    @Transactional(readOnly = true)
    public SocialDto.ChatHistoryResponse getGlobalMessages(int page, int size) {
        log.info("Getting global messages, page: {}, size: {}", page, size);

        int pageSize = Math.min(size, DEFAULT_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<ChatMessage> messages = chatMessageRepository
                .findByMessageTypeOrderByCreatedAtDesc(MessageType.GLOBAL, pageable);

        List<SocialDto.ChatMessageResponse> messageResponses = messages.getContent().stream()
                .map(SocialDto.ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());

        return SocialDto.ChatHistoryResponse.builder()
                .messages(messageResponses)
                .totalCount((int) messages.getTotalElements())
                .hasMore(messages.hasNext())
                .build();
    }

    /**
     * Get whisper conversation between two players
     */
    @Transactional(readOnly = true)
    public SocialDto.ChatHistoryResponse getWhisperConversation(Long playerId1, Long playerId2, int page, int size) {
        log.info("Getting whisper conversation between {} and {}", playerId1, playerId2);

        int pageSize = Math.min(size, DEFAULT_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<ChatMessage> messages = chatMessageRepository
                .findWhisperConversation(playerId1, playerId2, pageable);

        List<SocialDto.ChatMessageResponse> messageResponses = messages.getContent().stream()
                .map(SocialDto.ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());

        return SocialDto.ChatHistoryResponse.builder()
                .messages(messageResponses)
                .totalCount((int) messages.getTotalElements())
                .hasMore(messages.hasNext())
                .build();
    }

    // ==================== Read Status ====================

    /**
     * Mark a message as read
     */
    @Transactional
    public void markMessageAsRead(Long messageId, Long playerId) {
        log.info("Marking message {} as read by player {}", messageId, playerId);

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        // Only the receiver can mark as read
        if (message.getReceiver() == null || !message.getReceiver().getId().equals(playerId)) {
            throw new IllegalArgumentException("You cannot mark this message as read");
        }

        if (message.getReadAt() == null) {
            chatMessageRepository.updateReadAt(messageId, LocalDateTime.now());
        }
    }

    /**
     * Mark all whispers from a sender as read
     */
    @Transactional
    public int markConversationAsRead(Long receiverId, Long senderId) {
        log.info("Marking conversation from {} to {} as read", senderId, receiverId);

        return chatMessageRepository.markConversationAsRead(receiverId, senderId, LocalDateTime.now());
    }

    /**
     * Get unread whisper count
     */
    @Transactional(readOnly = true)
    public int getUnreadCount(Long playerId) {
        return chatMessageRepository.countByReceiverIdAndMessageTypeAndReadAtIsNull(playerId, MessageType.WHISPER);
    }

    /**
     * Get unread whispers
     */
    @Transactional(readOnly = true)
    public List<SocialDto.ChatMessageResponse> getUnreadWhispers(Long playerId) {
        log.info("Getting unread whispers for player {}", playerId);

        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByReceiverIdAndMessageTypeAndReadAtIsNullOrderByCreatedAtDesc(playerId, MessageType.WHISPER);

        return unreadMessages.stream()
                .map(SocialDto.ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== Validation ====================

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message content exceeds maximum length of " + MAX_MESSAGE_LENGTH);
        }
    }
}
