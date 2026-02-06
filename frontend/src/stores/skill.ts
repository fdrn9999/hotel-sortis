/**
 * Skill Store (Pinia)
 * See CLAUDE.md rules - max 4 skills equipped
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Skill, SkillListResponse, EquippedSkills } from '@/types/game'
import { getAllSkills, getSkillsByIds } from '@/api/skill'

export const useSkillStore = defineStore('skill', () => {
  // State
  const allSkills = ref<Skill[]>([])
  const equippedSkills = ref<EquippedSkills>([null, null, null, null])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const equippedCount = computed(() => {
    return equippedSkills.value.filter(skill => skill !== null).length
  })

  const canEquipMore = computed(() => {
    return equippedCount.value < 4
  })

  const skillsByRarity = computed(() => {
    return (rarity: string) => {
      return allSkills.value.filter(skill => skill.rarity === rarity)
    }
  })

  // Actions
  /**
   * Load all skills (i18n auto-applied)
   */
  async function loadAllSkills() {
    loading.value = true
    error.value = null

    try {
      const response: SkillListResponse = await getAllSkills()
      allSkills.value = response.skills
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load skills'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Equip skill
   * CLAUDE.md 1.1.1 - max 4 skills
   */
  function equipSkill(skill: Skill, slotIndex: number) {
    // Validate slot index
    if (slotIndex < 0 || slotIndex >= 4) {
      throw new Error(`Invalid slot index: ${slotIndex}. Must be 0-3`)
    }

    // Check if already equipped in another slot
    const existingIndex = equippedSkills.value.findIndex(s => s?.id === skill.id)
    if (existingIndex !== -1 && existingIndex !== slotIndex) {
      // Remove from existing slot
      equippedSkills.value[existingIndex] = null
    }

    // Equip skill to slot
    equippedSkills.value[slotIndex] = skill
  }

  /**
   * Unequip skill
   */
  function unequipSkill(slotIndex: number) {
    if (slotIndex < 0 || slotIndex >= 4) {
      throw new Error(`Invalid slot index: ${slotIndex}. Must be 0-3`)
    }

    equippedSkills.value[slotIndex] = null
  }

  /**
   * Unequip all skills
   */
  function unequipAllSkills() {
    equippedSkills.value = [null, null, null, null]
  }

  /**
   * Get list of equipped skill IDs
   */
  function getEquippedSkillIds(): number[] {
    return equippedSkills.value
      .filter(skill => skill !== null)
      .map(skill => skill!.id)
  }

  /**
   * Check if a specific skill is equipped
   */
  function isSkillEquipped(skillId: number): boolean {
    return equippedSkills.value.some(skill => skill?.id === skillId)
  }

  /**
   * Validate loadout (before battle start)
   */
  function validateLoadout(): boolean {
    const equipped = equippedSkills.value.filter(s => s !== null)

    // Must be between 0-4 skills
    if (equipped.length > 4) {
      return false
    }

    // Check for duplicates
    const ids = equipped.map(s => s!.id)
    const unique = new Set(ids)
    if (ids.length !== unique.size) {
      return false
    }

    return true
  }

  /**
   * Load equipped skills (from saved ID list)
   */
  async function loadEquippedSkills(skillIds: number[]) {
    if (!skillIds || skillIds.length === 0) {
      unequipAllSkills()
      return
    }

    if (skillIds.length > 4) {
      throw new Error('Cannot load more than 4 skills')
    }

    loading.value = true
    error.value = null

    try {
      const response: SkillListResponse = await getSkillsByIds(skillIds)

      // Equip in ID order
      skillIds.forEach((id, index) => {
        const skill = response.skills.find(s => s.id === id)
        if (skill) {
          equippedSkills.value[index] = skill
        }
      })
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load equipped skills'
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    allSkills,
    equippedSkills,
    loading,
    error,

    // Getters
    equippedCount,
    canEquipMore,
    skillsByRarity,

    // Actions
    loadAllSkills,
    equipSkill,
    unequipSkill,
    unequipAllSkills,
    getEquippedSkillIds,
    isSkillEquipped,
    validateLoadout,
    loadEquippedSkills
  }
})
