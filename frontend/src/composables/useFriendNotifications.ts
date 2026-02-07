/**
 * Friend Notifications Composable
 * Handles WebSocket-based friend request notifications
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useSocialStore } from '@/stores/social'
import type { FriendRequest } from '@/types/game'

// Custom event type for friend requests
declare global {
  interface WindowEventMap {
    'friend-request-received': CustomEvent<FriendRequest>
  }
}

export function useFriendNotifications(playerId: number) {
  const socialStore = useSocialStore()
  const connected = ref(false)
  let stompClient: unknown = null
  let subscription: unknown = null

  /**
   * Connect to WebSocket and subscribe to friend request topic
   */
  const connect = () => {
    // Check if SockJS and Stomp are available
    if (typeof window === 'undefined') return

    try {
      // @ts-ignore - SockJS loaded globally
      const SockJS = window.SockJS
      // @ts-expect-error - Stomp loaded globally
      const Stomp = window.Stomp

      if (!SockJS || !Stomp) {
        return
      }

      const socket = new SockJS('/ws')
      stompClient = Stomp.over(socket)

      // Disable debug logging in production
      // @ts-expect-error - stompClient type
      stompClient.debug = () => {}

      // @ts-expect-error - stompClient type
      stompClient.connect({}, () => {
        connected.value = true
        subscribeToFriendRequests()
      })
    } catch {
      // WebSocket not available
    }
  }

  /**
   * Subscribe to friend request notifications
   */
  const subscribeToFriendRequests = () => {
    if (!stompClient || !connected.value) return

    try {
      // @ts-expect-error - stompClient type
      subscription = stompClient.subscribe(
        `/user/${playerId}/queue/friend-requests`,
        (message: { body: string }) => {
          const request: FriendRequest = JSON.parse(message.body)
          handleIncomingRequest(request)
        }
      )
    } catch {
      // Subscription failed
    }
  }

  /**
   * Handle incoming friend request
   */
  const handleIncomingRequest = (request: FriendRequest) => {
    // Add to store
    socialStore.addIncomingFriendRequest(request)

    // Dispatch custom event for notification component
    const event = new CustomEvent('friend-request-received', {
      detail: request
    })
    window.dispatchEvent(event)
  }

  /**
   * Disconnect from WebSocket
   */
  const disconnect = () => {
    try {
      if (subscription) {
        // @ts-expect-error - subscription type
        subscription.unsubscribe()
      }
      if (stompClient && connected.value) {
        // @ts-expect-error - stompClient type
        stompClient.disconnect()
      }
    } catch {
      // Disconnect failed
    } finally {
      connected.value = false
      subscription = null
      stompClient = null
    }
  }

  // Auto-connect on mount
  onMounted(() => {
    connect()
  })

  // Cleanup on unmount
  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    connect,
    disconnect
  }
}
