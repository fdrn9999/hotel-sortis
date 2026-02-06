/**
 * Skill API client (i18n support)
 * See CLAUDE.md i18n rules
 */
import { apiClient } from './client'
import type { Skill, SkillListResponse } from '@/types/game'

/**
 * Get all skills
 * Accept-Language header is automatically included
 */
export async function getAllSkills(): Promise<SkillListResponse> {
  const response = await apiClient.get<SkillListResponse>('/api/v1/skills')
  return response.data
}

/**
 * Get skills by rarity
 */
export async function getSkillsByRarity(rarity: string): Promise<SkillListResponse> {
  const response = await apiClient.get<SkillListResponse>(`/api/v1/skills/rarity/${rarity}`)
  return response.data
}

/**
 * Get specific skill
 */
export async function getSkill(id: number): Promise<Skill> {
  const response = await apiClient.get<Skill>(`/api/v1/skills/${id}`)
  return response.data
}

/**
 * Get skills by multiple IDs (used for skill equipping)
 */
export async function getSkillsByIds(ids: number[]): Promise<SkillListResponse> {
  const response = await apiClient.get<SkillListResponse>('/api/v1/skills/batch', {
    params: { ids: ids.join(',') }
  })
  return response.data
}
