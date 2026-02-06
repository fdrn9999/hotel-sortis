/**
 * Cosmetic API client (i18n support)
 * See CLAUDE.md i18n rules
 */
import { apiClient } from './client'
import type {
  DiceSkin,
  Avatar,
  CollectionResponse,
  EquipCosmeticRequest,
  EquipCosmeticResponse,
  CosmeticType
} from '@/types/game'

/**
 * Get all dice skins (including ownership status)
 * Accept-Language header is automatically included
 */
export async function getAllDiceSkins(playerId: number): Promise<DiceSkin[]> {
  const response = await apiClient.get<DiceSkin[]>('/api/v1/cosmetics/dice-skins', {
    params: { playerId }
  })
  return response.data
}

/**
 * Get all avatars (including ownership status)
 */
export async function getAllAvatars(playerId: number): Promise<Avatar[]> {
  const response = await apiClient.get<Avatar[]>('/api/v1/cosmetics/avatars', {
    params: { playerId }
  })
  return response.data
}

/**
 * Get player collection (owned cosmetics only)
 */
export async function getPlayerCollection(playerId: number): Promise<CollectionResponse> {
  const response = await apiClient.get<CollectionResponse>('/api/v1/cosmetics/collection', {
    params: { playerId }
  })
  return response.data
}

/**
 * Equip cosmetic
 */
export async function equipCosmetic(
  playerId: number,
  cosmeticType: CosmeticType,
  cosmeticId: number
): Promise<EquipCosmeticResponse> {
  const request: EquipCosmeticRequest = {
    playerId,
    cosmeticType,
    cosmeticId
  }
  const response = await apiClient.post<EquipCosmeticResponse>('/api/v1/cosmetics/equip', request)
  return response.data
}

/**
 * Unequip cosmetic
 */
export async function unequipCosmetic(
  playerId: number,
  cosmeticType: CosmeticType
): Promise<EquipCosmeticResponse> {
  const response = await apiClient.delete<EquipCosmeticResponse>('/api/v1/cosmetics/equip', {
    params: { playerId, cosmeticType }
  })
  return response.data
}
