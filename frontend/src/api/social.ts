/**
 * Social API client (Friends, Blocks, Chat)
 */
import { apiClient } from './client'
import type {
  FriendInfo,
  FriendListResponse,
  FriendRequest,
  PendingRequestsResponse,
  BlockedPlayer,
  BlockListResponse,
  ChatMessage,
  ChatHistoryResponse,
  UnreadCountResponse,
  SuccessResponse
} from '@/types/game'

// ==================== Friend API ====================

/**
 * Send a friend request
 */
export async function sendFriendRequest(playerId: number, targetPlayerId: number): Promise<FriendInfo> {
  const response = await apiClient.post<FriendInfo>('/api/v1/social/friends/request',
    { targetPlayerId },
    { params: { playerId } }
  )
  return response.data
}

/**
 * Accept a friend request
 */
export async function acceptFriendRequest(playerId: number, requestId: number): Promise<FriendInfo> {
  const response = await apiClient.post<FriendInfo>(`/api/v1/social/friends/accept/${requestId}`, null, {
    params: { playerId }
  })
  return response.data
}

/**
 * Decline a friend request
 */
export async function declineFriendRequest(playerId: number, requestId: number): Promise<SuccessResponse> {
  const response = await apiClient.delete<SuccessResponse>(`/api/v1/social/friends/request/${requestId}`, {
    params: { playerId }
  })
  return response.data
}

/**
 * Remove a friend
 */
export async function removeFriend(playerId: number, friendId: number): Promise<SuccessResponse> {
  const response = await apiClient.delete<SuccessResponse>(`/api/v1/social/friends/${friendId}`, {
    params: { playerId }
  })
  return response.data
}

/**
 * Get friend list
 */
export async function getFriends(playerId: number): Promise<FriendListResponse> {
  const response = await apiClient.get<FriendListResponse>('/api/v1/social/friends', {
    params: { playerId }
  })
  return response.data
}

/**
 * Get pending friend requests
 */
export async function getPendingRequests(playerId: number): Promise<PendingRequestsResponse> {
  const response = await apiClient.get<PendingRequestsResponse>('/api/v1/social/friends/pending', {
    params: { playerId }
  })
  return response.data
}

// ==================== Block API ====================

/**
 * Block a player
 */
export async function blockPlayer(playerId: number, targetPlayerId: number): Promise<BlockedPlayer> {
  const response = await apiClient.post<BlockedPlayer>('/api/v1/social/blocks',
    { targetPlayerId },
    { params: { playerId } }
  )
  return response.data
}

/**
 * Unblock a player
 */
export async function unblockPlayer(playerId: number, blockedId: number): Promise<SuccessResponse> {
  const response = await apiClient.delete<SuccessResponse>(`/api/v1/social/blocks/${blockedId}`, {
    params: { playerId }
  })
  return response.data
}

/**
 * Get blocked players list
 */
export async function getBlockedPlayers(playerId: number): Promise<BlockListResponse> {
  const response = await apiClient.get<BlockListResponse>('/api/v1/social/blocks', {
    params: { playerId }
  })
  return response.data
}

// ==================== Chat API ====================

/**
 * Send a chat message (global or whisper)
 */
export async function sendChatMessage(
  playerId: number,
  messageType: 'GLOBAL' | 'WHISPER',
  content: string,
  receiverId?: number
): Promise<ChatMessage> {
  const response = await apiClient.post<ChatMessage>('/api/v1/social/chat',
    { messageType, content, receiverId },
    { params: { playerId } }
  )
  return response.data
}

/**
 * Get global chat history
 */
export async function getGlobalMessages(page = 0, size = 50): Promise<ChatHistoryResponse> {
  const response = await apiClient.get<ChatHistoryResponse>('/api/v1/social/chat/global', {
    params: { page, size }
  })
  return response.data
}

/**
 * Get whisper conversation
 */
export async function getWhisperConversation(
  playerId: number,
  partnerId: number,
  page = 0,
  size = 50
): Promise<ChatHistoryResponse> {
  const response = await apiClient.get<ChatHistoryResponse>(`/api/v1/social/chat/whisper/${partnerId}`, {
    params: { playerId, page, size }
  })
  return response.data
}

/**
 * Mark a message as read
 */
export async function markMessageAsRead(playerId: number, messageId: number): Promise<SuccessResponse> {
  const response = await apiClient.patch<SuccessResponse>(`/api/v1/social/chat/${messageId}/read`, null, {
    params: { playerId }
  })
  return response.data
}

/**
 * Mark conversation as read
 */
export async function markConversationAsRead(playerId: number, senderId: number): Promise<SuccessResponse> {
  const response = await apiClient.patch<SuccessResponse>(`/api/v1/social/chat/conversation/${senderId}/read`, null, {
    params: { playerId }
  })
  return response.data
}

/**
 * Get unread message count
 */
export async function getUnreadCount(playerId: number): Promise<UnreadCountResponse> {
  const response = await apiClient.get<UnreadCountResponse>('/api/v1/social/chat/unread/count', {
    params: { playerId }
  })
  return response.data
}

/**
 * Get unread whispers
 */
export async function getUnreadWhispers(playerId: number): Promise<ChatMessage[]> {
  const response = await apiClient.get<ChatMessage[]>('/api/v1/social/chat/unread', {
    params: { playerId }
  })
  return response.data
}
