/**
 * 코스메틱 Store (Pinia)
 * CLAUDE.md 규칙 참조 - Pay-to-Win 금지
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
   * 모든 주사위 스킨 로드 (소유 여부 포함)
   */
  async function loadAllDiceSkins(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const skins = await getAllDiceSkins(playerId)
      diceSkins.value = skins
      console.log(`Loaded ${skins.length} dice skins`)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load dice skins'
      console.error('Failed to load dice skins:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 모든 아바타 로드 (소유 여부 포함)
   */
  async function loadAllAvatars(playerId: number) {
    loading.value = true
    error.value = null

    try {
      const avatarList = await getAllAvatars(playerId)
      avatars.value = avatarList
      console.log(`Loaded ${avatarList.length} avatars`)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load avatars'
      console.error('Failed to load avatars:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 플레이어 컬렉션 로드 (보유 중인 코스메틱만)
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

      console.log(`Loaded collection: ${collection.diceSkins.length} dice skins, ${collection.avatars.length} avatars`)
      console.log(`Equipped: Dice skin ${equippedDiceSkinId.value}, Avatar ${equippedAvatarId.value}`)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load collection'
      console.error('Failed to load collection:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 코스메틱 장착
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

      // 로컬 상태 업데이트
      if (cosmeticType === 'DICE_SKIN') {
        equippedDiceSkinId.value = cosmeticId
        // 주사위 스킨 목록 업데이트
        diceSkins.value = diceSkins.value.map(skin => ({
          ...skin,
          isEquipped: skin.id === cosmeticId
        }))
      } else if (cosmeticType === 'AVATAR') {
        equippedAvatarId.value = cosmeticId
        // 아바타 목록 업데이트
        avatars.value = avatars.value.map(avatar => ({
          ...avatar,
          isEquipped: avatar.id === cosmeticId
        }))
      }

      console.log(`Equipped ${cosmeticType}: ${cosmeticId}`)
      return response
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to equip cosmetic'
      console.error('Failed to equip cosmetic:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 코스메틱 장착 해제
   */
  async function unequipCosmetic(
    playerId: number,
    cosmeticType: CosmeticType
  ): Promise<EquipCosmeticResponse> {
    loading.value = true
    error.value = null

    try {
      const response = await apiUnequipCosmetic(playerId, cosmeticType)

      // 로컬 상태 업데이트
      if (cosmeticType === 'DICE_SKIN') {
        equippedDiceSkinId.value = null
        // 주사위 스킨 목록 업데이트
        diceSkins.value = diceSkins.value.map(skin => ({
          ...skin,
          isEquipped: false
        }))
      } else if (cosmeticType === 'AVATAR') {
        equippedAvatarId.value = null
        // 아바타 목록 업데이트
        avatars.value = avatars.value.map(avatar => ({
          ...avatar,
          isEquipped: false
        }))
      }

      console.log(`Unequipped ${cosmeticType}`)
      return response
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to unequip cosmetic'
      console.error('Failed to unequip cosmetic:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 특정 코스메틱이 소유되어 있는지 확인
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
   * 특정 코스메틱이 장착되어 있는지 확인
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
