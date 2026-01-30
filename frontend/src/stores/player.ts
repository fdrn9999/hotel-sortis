import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Skill, EquippedSkills } from '@/types/game'

export const usePlayerStore = defineStore('player', () => {
  const id = ref<number | null>(null)
  const username = ref('')
  const elo = ref(1000)
  const soulStones = ref(0)
  const currentFloor = ref(1)
  const highestFloorCleared = ref(0)
  const hp = ref(100)
  const unlockedSkills = ref<Skill[]>([])
  const equippedSkills = ref<EquippedSkills>([null, null, null, null])

  const isLoggedIn = computed(() => id.value !== null)

  const setPlayer = (playerData: {
    id: number
    username: string
    elo: number
    soulStones: number
    currentFloor: number
    highestFloorCleared: number
  }) => {
    id.value = playerData.id
    username.value = playerData.username
    elo.value = playerData.elo
    soulStones.value = playerData.soulStones
    currentFloor.value = playerData.currentFloor
    highestFloorCleared.value = playerData.highestFloorCleared
  }

  const equipSkill = (slot: number, skill: Skill | null) => {
    if (slot >= 0 && slot < 4) {
      equippedSkills.value[slot] = skill
    }
  }

  const takeDamage = (damage: number) => {
    hp.value = Math.max(0, hp.value - damage)
  }

  const resetHP = () => {
    hp.value = 100
  }

  return {
    id,
    username,
    elo,
    soulStones,
    currentFloor,
    highestFloorCleared,
    hp,
    unlockedSkills,
    equippedSkills,
    isLoggedIn,
    setPlayer,
    equipSkill,
    takeDamage,
    resetHP
  }
})
