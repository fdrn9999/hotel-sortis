import type {
  JoinQueueResponse,
  MatchFoundResponse,
  RankInfoResponse,
  DraftState,
  PlayerPublicProfile,
  PlayerStats,
  MatchHistoryEntry
} from '@/types/game'
import { apiClient } from './client'

/**
 * PvP API client
 */

/**
 * Join matchmaking queue
 */
export async function joinMatchmakingQueue(playerId: number): Promise<JoinQueueResponse> {
  const response = await apiClient.post<JoinQueueResponse>('/api/v1/pvp/matchmaking/join', {
    playerId
  })
  return response.data
}

/**
 * Find match (polling)
 */
export async function findMatch(playerId: number): Promise<MatchFoundResponse | null> {
  try {
    const response = await apiClient.get<MatchFoundResponse>(
      `/api/v1/pvp/matchmaking/find/${playerId}`
    )
    return response.data
  } catch (error: any) {
    // 204 No Content: no opponent found yet
    if (error.response?.status === 204) {
      return null
    }
    throw error
  }
}

/**
 * Leave matchmaking queue
 */
export async function leaveMatchmakingQueue(playerId: number): Promise<void> {
  await apiClient.post('/api/v1/pvp/matchmaking/leave', {
    playerId
  })
}

/**
 * Get rank info
 */
export async function getRankInfo(playerId: number): Promise<RankInfoResponse> {
  const response = await apiClient.get<RankInfoResponse>(`/api/v1/pvp/rank/${playerId}`)
  return response.data
}

/**
 * Calculate tier based on ELO (client-side)
 */
export function calculateTier(elo: number): string {
  if (elo >= 2200) return 'MASTER'
  if (elo >= 1900) return 'DIAMOND'
  if (elo >= 1600) return 'PLATINUM'
  if (elo >= 1300) return 'GOLD'
  if (elo >= 1000) return 'SILVER'
  return 'BRONZE'
}

/**
 * Tier color
 */
export function getTierColor(tier: string): string {
  const colors: Record<string, string> = {
    BRONZE: '#CD7F32',
    SILVER: '#C0C0C0',
    GOLD: '#FFD700',
    PLATINUM: '#E5E4E2',
    DIAMOND: '#B9F2FF',
    MASTER: '#FF10F0'
  }
  return colors[tier] || '#808080'
}

/**
 * Tier name (i18n key)
 */
export function getTierI18nKey(tier: string): string {
  return `pvp.tiers.${tier.toLowerCase()}`
}

/**
 * Get draft state (REST fallback for initial load)
 */
export async function getDraftState(battleId: number): Promise<DraftState | null> {
  try {
    const response = await apiClient.get<DraftState>(`/api/v1/pvp/draft/${battleId}`)
    return response.data
  } catch (error: any) {
    if (error.response?.status === 404) {
      return null
    }
    throw error
  }
}

/**
 * Get player public profile (for viewing other players)
 */
export async function getPlayerProfile(playerId: number): Promise<PlayerPublicProfile> {
  const response = await apiClient.get<PlayerPublicProfile>(`/api/v1/users/${playerId}/profile`)
  return response.data
}

/**
 * Get my stats
 */
export async function getMyStats(): Promise<PlayerStats> {
  const response = await apiClient.get<PlayerStats>('/api/v1/users/me/stats')
  return response.data
}

/**
 * Get my match history
 */
export async function getMatchHistory(limit: number = 10): Promise<MatchHistoryEntry[]> {
  const response = await apiClient.get<MatchHistoryEntry[]>(
    `/api/v1/users/me/match-history?limit=${limit}`
  )
  return response.data
}

/**
 * Get tier thresholds for progress calculation
 */
export function getTierThresholds(): Record<string, { min: number; max: number }> {
  return {
    BRONZE: { min: 0, max: 999 },
    SILVER: { min: 1000, max: 1299 },
    GOLD: { min: 1300, max: 1599 },
    PLATINUM: { min: 1600, max: 1899 },
    DIAMOND: { min: 1900, max: 2199 },
    MASTER: { min: 2200, max: 9999 }
  }
}

/**
 * Calculate progress to next tier (0-100%)
 */
export function calculateTierProgress(elo: number): number {
  const tier = calculateTier(elo)
  const thresholds = getTierThresholds()
  const { min, max } = thresholds[tier]

  if (tier === 'MASTER') return 100

  const progress = ((elo - min) / (max - min + 1)) * 100
  return Math.min(100, Math.max(0, progress))
}
