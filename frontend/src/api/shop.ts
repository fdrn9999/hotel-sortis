/**
 * Shop API client (i18n support)
 * No Pay-to-Win - purchase with soul stones only
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
 * Get shop (list of purchasable cosmetics)
 * Accept-Language header is automatically included
 */
export async function getShop(playerId: number): Promise<ShopResponse> {
  const response = await apiClient.get<ShopResponse>('/api/v1/shop', {
    params: { playerId }
  })
  return response.data
}

/**
 * Purchase cosmetic
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
 * Get player wallet (soul stones, ELO)
 */
export async function getPlayerWallet(playerId: number): Promise<PlayerWallet> {
  const response = await apiClient.get<PlayerWallet>('/api/v1/shop/wallet', {
    params: { playerId }
  })
  return response.data
}
