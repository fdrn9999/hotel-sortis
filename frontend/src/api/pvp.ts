import type {
  JoinQueueResponse,
  MatchFoundResponse,
  RankInfoResponse,
  DraftState
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
