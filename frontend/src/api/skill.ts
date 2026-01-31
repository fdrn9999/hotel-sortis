/**
 * 스킬 API 클라이언트 (i18n 지원)
 * CLAUDE.md i18n 규칙 참조
 */
import { apiClient } from './client'
import type { Skill, SkillListResponse } from '@/types/game'

/**
 * 모든 스킬 조회
 * Accept-Language 헤더가 자동으로 포함됨
 */
export async function getAllSkills(): Promise<SkillListResponse> {
  const response = await apiClient.get<SkillListResponse>('/api/v1/skills')
  return response.data
}

/**
 * 희귀도별 스킬 조회
 */
export async function getSkillsByRarity(rarity: string): Promise<SkillListResponse> {
  const response = await apiClient.get<SkillListResponse>(`/api/v1/skills/rarity/${rarity}`)
  return response.data
}

/**
 * 특정 스킬 조회
 */
export async function getSkill(id: number): Promise<Skill> {
  const response = await apiClient.get<Skill>(`/api/v1/skills/${id}`)
  return response.data
}

/**
 * 여러 스킬 ID로 조회 (스킬 장착 시 사용)
 */
export async function getSkillsByIds(ids: number[]): Promise<SkillListResponse> {
  const response = await apiClient.get<SkillListResponse>('/api/v1/skills/batch', {
    params: { ids: ids.join(',') }
  })
  return response.data
}
