import apiClient from './client'
import type { HandRank } from '@/types/game'

// Request/Response types matching backend DTOs
export interface StartBattleRequest {
  playerId: number
  battleType: 'PVE' | 'PVP'
  floor?: number
  equippedSkills?: number[]
}

export interface StartBattleResponse {
  battleId: number
  playerId: number
  enemyId?: number
  currentTurn: 'PLAYER' | 'ENEMY'
  playerHp: number
  enemyHp: number
  turnCount: number
  status: 'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW'
}

export interface RollDiceRequest {
  playerId: number
}

export interface HandResult {
  rank: string
  rankKR: string
  power: number
}

export interface EnemyTurnResult {
  dice: [number, number, number]
  hand: HandResult
  damage: number
}

export interface RollDiceResponse {
  dice: [number, number, number]
  hash: string
  hand: HandResult
  damage: number
  playerHp: number
  enemyHp: number
  currentTurn: 'PLAYER' | 'ENEMY'
  status: 'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW'
  enemyTurn?: EnemyTurnResult
}

export interface BattleStatus {
  battleId: number
  playerHp: number
  enemyHp: number
  turnCount: number
  currentTurn: 'PLAYER' | 'ENEMY'
  status: 'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW'
}

/**
 * Battle API Service
 * All dice rolling happens SERVER-SIDE for security
 */
export const battleApi = {
  /**
   * Start a new battle
   */
  async startBattle(request: StartBattleRequest): Promise<StartBattleResponse> {
    const response = await apiClient.post<StartBattleResponse>('/battles', request)
    return response.data
  },

  /**
   * Roll dice in current battle
   * Dice are generated on the server - never on client!
   */
  async rollDice(battleId: number, request: RollDiceRequest): Promise<RollDiceResponse> {
    const response = await apiClient.post<RollDiceResponse>(
      `/battles/${battleId}/roll`,
      request
    )
    return response.data
  },

  /**
   * Get current battle status
   */
  async getBattleStatus(battleId: number): Promise<BattleStatus> {
    const response = await apiClient.get<BattleStatus>(`/battles/${battleId}`)
    return response.data
  }
}

export default battleApi
