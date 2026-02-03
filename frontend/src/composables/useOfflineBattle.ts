/**
 * Offline Battle Logic
 *
 * This module contains the offline/testing fallback for battle mechanics.
 *
 * IMPORTANT: This logic MUST be kept in sync with the server-side implementation:
 * - Backend: backend/src/main/java/com/hotelsortis/game/service/HandEvaluator.java
 * - Database: docs/insert.sql (hand power values)
 *
 * When updating hand evaluation or damage values, update BOTH files!
 *
 * System A Power Values (as of Phase 3.6):
 * - Ace [1-1-1]: 45
 * - Triple [X-X-X]: 8 + X*4 (16-32)
 * - Straight [4-5-6]: 38
 * - Strike [3-4-5]: 30
 * - Slash [2-3-4]: 24
 * - Storm [1-2-3]: 16
 * - Pair [X-X-Y]: 5 + X*2 (7-17)
 * - NoHand: sum of dice (3-16)
 */

import type { RollDiceResponse } from '@/api/battle'

export interface HandResult {
  rank: string
  rankKR: string
  power: number
}

export interface OfflineBattleState {
  playerHP: number
  enemyHP: number
  playerShield: number
  enemyShield: number
  turnCount: number
}

/**
 * Evaluate a hand of 3 dice
 *
 * @param dice - Array of 3 dice values (1-6)
 * @returns Hand result with rank name, Korean name, and power
 */
export function evaluateHand(dice: [number, number, number]): HandResult {
  const sorted = [...dice].sort((a, b) => a - b) as [number, number, number]
  const [a, b, c] = sorted

  // Ace: [1-1-1] - Highest rank
  if (a === 1 && b === 1 && c === 1) {
    return { rank: 'Ace', rankKR: '에이스', power: 45 }
  }

  // Triple: [X-X-X] where X >= 2
  if (a === b && b === c && a >= 2) {
    return { rank: 'Triple', rankKR: '트리플', power: 8 + (a * 4) }
  }

  // Straight: [4-5-6]
  if (a === 4 && b === 5 && c === 6) {
    return { rank: 'Straight', rankKR: '스트레이트', power: 38 }
  }

  // Strike: [3-4-5]
  if (a === 3 && b === 4 && c === 5) {
    return { rank: 'Strike', rankKR: '스트라이크', power: 30 }
  }

  // Slash: [2-3-4]
  if (a === 2 && b === 3 && c === 4) {
    return { rank: 'Slash', rankKR: '슬래시', power: 24 }
  }

  // Storm: [1-2-3]
  if (a === 1 && b === 2 && c === 3) {
    return { rank: 'Storm', rankKR: '스톰', power: 16 }
  }

  // Pair: [X-X-Y]
  if (a === b || b === c) {
    const pairValue = a === b ? a : b
    return { rank: 'Pair', rankKR: '페어', power: 5 + (pairValue * 2) }
  }

  // NoHand: no pattern, power = sum
  return { rank: 'NoHand', rankKR: '노 핸드', power: a + b + c }
}

/**
 * Generate random dice roll (3 dice)
 */
export function rollDice(): [number, number, number] {
  return [
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1
  ]
}

/**
 * Simulate a complete battle turn (offline mode)
 *
 * @param state - Current battle state
 * @returns Simulated roll response
 */
export function simulateRoll(state: OfflineBattleState): RollDiceResponse {
  const dice = rollDice()
  const hand = evaluateHand(dice)
  const newEnemyHp = Math.max(0, state.enemyHP - hand.power)

  const enemyDiceRoll = rollDice()
  const enemyHandResult = evaluateHand(enemyDiceRoll)
  const newPlayerHp = Math.max(0, state.playerHP - enemyHandResult.power)

  let newStatus: 'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW' = 'ONGOING'
  if (newEnemyHp <= 0) {
    newStatus = 'VICTORY'
  } else if (newPlayerHp <= 0) {
    newStatus = 'DEFEAT'
  } else if (state.turnCount >= 10) {
    newStatus = 'DRAW'
  }

  return {
    dice,
    hash: 'offline',
    hand,
    damage: hand.power,
    playerHp: newPlayerHp,
    enemyHp: newEnemyHp,
    playerShield: state.playerShield,
    enemyShield: state.enemyShield,
    currentTurn: 'PLAYER',
    status: newStatus,
    enemyTurn: newEnemyHp > 0 ? {
      dice: enemyDiceRoll,
      hand: enemyHandResult,
      damage: enemyHandResult.power
    } : undefined
  }
}

/**
 * Composable for offline battle functionality
 */
export function useOfflineBattle() {
  return {
    evaluateHand,
    rollDice,
    simulateRoll
  }
}
