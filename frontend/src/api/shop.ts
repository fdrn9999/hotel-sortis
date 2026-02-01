/**
 * 상점 API 클라이언트 (i18n 지원)
 * Pay-to-Win 금지 - 영혼석으로만 구매 가능
 */
import { apiClient } from './client'
import type {
  ShopResponse,
  PurchaseRequest,
  PurchaseResponse,
  PlayerWallet,
  CosmeticType
} from '@/types/game'

/**
 * 상점 조회 (구매 가능한 코스메틱 목록)
 * Accept-Language 헤더가 자동으로 포함됨
 */
export async function getShop(playerId: number): Promise<ShopResponse> {
  const response = await apiClient.get<ShopResponse>('/api/v1/shop', {
    params: { playerId }
  })
  return response.data
}

/**
 * 코스메틱 구매
 */
export async function purchaseCosmetic(
  playerId: number,
  cosmeticType: CosmeticType,
  cosmeticId: number
): Promise<PurchaseResponse> {
  const request: PurchaseRequest = {
    playerId,
    cosmeticType,
    cosmeticId
  }
  const response = await apiClient.post<PurchaseResponse>('/api/v1/shop/purchase', request)
  return response.data
}

/**
 * 플레이어 지갑 조회 (영혼석, ELO)
 */
export async function getPlayerWallet(playerId: number): Promise<PlayerWallet> {
  const response = await apiClient.get<PlayerWallet>('/api/v1/shop/wallet', {
    params: { playerId }
  })
  return response.data
}
