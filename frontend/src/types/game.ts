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
  playerShield: number
  enemyShield: number
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
  // Mutator fields
  mutatorId?: string
  mutatorName?: string
  mutatorDescription?: string
  mutatorIcon?: string
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
  // Mutator fields
  mutatorId?: string
  mutatorName?: string
  mutatorDescription?: string
  mutatorIcon?: string
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
export type RankTier = 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM' | 'DIAMOND' | 'MASTER'

export interface PvPMatch {
  matchId: number
  player1Id: number
  player2Id: number
  player1Elo: number
  player2Elo: number
  status: 'MATCHING' | 'IN_PROGRESS' | 'FINISHED'
  winnerId?: number
}

// PvP API 응답 타입들
export interface JoinQueueResponse {
  playerId: number
  elo: number
  queueSize: number
  status: 'WAITING' | 'MATCH_FOUND'
}

export interface MatchFoundResponse {
  battleId: number
  player1Id: number
  player2Id: number
  player1Elo: number
  player2Elo: number
  status: 'MATCH_FOUND'
  hasDraft?: boolean
}

export interface RankInfoResponse {
  playerId: number
  elo: number
  tier: RankTier
  wins: number
  losses: number
  draws: number
  winRate: number
}

export interface PvPRewardInfo {
  eloChange: number     // +25, -25, 0
  soulStones: number    // 20, 5, 10
  result: 'VICTORY' | 'DEFEAT' | 'DRAW'
}

// WebSocket 메시지 타입들
export interface TurnStartMessage {
  battleId: number
  currentTurn: TurnActor
  turnNumber: number
  timeRemaining: number  // 밀리초
}

export interface DiceResultMessage {
  battleId: number
  playerId: number
  dice: number[]
  handRank: string
  handRankKR: string
  handPower: number
  damage: number
  opponentHp: number
}

export interface BattleEndMessage {
  battleId: number
  result: 'VICTORY' | 'DEFEAT' | 'DRAW'
  reward: PvPRewardInfo
}

// Cosmetic System (Phase 9)
export type CosmeticType = 'DICE_SKIN' | 'AVATAR'
export type CosmeticRarity = SkillRarity // Common, Rare, Epic, Legendary

export interface DiceSkin {
  id: number
  skinCode: string
  name: string          // 사용자 언어의 이름
  description: string   // 사용자 언어의 설명
  rarity: CosmeticRarity
  price: number
  material: string      // 'standard', 'metal', 'glass', 'cosmic'
  baseColor: string     // HEX color
  pipColor: string      // HEX color
  textureUrl?: string
  metalness: number     // 0.0 - 1.0
  roughness: number     // 0.0 - 1.0
  emissiveColor?: string
  emissiveIntensity?: number
  isDefault: boolean
  isAvailable: boolean
  isOwned: boolean      // 플레이어 소유 여부
  isEquipped: boolean   // 장착 여부
  previewUrl?: string
}

export interface Avatar {
  id: number
  avatarCode: string
  name: string          // 사용자 언어의 이름
  description: string   // 사용자 언어의 설명
  rarity: CosmeticRarity
  price: number
  avatarUrl: string
  isDefault: boolean
  isAvailable: boolean
  isOwned: boolean      // 플레이어 소유 여부
  isEquipped: boolean   // 장착 여부
  previewUrl?: string
}

export interface CollectionResponse {
  diceSkins: DiceSkin[]
  avatars: Avatar[]
  equippedDiceSkinId?: number
  equippedAvatarId?: number
  language: string
}

export interface ShopItem {
  cosmeticType: CosmeticType
  id: number
  code: string
  name: string
  description: string
  rarity: CosmeticRarity
  price: number
  previewUrl?: string
  isOwned: boolean
  isPurchasable: boolean  // 미소유 + 영혼석 충분
}

export interface ShopResponse {
  diceSkins: ShopItem[]
  avatars: ShopItem[]
  playerSoulStones: number
  language: string
}

export interface PurchaseRequest {
  playerId: number
  cosmeticType: CosmeticType
  cosmeticId: number
}

export interface PurchaseResponse {
  success: boolean
  message: string
  cosmeticType?: CosmeticType
  cosmeticId?: number
  pricePaid?: number
  remainingSoulStones?: number
}

export interface PlayerWallet {
  playerId: number
  soulStones: number
  elo: number
}

export interface EquipCosmeticRequest {
  playerId: number
  cosmeticType: CosmeticType
  cosmeticId: number
}

export interface EquipCosmeticResponse {
  message: string
  cosmeticType: CosmeticType
  cosmeticId?: number
  equippedDiceSkinId?: number
  equippedAvatarId?: number
}

// Draft Mode Types
export type DraftStatus = 'IN_PROGRESS' | 'PICKS_COMPLETE' | 'COMPLETED' | 'CANCELLED'

export interface DraftSkillInfo {
  skillId: number
  skillCode: string
  name: string
  description: string
  rarity: string
  triggerType: string
}

export interface DraftState {
  battleId: number
  player1Id: number
  player2Id: number
  currentTurn: 'player1' | 'player2'
  pickNumber: number
  timeRemaining: number
  pool: DraftSkillInfo[]
  player1Picks: DraftSkillInfo[]
  player2Picks: DraftSkillInfo[]
  player1Ready: boolean
  player2Ready: boolean
  status: DraftStatus
}

export interface DraftPickRequest {
  playerId: number
  skillId: number
}

export interface DraftPickResponse {
  battleId: number
  playerId: number
  skillId: number
  skillName: string
  pickNumber: number
  nextTurn: string | null
  success: boolean
  error?: string
}

export interface DraftCompleteMessage {
  battleId: number
  player1Skills: number[]
  player2Skills: number[]
  status: 'READY_TO_START'
}

export interface DraftTimerUpdate {
  battleId: number
  timeRemaining: number
  currentTurn: string
}
