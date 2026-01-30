// Hand Rankings
export type HandRank = 'Ace' | 'Triple' | 'Straight' | 'Storm' | 'Pair' | 'NoHand'

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
  name: string
  nameKR: string
  nameJP: string
  nameCN: string
  rarity: SkillRarity
  description: string
  descriptionKR: string
  descriptionJP: string
  descriptionCN: string
  trigger: SkillTrigger
  iconUrl?: string
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
