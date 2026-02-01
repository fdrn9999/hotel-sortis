// Hand Rankings
export type HandRank = 'Ace' | 'Triple' | 'Straight' | 'Strike' | 'Slash' | 'Storm' | 'Pair' | 'NoHand'

export interface HandResult {
  rank: HandRank
  rankKR: string
  power: number
}

// Skill System
export type SkillRarity = 'Common' | 'Rare' | 'Epic' | 'Legendary'
export type SkillTrigger = 'BATTLE_START' | 'TURN_START' | 'DICE_ROLL' | 'BEFORE_DAMAGE' | 'AFTER_DAMAGE' | 'PASSIVE'

export interface Skill {
  id: number
  skillCode: string
  name: string          // 사용자 언어의 이름 (백엔드 API 응답)
  description: string   // 사용자 언어의 설명 (백엔드 API 응답)
  rarity: SkillRarity
  triggerType: string   // BATTLE_START, DICE_ROLL, etc.
  iconUrl?: string
}

export interface SkillListResponse {
  skills: Skill[]
  total: number
  language: string  // 응답에 사용된 언어 (ko, en, ja, zh)
}

export type EquippedSkills = [Skill | null, Skill | null, Skill | null, Skill | null]

// Battle System
export type BattleType = 'PVE' | 'PVP'
export type BattleStatus = 'ONGOING' | 'VICTORY' | 'DEFEAT' | 'DRAW'
export type TurnActor = 'PLAYER' | 'ENEMY'

export interface Battle {
  id: number
  playerId: number
  enemyId?: number
  battleType: BattleType
  floor?: number
  status: BattleStatus
  playerHP: number
  enemyHP: number
  turnCount: number
  currentTurn: TurnActor
  playerEquippedSkills: number[]
  enemyEquippedSkills?: number[]
}

export interface BattleLog {
  turnNumber: number
  actor: TurnActor
  diceResult: [number, number, number]
  handRank: HandRank
  handRankKR: string
  handPower: number
  damageDealt: number
  skillsActivated?: number[]
}

// AI Levels
export type AILevel = 0 | 1 | 2 | 3

// Boss Configuration
export interface BossPhase {
  hp: number
  aiLevel: AILevel
  skills: string[]
  pattern: string
  restrictions?: string[]
}

export interface Boss {
  id: string
  name: string
  nameKR: string
  floor: number
  phases: BossPhase[]
  totalHP: number
}

// Campaign
export interface FloorConfig {
  floor: number
  type: 'normal' | 'elite' | 'boss'
  battleCount: number
  bossId?: string
  skillRewardRarity?: SkillRarity
}

// Campaign API Response Types
export interface CampaignFloorStatus {
  floor: number
  floorType: string
  battleCount: number
  cleared: boolean
  locked: boolean
  description: string
  aiLevel: number
  bossId?: string
  bossName?: string
  skillRewardRarity?: string
}

export interface CampaignProgressResponse {
  playerId: number
  currentRunFloor: number
  currentRunHp: number
  floors: CampaignFloorStatus[]
  bossesDefeated: Record<string, boolean>
  totalRuns: number
  totalVictories: number
  totalDefeats: number
}

export interface StartFloorResponse {
  battleId: number
  floor: number
  floorType: string
  battleIndex: number
  totalBattles: number
  aiLevel: number
  enemySkillIds: number[]
  bossId?: string
  bossName?: string
  bossPhase?: number
  bossTotalPhases?: number
}

export interface FloorCompleteResponse {
  floor: number
  cleared: boolean
  nextFloor?: number
  skillRewardAvailable: boolean
  offeredSkills?: SkillRewardOption[]
}

export interface SkillRewardOption {
  skillId: number
  skillCode: string
  name: string
  description: string
  rarity: string
  triggerType: string
}

export interface BossPhaseTransition {
  bossId: string
  newPhase: number
  totalPhases: number
  quote?: string
  newPattern?: string
}

// PvP
export interface PvPMatch {
  matchId: number
  player1Id: number
  player2Id: number
  player1Elo: number
  player2Elo: number
  status: 'MATCHING' | 'IN_PROGRESS' | 'FINISHED'
  winnerId?: number
}

export type RankTier = 'Bronze' | 'Silver' | 'Gold' | 'Platinum' | 'Diamond' | 'Master'
