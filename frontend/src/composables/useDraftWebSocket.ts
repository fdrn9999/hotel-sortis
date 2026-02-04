import { ref, onUnmounted } from 'vue'
import type { IMessage } from '@stomp/stompjs'
import { useWebSocket } from './useWebSocket'
import type {
  DraftState,
  DraftPickResponse,
  DraftCompleteMessage,
  DraftTimerUpdate
} from '@/types/game'

/**
 * Draft WebSocket composable for PvP skill draft mode
 *
 * Handles real-time draft state updates, pick confirmations,
 * and draft completion notifications.
 *
 * WebSocket Channels:
 * - /user/queue/draft/state - Draft state updates
 * - /user/queue/draft/pick - Pick confirmation
 * - /user/queue/draft/picks-complete - All 8 picks done
 * - /user/queue/draft/complete - Both players ready, start battle
 * - /user/queue/draft/timer - Timer updates from server
 * - /user/queue/draft/error - Error messages
 */
export function useDraftWebSocket(battleId: number, playerId: number) {
  const { connected, connect, disconnect, subscribe, send } = useWebSocket()

  // Subscription storage for cleanup
  const subscriptions = ref<any[]>([])

  // Retry logic to prevent infinite loops
  const MAX_RETRIES = 5
  let retryCount = 0

  // Event handlers
  const onStateUpdate = ref<((state: DraftState) => void) | null>(null)
  const onPickResponse = ref<((response: DraftPickResponse) => void) | null>(null)
  const onPicksComplete = ref<((state: DraftState) => void) | null>(null)
  const onDraftComplete = ref<((msg: DraftCompleteMessage) => void) | null>(null)
  const onTimerUpdate = ref<((timer: DraftTimerUpdate) => void) | null>(null)
  const onError = ref<((error: DraftPickResponse) => void) | null>(null)

  /**
   * Subscribe to all draft WebSocket channels
   */
  const subscribeDraft = () => {
    if (!connected.value) {
      if (retryCount >= MAX_RETRIES) {
        console.error('WebSocket connection failed after max retries')
        return
      }
      console.warn('WebSocket not connected. Connecting...')
      connect()
      retryCount++
      // Exponential backoff: 1s, 2s, 4s, 8s, 10s max
      const delay = Math.min(1000 * Math.pow(2, retryCount - 1), 10000)
      setTimeout(subscribeDraft, delay)
      return
    }
    retryCount = 0 // Reset on successful connection

    // 1. Draft state updates
    const stateSub = subscribe('/user/queue/draft/state', (message: IMessage) => {
      const state: DraftState = JSON.parse(message.body)
      console.log('Draft state update:', state)
      if (onStateUpdate.value) {
        onStateUpdate.value(state)
      }
    })
    if (stateSub) subscriptions.value.push(stateSub)

    // 2. Pick confirmation
    const pickSub = subscribe('/user/queue/draft/pick', (message: IMessage) => {
      const response: DraftPickResponse = JSON.parse(message.body)
      console.log('Draft pick response:', response)
      if (onPickResponse.value) {
        onPickResponse.value(response)
      }
    })
    if (pickSub) subscriptions.value.push(pickSub)

    // 3. All picks complete notification
    const picksCompleteSub = subscribe('/user/queue/draft/picks-complete', (message: IMessage) => {
      const state: DraftState = JSON.parse(message.body)
      console.log('All picks complete:', state)
      if (onPicksComplete.value) {
        onPicksComplete.value(state)
      }
    })
    if (picksCompleteSub) subscriptions.value.push(picksCompleteSub)

    // 4. Draft finalized - ready to battle
    const completeSub = subscribe('/user/queue/draft/complete', (message: IMessage) => {
      const complete: DraftCompleteMessage = JSON.parse(message.body)
      console.log('Draft complete:', complete)
      if (onDraftComplete.value) {
        onDraftComplete.value(complete)
      }
    })
    if (completeSub) subscriptions.value.push(completeSub)

    // 5. Timer updates
    const timerSub = subscribe('/user/queue/draft/timer', (message: IMessage) => {
      const timer: DraftTimerUpdate = JSON.parse(message.body)
      if (onTimerUpdate.value) {
        onTimerUpdate.value(timer)
      }
    })
    if (timerSub) subscriptions.value.push(timerSub)

    // 6. Error notifications
    const errorSub = subscribe('/user/queue/draft/error', (message: IMessage) => {
      const error: DraftPickResponse = JSON.parse(message.body)
      console.log('Draft error:', error)
      if (onError.value) {
        onError.value(error)
      }
    })
    if (errorSub) subscriptions.value.push(errorSub)

    console.log(`Draft WebSocket subscribed for battle ${battleId}, player ${playerId}`)
  }

  /**
   * Send skill pick to server
   */
  const sendPick = (skillId: number) => {
    send(`/app/draft/${battleId}/pick`, {
      playerId,
      skillId
    })
  }

  /**
   * Send ready signal after all picks complete
   */
  const sendReady = () => {
    send(`/app/draft/${battleId}/ready`, {
      playerId
    })
  }

  /**
   * Cleanup all subscriptions
   */
  const unsubscribeAll = () => {
    subscriptions.value.forEach((sub) => {
      if (sub && sub.unsubscribe) {
        sub.unsubscribe()
      }
    })
    subscriptions.value = []
    console.log('Draft WebSocket unsubscribed')
  }

  onUnmounted(() => {
    unsubscribeAll()
  })

  return {
    connected,
    connect,
    disconnect,
    subscribeDraft,
    unsubscribeAll,
    sendPick,
    sendReady,
    onStateUpdate,
    onPickResponse,
    onPicksComplete,
    onDraftComplete,
    onTimerUpdate,
    onError
  }
}
