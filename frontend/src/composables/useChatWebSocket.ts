import { ref, onUnmounted } from 'vue'
import type { IMessage } from '@stomp/stompjs'
import { useWebSocket } from './useWebSocket'
import type { ChatMessage, FriendRequest, FriendInfo } from '@/types/game'
import { useSocialStore } from '@/stores/social'

/**
 * Chat WebSocket composable
 *
 * - Global chat messages
 * - Whisper (DM) messages
 * - Friend request notifications
 * - Read receipts
 */
export function useChatWebSocket(playerId: number) {
  const { connected, connect, disconnect, subscribe, send } = useWebSocket()
  const socialStore = useSocialStore()

  // Store subscription objects (for cleanup on unmount)
  const subscriptions = ref<any[]>([])

  // Event handlers
  const onGlobalMessage = ref<((message: ChatMessage) => void) | null>(null)
  const onWhisper = ref<((message: ChatMessage) => void) | null>(null)
  const onFriendRequest = ref<((request: FriendRequest) => void) | null>(null)
  const onFriendAccepted = ref<((friend: FriendInfo) => void) | null>(null)
  const onReadReceipt = ref<((readerId: number, count: number) => void) | null>(null)
  const onError = ref<((error: string) => void) | null>(null)

  /**
   * Start chat subscriptions
   */
  const subscribeChat = () => {
    if (!connected.value) {
      connect()
      // Wait 1 second after connection then retry
      setTimeout(subscribeChat, 1000)
      return
    }

    // 1. Subscribe to global chat
    const globalSub = subscribe('/topic/chat/global', (message: IMessage) => {
      const chatMessage: ChatMessage = JSON.parse(message.body)
      socialStore.addIncomingMessage(chatMessage)
      if (onGlobalMessage.value) {
        onGlobalMessage.value(chatMessage)
      }
    })
    if (globalSub) subscriptions.value.push(globalSub)

    // 2. Subscribe to whispers
    const whisperSub = subscribe('/user/queue/whisper', (message: IMessage) => {
      const chatMessage: ChatMessage = JSON.parse(message.body)
      // Only add to store if it's not from self
      if (chatMessage.senderId !== playerId) {
        socialStore.addIncomingMessage(chatMessage)
      }
      if (onWhisper.value) {
        onWhisper.value(chatMessage)
      }
    })
    if (whisperSub) subscriptions.value.push(whisperSub)

    // 3. Subscribe to friend requests
    const friendRequestSub = subscribe('/user/queue/friend-request', (message: IMessage) => {
      const request: FriendRequest = JSON.parse(message.body)
      socialStore.addIncomingFriendRequest(request)
      if (onFriendRequest.value) {
        onFriendRequest.value(request)
      }
    })
    if (friendRequestSub) subscriptions.value.push(friendRequestSub)

    // 4. Subscribe to friend accepted notifications
    const friendAcceptedSub = subscribe('/user/queue/friend-accepted', (message: IMessage) => {
      const friend: FriendInfo = JSON.parse(message.body)
      if (onFriendAccepted.value) {
        onFriendAccepted.value(friend)
      }
    })
    if (friendAcceptedSub) subscriptions.value.push(friendAcceptedSub)

    // 5. Subscribe to read receipts
    const readReceiptSub = subscribe('/user/queue/read-receipt', (message: IMessage) => {
      const receipt = JSON.parse(message.body)
      if (onReadReceipt.value) {
        onReadReceipt.value(receipt.readerId, receipt.count)
      }
    })
    if (readReceiptSub) subscriptions.value.push(readReceiptSub)

    // 6. Subscribe to errors
    const errorSub = subscribe('/user/queue/errors', (message: IMessage) => {
      const error = JSON.parse(message.body)
      if (onError.value) {
        onError.value(error.message)
      }
    })
    if (errorSub) subscriptions.value.push(errorSub)
  }

  /**
   * Send global message via WebSocket
   */
  const sendGlobalMessage = (content: string) => {
    if (!connected.value) return false

    send('/app/chat/global', {
      senderId: playerId,
      content
    })
    return true
  }

  /**
   * Send whisper via WebSocket
   */
  const sendWhisper = (receiverId: number, content: string) => {
    if (!connected.value) return false

    send(`/app/chat/whisper/${receiverId}`, {
      senderId: playerId,
      content
    })
    return true
  }

  /**
   * Mark messages as read via WebSocket
   */
  const markAsRead = (senderId: number) => {
    if (!connected.value) return false

    send(`/app/chat/read/${senderId}`, {
      playerId
    })
    return true
  }

  /**
   * Unsubscribe all and cleanup
   */
  const unsubscribeAll = () => {
    subscriptions.value.forEach(sub => {
      if (sub && sub.unsubscribe) {
        sub.unsubscribe()
      }
    })
    subscriptions.value = []
  }

  // Cleanup on unmount
  onUnmounted(() => {
    unsubscribeAll()
  })

  return {
    // Connection state
    connected,
    connect,
    disconnect,

    // Subscription
    subscribeChat,
    unsubscribeAll,

    // Send actions
    sendGlobalMessage,
    sendWhisper,
    markAsRead,

    // Event handlers (set these to receive events)
    onGlobalMessage,
    onWhisper,
    onFriendRequest,
    onFriendAccepted,
    onReadReceipt,
    onError
  }
}
