import type {
  JoinQueueResponse,
  MatchFoundResponse,
  RankInfoResponse
} from '@/types/game'
import { apiClient } from './client'

/**
 * PvP API 클라이언트
 */

/**
 * 매칭 대기열 참가
 */
export async function joinMatchmakingQueue(playerId: number): Promise<JoinQueueResponse> {
  const response = await apiClient.post<JoinQueueResponse>('/api/v1/pvp/matchmaking/join', {
    playerId
  })
  return response.data
}

/**
 * 상대 찾기 (폴링)
 */
export async function findMatch(playerId: number): Promise<MatchFoundResponse | null> {
  try {
    const response = await apiClient.get<MatchFoundResponse>(
      `/api/v1/pvp/matchmaking/find/${playerId}`
    )
    return response.data
  } catch (error: any) {
    // 204 No Content: 아직 상대 없음
    if (error.response?.status === 204) {
      return null
    }
    throw error
  }
}

/**
 * 매칭 대기 취소
 */
export async function leaveMatchmakingQueue(playerId: number): Promise<void> {
  await apiClient.post('/api/v1/pvp/matchmaking/leave', {
    playerId
  })
}

/**
 * 랭크 정보 조회
 */
export async function getRankInfo(playerId: number): Promise<RankInfoResponse> {
  const response = await apiClient.get<RankInfoResponse>(`/api/v1/pvp/rank/${playerId}`)
  return response.data
}

/**
 * ELO 기반 티어 계산 (클라이언트 사이드)
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
 * 티어별 색상
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
 * 티어 이름 (i18n 키)
 */
export function getTierI18nKey(tier: string): string {
  return `pvp.tiers.${tier.toLowerCase()}`
}
