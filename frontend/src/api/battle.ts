import apiClient from './client'

// Request/Response types matching backend DTOs
export interface StartBattleRequest {
  playerId: number
  battleType: 'PVE' | 'PVP'
  floor?: number
  equippedSkills?: number[]
  bossId?: string
  bossPhase?: number
  enemySkills?: number[]
  mutatorId?: string
}

export interface StartBattleResponse {
  battleId: number
  playerId: number
  enemyId?: number
  currentTurn: 'PLAYER' | 'ENEMY'
  playerHp: number
  enemyHp: number
  playerShield: number
  enemyShield: number
  turnCount: number
  status: 'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW'
  bossId?: string
  bossPhase?: number
  bossTotalPhases?: number
  bossName?: string
  mutatorId?: string
  fogActive?: boolean
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

export interface BossPhaseTransitionResult {
  bossId: string
  newPhase: number
  totalPhases: number
  quote?: string
  newPattern?: string
}

export interface RollDiceResponse {
  dice: [number, number, number]
  hash: string
  hand: HandResult
  damage: number
  playerHp: number
  enemyHp: number
  playerShield: number
  enemyShield: number
  currentTurn: 'PLAYER' | 'ENEMY'
  status: 'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW'
  enemyTurn?: EnemyTurnResult
  bossPhaseTransition?: BossPhaseTransitionResult
  fogActive?: boolean
  skillsSilenced?: boolean
}

export interface BattleStatus {
  battleId: number
  playerHp: number
  enemyHp: number
  playerShield: number
  enemyShield: number
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
