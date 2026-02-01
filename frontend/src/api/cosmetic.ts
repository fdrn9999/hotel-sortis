/**
 * 코스메틱 API 클라이언트 (i18n 지원)
 * CLAUDE.md i18n 규칙 참조
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
 * 모든 주사위 스킨 조회 (소유 여부 포함)
 * Accept-Language 헤더가 자동으로 포함됨
 */
export async function getAllDiceSkins(playerId: number): Promise<DiceSkin[]> {
  const response = await apiClient.get<DiceSkin[]>('/api/v1/cosmetics/dice-skins', {
    params: { playerId }
  })
  return response.data
}

/**
 * 모든 아바타 조회 (소유 여부 포함)
 */
export async function getAllAvatars(playerId: number): Promise<Avatar[]> {
  const response = await apiClient.get<Avatar[]>('/api/v1/cosmetics/avatars', {
    params: { playerId }
  })
  return response.data
}

/**
 * 플레이어 컬렉션 조회 (보유 중인 코스메틱만)
 */
export async function getPlayerCollection(playerId: number): Promise<CollectionResponse> {
  const response = await apiClient.get<CollectionResponse>('/api/v1/cosmetics/collection', {
    params: { playerId }
  })
  return response.data
}

/**
 * 코스메틱 장착
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
 * 코스메틱 장착 해제
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
