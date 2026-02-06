import { ref, onUnmounted } from 'vue'
import type { IMessage } from '@stomp/stompjs'
import { useWebSocket } from './useWebSocket'
import type {
  TurnStartMessage,
  DiceResultMessage,
  BattleEndMessage,
  MatchFoundResponse
} from '@/types/game'

/**
 * PvP WebSocket dedicated composable
 *
 * - Match found notification
 * - Turn start notification
 * - Dice result broadcast
 * - Battle end notification
 */
export function usePvPWebSocket(playerId: number) {
  const { connected, connect, disconnect, subscribe, send } = useWebSocket()

  // Store subscription objects (for cleanup on unmount)
  const subscriptions = ref<any[]>([])

  // Event handlers
  const onMatchFound = ref<((match: MatchFoundResponse) => void) | null>(null)
  const onTurnStart = ref<((turn: TurnStartMessage) => void) | null>(null)
  const onDiceResult = ref<((result: DiceResultMessage) => void) | null>(null)
  const onBattleEnd = ref<((end: BattleEndMessage) => void) | null>(null)

  /**
   * Start PvP subscriptions
   */
  const subscribePvP = () => {
    if (!connected.value) {
      connect()
      // Wait 1 second after connection then retry
      setTimeout(subscribePvP, 1000)
      return
    }

    // 1. Subscribe to match found notification
    const matchSub = subscribe(`/user/queue/match-found`, (message: IMessage) => {
      const match: MatchFoundResponse = JSON.parse(message.body)
      if (onMatchFound.value) {
        onMatchFound.value(match)
      }
    })
    if (matchSub) subscriptions.value.push(matchSub)

    // 2. Subscribe to turn start notification
    const turnSub = subscribe(`/user/queue/pvp/turn-start`, (message: IMessage) => {
      const turn: TurnStartMessage = JSON.parse(message.body)
      if (onTurnStart.value) {
        onTurnStart.value(turn)
      }
    })
    if (turnSub) subscriptions.value.push(turnSub)

    // 3. Subscribe to dice result
    const diceSub = subscribe(`/user/queue/pvp/dice-result`, (message: IMessage) => {
      const result: DiceResultMessage = JSON.parse(message.body)
      if (onDiceResult.value) {
        onDiceResult.value(result)
      }
    })
    if (diceSub) subscriptions.value.push(diceSub)

    // 4. Subscribe to battle end
    const endSub = subscribe(`/user/queue/pvp/battle-end`, (message: IMessage) => {
      const end: BattleEndMessage = JSON.parse(message.body)
      if (onBattleEnd.value) {
        onBattleEnd.value(end)
      }
    })
    if (endSub) subscriptions.value.push(endSub)
  }

  /**
   * Send dice roll
   */
  const sendRollDice = (battleId: number) => {
    send(`/app/pvp/battles/${battleId}/roll`, {
      playerId
    })
  }

  /**
   * Cleanup subscriptions
   */
  const unsubscribeAll = () => {
    subscriptions.value.forEach((sub) => {
      if (sub && sub.unsubscribe) {
        sub.unsubscribe()
      }
    })
    subscriptions.value = []
  }

  onUnmounted(() => {
    unsubscribeAll()
  })

  return {
    connected,
    connect,
    disconnect,
    subscribePvP,
    unsubscribeAll,
    sendRollDice,
    onMatchFound,
    onTurnStart,
    onDiceResult,
    onBattleEnd
  }
}
