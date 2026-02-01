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
 * PvP WebSocket 전용 composable
 *
 * - 매치 찾기 알림
 * - 턴 시작 알림
 * - 주사위 결과 브로드캐스트
 * - 전투 종료 알림
 */
export function usePvPWebSocket(playerId: number) {
  const { connected, connect, disconnect, subscribe, send } = useWebSocket()

  // 구독 객체 저장 (언마운트 시 정리용)
  const subscriptions = ref<any[]>([])

  // 이벤트 핸들러
  const onMatchFound = ref<((match: MatchFoundResponse) => void) | null>(null)
  const onTurnStart = ref<((turn: TurnStartMessage) => void) | null>(null)
  const onDiceResult = ref<((result: DiceResultMessage) => void) | null>(null)
  const onBattleEnd = ref<((end: BattleEndMessage) => void) | null>(null)

  /**
   * PvP 구독 시작
   */
  const subscribePvP = () => {
    if (!connected.value) {
      console.warn('WebSocket not connected. Connecting...')
      connect()
      // 연결 후 1초 대기 후 재시도
      setTimeout(subscribePvP, 1000)
      return
    }

    // 1. 매치 찾기 알림 구독
    const matchSub = subscribe(`/user/queue/match-found`, (message: IMessage) => {
      const match: MatchFoundResponse = JSON.parse(message.body)
      console.log('Match found:', match)
      if (onMatchFound.value) {
        onMatchFound.value(match)
      }
    })
    if (matchSub) subscriptions.value.push(matchSub)

    // 2. 턴 시작 알림 구독
    const turnSub = subscribe(`/user/queue/pvp/turn-start`, (message: IMessage) => {
      const turn: TurnStartMessage = JSON.parse(message.body)
      console.log('Turn start:', turn)
      if (onTurnStart.value) {
        onTurnStart.value(turn)
      }
    })
    if (turnSub) subscriptions.value.push(turnSub)

    // 3. 주사위 결과 구독
    const diceSub = subscribe(`/user/queue/pvp/dice-result`, (message: IMessage) => {
      const result: DiceResultMessage = JSON.parse(message.body)
      console.log('Dice result:', result)
      if (onDiceResult.value) {
        onDiceResult.value(result)
      }
    })
    if (diceSub) subscriptions.value.push(diceSub)

    // 4. 전투 종료 구독
    const endSub = subscribe(`/user/queue/pvp/battle-end`, (message: IMessage) => {
      const end: BattleEndMessage = JSON.parse(message.body)
      console.log('Battle end:', end)
      if (onBattleEnd.value) {
        onBattleEnd.value(end)
      }
    })
    if (endSub) subscriptions.value.push(endSub)

    console.log(`PvP WebSocket subscribed for player ${playerId}`)
  }

  /**
   * 주사위 굴림 전송
   */
  const sendRollDice = (battleId: number) => {
    send(`/app/pvp/battles/${battleId}/roll`, {
      playerId
    })
  }

  /**
   * 구독 정리
   */
  const unsubscribeAll = () => {
    subscriptions.value.forEach((sub) => {
      if (sub && sub.unsubscribe) {
        sub.unsubscribe()
      }
    })
    subscriptions.value = []
    console.log('PvP WebSocket unsubscribed')
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
