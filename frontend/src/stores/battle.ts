import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Battle, BattleLog, HandResult, TurnActor } from '@/types/game'

export const useBattleStore = defineStore('battle', () => {
  const currentBattle = ref<Battle | null>(null)
  const battleLogs = ref<BattleLog[]>([])
  const playerDice = ref<[number, number, number] | null>(null)
  const enemyDice = ref<[number, number, number] | null>(null)
  const isRolling = ref(false)

  const isInBattle = computed(() => currentBattle.value !== null)

  // Hand evaluation logic (Balanced System - PROJECTPLAN.md)
  const evaluateHand = (dice: [number, number, number]): HandResult => {
    const sorted = [...dice].sort((a, b) => a - b) as [number, number, number]
    const [a, b, c] = sorted

    // 1. Ace: [1-1-1] → 45
    if (a === 1 && b === 1 && c === 1) {
      return { rank: 'Ace', rankKR: '에이스', power: 45 }
    }

    // 2. Triple: Same 3 [2-6] → 8 + (N*4)
    if (a === b && b === c && a >= 2) {
      return { rank: 'Triple', rankKR: '트리플', power: 8 + (a * 4) }
    }

    // 3. Straight: [4-5-6] → 38
    if (a === 4 && b === 5 && c === 6) {
      return { rank: 'Straight', rankKR: '스트레이트', power: 38 }
    }

    // 4. Strike: [3-4-5] → 30
    if (a === 3 && b === 4 && c === 5) {
      return { rank: 'Strike', rankKR: '스트라이크', power: 30 }
    }

    // 5. Slash: [2-3-4] → 24
    if (a === 2 && b === 3 && c === 4) {
      return { rank: 'Slash', rankKR: '슬래시', power: 24 }
    }

    // 6. Storm: [1-2-3] → 16
    if (a === 1 && b === 2 && c === 3) {
      return { rank: 'Storm', rankKR: '스톰', power: 16 }
    }

    // 7. Pair: Same 2 → 5 + (N*2)
    if (a === b || b === c) {
      const pairValue = a === b ? a : b
      return { rank: 'Pair', rankKR: '페어', power: 5 + (pairValue * 2) }
    }

    // 8. No Hand: Sum (3-16)
    return { rank: 'NoHand', rankKR: '노 핸드', power: a + b + c }
  }

  const startBattle = (battle: Battle) => {
    currentBattle.value = battle
    battleLogs.value = []
    playerDice.value = null
    enemyDice.value = null
  }

  const setPlayerDice = (dice: [number, number, number]) => {
    playerDice.value = dice
  }

  const setEnemyDice = (dice: [number, number, number]) => {
    enemyDice.value = dice
  }

  const addBattleLog = (log: BattleLog) => {
    battleLogs.value.push(log)
  }

  const updateBattle = (updates: Partial<Battle>) => {
    if (currentBattle.value) {
      currentBattle.value = { ...currentBattle.value, ...updates }
    }
  }

  const endBattle = () => {
    currentBattle.value = null
    battleLogs.value = []
    playerDice.value = null
    enemyDice.value = null
  }

  return {
    currentBattle,
    battleLogs,
    playerDice,
    enemyDice,
    isRolling,
    isInBattle,
    evaluateHand,
    startBattle,
    setPlayerDice,
    setEnemyDice,
    addBattleLog,
    updateBattle,
    endBattle
  }
})
