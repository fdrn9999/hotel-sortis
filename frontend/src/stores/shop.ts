/**
 * Shop Store (Pinia)
 * No Pay-to-Win - purchase with soul stones only (cosmetics only)
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
   * Load shop (list of purchasable cosmetics)
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
   * Purchase cosmetic
   * No Pay-to-Win - purchase with soul stones only
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
        // Update soul stones
        playerSoulStones.value = response.remainingSoulStones || 0

        // Update shop list (mark purchased item as owned)
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

        // Also update cosmetic store
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
   * Load player wallet (soul stones, ELO)
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
   * Add soul stones (PvP rewards, etc.)
   */
  function addSoulStones(amount: number) {
    playerSoulStones.value += amount
  }

  /**
   * Deduct soul stones (auto-called on purchase)
   */
  function deductSoulStones(amount: number) {
    if (playerSoulStones.value < amount) {
      throw new Error('Not enough soul stones')
    }
    playerSoulStones.value -= amount
  }

  /**
   * Check if a specific item can be purchased
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
   * Clear last purchase result
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
