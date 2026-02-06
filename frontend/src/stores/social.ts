/**
 * Social Store (Pinia)
 * Friends, Blocks, Chat management
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  FriendInfo,
  FriendRequest,
  BlockedPlayer,
  ChatMessage
} from '@/types/game'
import {
  getFriends,
  getPendingRequests,
  sendFriendRequest as apiSendFriendRequest,
  acceptFriendRequest as apiAcceptFriendRequest,
  declineFriendRequest as apiDeclineFriendRequest,
  removeFriend as apiRemoveFriend,
  getBlockedPlayers,
  blockPlayer as apiBlockPlayer,
  unblockPlayer as apiUnblockPlayer,
  getGlobalMessages,
  getWhisperConversation,
  sendChatMessage as apiSendChatMessage,
  getUnreadCount,
  markConversationAsRead as apiMarkConversationAsRead
} from '@/api/social'

export const useSocialStore = defineStore('social', () => {
  // ==================== State ====================

  // Friends
  const friends = ref<FriendInfo[]>([])
  const pendingRequests = ref<FriendRequest[]>([])

  // Blocks
  const blockedPlayers = ref<BlockedPlayer[]>([])

  // Chat
  const globalMessages = ref<ChatMessage[]>([])
  const whisperConversations = ref<Map<number, ChatMessage[]>>(new Map())
  const unreadCount = ref(0)

  // UI State
  const loading = ref(false)
  const error = ref<string | null>(null)

  // ==================== Getters ====================

  const friendCount = computed(() => friends.value.length)
  const pendingRequestCount = computed(() => pendingRequests.value.length)
  const blockedCount = computed(() => blockedPlayers.value.length)

  const isFriend = computed(() => (playerId: number) => {
    return friends.value.some(f => f.playerId === playerId)
  })

  const isBlocked = computed(() => (playerId: number) => {
    return blockedPlayers.value.some(b => b.playerId === playerId)
  })

  const getWhisperHistory = computed(() => (partnerId: number) => {
    return whisperConversations.value.get(partnerId) || []
  })

  // ==================== Friend Actions ====================

  /**
   * Load friends list
   */
  async function loadFriends(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const response = await getFriends(playerId)
      friends.value = response.friends
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load friends'
    } finally {
      loading.value = false
    }
  }

  /**
   * Load pending friend requests
   */
  async function loadPendingRequests(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const response = await getPendingRequests(playerId)
      pendingRequests.value = response.requests
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load pending requests'
    } finally {
      loading.value = false
    }
  }

  /**
   * Send a friend request
   */
  async function sendFriendRequest(playerId: number, targetPlayerId: number) {
    loading.value = true
    error.value = null

    try {
      await apiSendFriendRequest(playerId, targetPlayerId)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to send friend request'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Accept a friend request
   */
  async function acceptFriendRequest(playerId: number, requestId: number) {
    loading.value = true
    error.value = null

    try {
      const friend = await apiAcceptFriendRequest(playerId, requestId)
      // Remove from pending
      pendingRequests.value = pendingRequests.value.filter(r => r.requestId !== requestId)
      // Add to friends
      friends.value.push(friend)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to accept friend request'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Decline a friend request
   */
  async function declineFriendRequest(playerId: number, requestId: number) {
    loading.value = true
    error.value = null

    try {
      await apiDeclineFriendRequest(playerId, requestId)
      pendingRequests.value = pendingRequests.value.filter(r => r.requestId !== requestId)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to decline friend request'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Remove a friend
   */
  async function removeFriend(playerId: number, friendId: number) {
    loading.value = true
    error.value = null

    try {
      await apiRemoveFriend(playerId, friendId)
      friends.value = friends.value.filter(f => f.playerId !== friendId)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to remove friend'
      return false
    } finally {
      loading.value = false
    }
  }

  // ==================== Block Actions ====================

  /**
   * Load blocked players
   */
  async function loadBlockedPlayers(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const response = await getBlockedPlayers(playerId)
      blockedPlayers.value = response.blockedPlayers
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load blocked players'
    } finally {
      loading.value = false
    }
  }

  /**
   * Block a player
   */
  async function blockPlayer(playerId: number, targetPlayerId: number) {
    loading.value = true
    error.value = null

    try {
      const blocked = await apiBlockPlayer(playerId, targetPlayerId)
      blockedPlayers.value.push(blocked)
      // Remove from friends if was a friend
      friends.value = friends.value.filter(f => f.playerId !== targetPlayerId)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to block player'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Unblock a player
   */
  async function unblockPlayer(playerId: number, blockedId: number) {
    loading.value = true
    error.value = null

    try {
      await apiUnblockPlayer(playerId, blockedId)
      blockedPlayers.value = blockedPlayers.value.filter(b => b.playerId !== blockedId)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to unblock player'
      return false
    } finally {
      loading.value = false
    }
  }

  // ==================== Chat Actions ====================

  /**
   * Load global chat messages
   */
  async function loadGlobalMessages(page = 0, size = 50) {
    loading.value = true
    error.value = null

    try {
      const response = await getGlobalMessages(page, size)
      if (page === 0) {
        globalMessages.value = response.messages
      } else {
        globalMessages.value = [...globalMessages.value, ...response.messages]
      }
      return response.hasMore
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load global messages'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Load whisper conversation
   */
  async function loadWhisperConversation(playerId: number, partnerId: number, page = 0, size = 50) {
    loading.value = true
    error.value = null

    try {
      const response = await getWhisperConversation(playerId, partnerId, page, size)
      if (page === 0) {
        whisperConversations.value.set(partnerId, response.messages)
      } else {
        const existing = whisperConversations.value.get(partnerId) || []
        whisperConversations.value.set(partnerId, [...existing, ...response.messages])
      }
      return response.hasMore
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load conversation'
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Send a global message
   */
  async function sendGlobalMessage(playerId: number, content: string) {
    try {
      const message = await apiSendChatMessage(playerId, 'GLOBAL', content)
      globalMessages.value.unshift(message)
      return message
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to send message'
      return null
    }
  }

  /**
   * Send a whisper
   */
  async function sendWhisper(playerId: number, receiverId: number, content: string) {
    try {
      const message = await apiSendChatMessage(playerId, 'WHISPER', content, receiverId)
      // Add to conversation
      const conversation = whisperConversations.value.get(receiverId) || []
      conversation.unshift(message)
      whisperConversations.value.set(receiverId, conversation)
      return message
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to send whisper'
      return null
    }
  }

  /**
   * Load unread count
   */
  async function loadUnreadCount(playerId: number) {
    try {
      const response = await getUnreadCount(playerId)
      unreadCount.value = response.unreadCount
    } catch (err) {
      // Silent fail
    }
  }

  /**
   * Mark conversation as read
   */
  async function markConversationAsRead(playerId: number, senderId: number) {
    try {
      await apiMarkConversationAsRead(playerId, senderId)
      // Update local unread count
      await loadUnreadCount(playerId)
    } catch (err) {
      // Silent fail
    }
  }

  /**
   * Add incoming message (from WebSocket)
   */
  function addIncomingMessage(message: ChatMessage) {
    if (message.messageType === 'GLOBAL') {
      globalMessages.value.unshift(message)
    } else if (message.senderId) {
      const partnerId = message.senderId
      const conversation = whisperConversations.value.get(partnerId) || []
      conversation.unshift(message)
      whisperConversations.value.set(partnerId, conversation)
      // Increment unread count
      unreadCount.value++
    }
  }

  /**
   * Add incoming friend request (from WebSocket)
   */
  function addIncomingFriendRequest(request: FriendRequest) {
    pendingRequests.value.unshift(request)
  }

  /**
   * Reset state
   */
  function $reset() {
    friends.value = []
    pendingRequests.value = []
    blockedPlayers.value = []
    globalMessages.value = []
    whisperConversations.value = new Map()
    unreadCount.value = 0
    loading.value = false
    error.value = null
  }

  return {
    // State
    friends,
    pendingRequests,
    blockedPlayers,
    globalMessages,
    whisperConversations,
    unreadCount,
    loading,
    error,

    // Getters
    friendCount,
    pendingRequestCount,
    blockedCount,
    isFriend,
    isBlocked,
    getWhisperHistory,

    // Friend Actions
    loadFriends,
    loadPendingRequests,
    sendFriendRequest,
    acceptFriendRequest,
    declineFriendRequest,
    removeFriend,

    // Block Actions
    loadBlockedPlayers,
    blockPlayer,
    unblockPlayer,

    // Chat Actions
    loadGlobalMessages,
    loadWhisperConversation,
    sendGlobalMessage,
    sendWhisper,
    loadUnreadCount,
    markConversationAsRead,
    addIncomingMessage,
    addIncomingFriendRequest,

    // Reset
    $reset
  }
})
