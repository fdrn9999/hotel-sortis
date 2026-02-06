/**
 * Cosmetic Store (Pinia)
 * See CLAUDE.md rules - No Pay-to-Win
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  DiceSkin,
  Avatar,
  CollectionResponse,
  EquipCosmeticResponse,
  CosmeticType
} from '@/types/game'
import {
  getAllDiceSkins,
  getAllAvatars,
  getPlayerCollection,
  equipCosmetic as apiEquipCosmetic,
  unequipCosmetic as apiUnequipCosmetic
} from '@/api/cosmetic'

export const useCosmeticStore = defineStore('cosmetic', () => {
  // State
  const diceSkins = ref<DiceSkin[]>([])
  const avatars = ref<Avatar[]>([])
  const ownedDiceSkins = ref<DiceSkin[]>([])
  const ownedAvatars = ref<Avatar[]>([])
  const equippedDiceSkinId = ref<number | null>(null)
  const equippedAvatarId = ref<number | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const equippedDiceSkin = computed(() => {
    if (!equippedDiceSkinId.value) return null
    return diceSkins.value.find(skin => skin.id === equippedDiceSkinId.value) || null
  })

  const equippedAvatar = computed(() => {
    if (!equippedAvatarId.value) return null
    return avatars.value.find(avatar => avatar.id === equippedAvatarId.value) || null
  })

  const ownedDiceSkinsCount = computed(() => {
    return ownedDiceSkins.value.length
  })

  const ownedAvatarsCount = computed(() => {
    return ownedAvatars.value.length
  })

  const diceSkinsByRarity = computed(() => {
    return (rarity: string) => {
      return diceSkins.value.filter(skin => skin.rarity === rarity)
    }
  })

  const avatarsByRarity = computed(() => {
    return (rarity: string) => {
      return avatars.value.filter(avatar => avatar.rarity === rarity)
    }
  })

  // Actions
  /**
   * Load all dice skins (including ownership status)
   */
  async function loadAllDiceSkins(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const skins = await getAllDiceSkins(playerId)
      diceSkins.value = skins
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load dice skins'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Load all avatars (including ownership status)
   */
  async function loadAllAvatars(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const avatarList = await getAllAvatars(playerId)
      avatars.value = avatarList
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load avatars'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Load player collection (owned cosmetics only)
   */
  async function loadCollection(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const collection: CollectionResponse = await getPlayerCollection(playerId)
      ownedDiceSkins.value = collection.diceSkins
      ownedAvatars.value = collection.avatars
      equippedDiceSkinId.value = collection.equippedDiceSkinId || null
      equippedAvatarId.value = collection.equippedAvatarId || null
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load collection'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Equip cosmetic
   */
  async function equipCosmetic(
    playerId: number,
    cosmeticType: CosmeticType,
    cosmeticId: number
  ): Promise<EquipCosmeticResponse> {
    loading.value = true
    error.value = null

    try {
      const response = await apiEquipCosmetic(playerId, cosmeticType, cosmeticId)

      // Update local state
      if (cosmeticType === 'DICE_SKIN') {
        equippedDiceSkinId.value = cosmeticId
        // Update dice skin list
        diceSkins.value = diceSkins.value.map(skin => ({
          ...skin,
          isEquipped: skin.id === cosmeticId
        }))
      } else if (cosmeticType === 'AVATAR') {
        equippedAvatarId.value = cosmeticId
        // Update avatar list
        avatars.value = avatars.value.map(avatar => ({
          ...avatar,
          isEquipped: avatar.id === cosmeticId
        }))
      }

      return response
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to equip cosmetic'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Unequip cosmetic
   */
  async function unequipCosmetic(
    playerId: number,
    cosmeticType: CosmeticType
  ): Promise<EquipCosmeticResponse> {
    loading.value = true
    error.value = null

    try {
      const response = await apiUnequipCosmetic(playerId, cosmeticType)

      // Update local state
      if (cosmeticType === 'DICE_SKIN') {
        equippedDiceSkinId.value = null
        // Update dice skin list
        diceSkins.value = diceSkins.value.map(skin => ({
          ...skin,
          isEquipped: false
        }))
      } else if (cosmeticType === 'AVATAR') {
        equippedAvatarId.value = null
        // Update avatar list
        avatars.value = avatars.value.map(avatar => ({
          ...avatar,
          isEquipped: false
        }))
      }

      return response
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to unequip cosmetic'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Check if a specific cosmetic is owned
   */
  function isCosmeticOwned(cosmeticType: CosmeticType, cosmeticId: number): boolean {
    if (cosmeticType === 'DICE_SKIN') {
      return ownedDiceSkins.value.some(skin => skin.id === cosmeticId)
    } else if (cosmeticType === 'AVATAR') {
      return ownedAvatars.value.some(avatar => avatar.id === cosmeticId)
    }
    return false
  }

  /**
   * Check if a specific cosmetic is equipped
   */
  function isCosmeticEquipped(cosmeticType: CosmeticType, cosmeticId: number): boolean {
    if (cosmeticType === 'DICE_SKIN') {
      return equippedDiceSkinId.value === cosmeticId
    } else if (cosmeticType === 'AVATAR') {
      return equippedAvatarId.value === cosmeticId
    }
    return false
  }

  return {
    // State
    diceSkins,
    avatars,
    ownedDiceSkins,
    ownedAvatars,
    equippedDiceSkinId,
    equippedAvatarId,
    loading,
    error,

    // Getters
    equippedDiceSkin,
    equippedAvatar,
    ownedDiceSkinsCount,
    ownedAvatarsCount,
    diceSkinsByRarity,
    avatarsByRarity,

    // Actions
    loadAllDiceSkins,
    loadAllAvatars,
    loadCollection,
    equipCosmetic,
    unequipCosmetic,
    isCosmeticOwned,
    isCosmeticEquipped
  }
})
