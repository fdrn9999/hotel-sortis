/**
 * 상점 Store (Pinia)
 * Pay-to-Win 금지 - 영혼석으로만 구매 가능 (코스메틱 전용)
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  ShopItem,
  ShopResponse,
  PurchaseResponse,
  PlayerWallet,
  CosmeticType
} from '@/types/game'
import {
  getShop,
  purchaseCosmetic as apiPurchaseCosmetic,
  getPlayerWallet as apiGetPlayerWallet
} from '@/api/shop'
import { useCosmeticStore } from './cosmetic'

export const useShopStore = defineStore('shop', () => {
  // State
  const diceSkins = ref<ShopItem[]>([])
  const avatars = ref<ShopItem[]>([])
  const playerSoulStones = ref(0)
  const playerElo = ref(1000)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const lastPurchase = ref<PurchaseResponse | null>(null)

  // Getters
  const totalItems = computed(() => {
    return diceSkins.value.length + avatars.value.length
  })

  const purchasableItems = computed(() => {
    return [...diceSkins.value, ...avatars.value].filter(item => item.isPurchasable)
  })

  const purchasableCount = computed(() => {
    return purchasableItems.value.length
  })

  const canAfford = computed(() => {
    return (price: number) => {
      return playerSoulStones.value >= price
    }
  })

  const itemsByRarity = computed(() => {
    return (rarity: string) => {
      return [...diceSkins.value, ...avatars.value].filter(item => item.rarity === rarity)
    }
  })

  // Actions
  /**
   * 상점 로드 (구매 가능한 코스메틱 목록)
   */
  async function loadShop(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const response: ShopResponse = await getShop(playerId)
      diceSkins.value = response.diceSkins
      avatars.value = response.avatars
      playerSoulStones.value = response.playerSoulStones
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load shop'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 코스메틱 구매
   * Pay-to-Win 금지 - 영혼석으로만 구매
   */
  async function purchaseCosmetic(
    playerId: number,
    cosmeticType: CosmeticType,
    cosmeticId: number
  ): Promise<PurchaseResponse> {
    loading.value = true
    error.value = null
    lastPurchase.value = null

    try {
      const response = await apiPurchaseCosmetic(playerId, cosmeticType, cosmeticId)
      lastPurchase.value = response

      if (response.success) {
        // 영혼석 업데이트
        playerSoulStones.value = response.remainingSoulStones || 0

        // 상점 목록 업데이트 (구매한 아이템 제거)
        if (cosmeticType === 'DICE_SKIN') {
          diceSkins.value = diceSkins.value.map(skin => ({
            ...skin,
            isOwned: skin.id === cosmeticId ? true : skin.isOwned,
            isPurchasable: skin.id === cosmeticId ? false : skin.isPurchasable
          }))
        } else if (cosmeticType === 'AVATAR') {
          avatars.value = avatars.value.map(avatar => ({
            ...avatar,
            isOwned: avatar.id === cosmeticId ? true : avatar.isOwned,
            isPurchasable: avatar.id === cosmeticId ? false : avatar.isPurchasable
          }))
        }

        // 코스메틱 스토어도 업데이트
        const cosmeticStore = useCosmeticStore()
        await cosmeticStore.loadCollection(playerId)
      }

      return response
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to purchase cosmetic'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 플레이어 지갑 로드 (영혼석, ELO)
   */
  async function loadPlayerWallet(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const wallet: PlayerWallet = await apiGetPlayerWallet(playerId)
      playerSoulStones.value = wallet.soulStones
      playerElo.value = wallet.elo
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load wallet'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 영혼석 추가 (PvP 보상 등)
   */
  function addSoulStones(amount: number) {
    playerSoulStones.value += amount
  }

  /**
   * 영혼석 차감 (구매 시 자동 호출됨)
   */
  function deductSoulStones(amount: number) {
    if (playerSoulStones.value < amount) {
      throw new Error('Not enough soul stones')
    }
    playerSoulStones.value -= amount
  }

  /**
   * 특정 아이템을 구매할 수 있는지 확인
   */
  function canPurchase(cosmeticType: CosmeticType, cosmeticId: number): boolean {
    const items = cosmeticType === 'DICE_SKIN' ? diceSkins.value : avatars.value
    const item = items.find(i => i.id === cosmeticId)

    if (!item) {
      return false
    }

    return item.isPurchasable && playerSoulStones.value >= item.price
  }

  /**
   * 마지막 구매 결과 초기화
   */
  function clearLastPurchase() {
    lastPurchase.value = null
  }

  return {
    // State
    diceSkins,
    avatars,
    playerSoulStones,
    playerElo,
    loading,
    error,
    lastPurchase,

    // Getters
    totalItems,
    purchasableItems,
    purchasableCount,
    canAfford,
    itemsByRarity,

    // Actions
    loadShop,
    purchaseCosmetic,
    loadPlayerWallet,
    addSoulStones,
    deductSoulStones,
    canPurchase,
    clearLastPurchase
  }
})
