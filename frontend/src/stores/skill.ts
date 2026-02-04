/**
 * 스킬 Store (Pinia)
 * CLAUDE.md 규칙 참조 - 최대 4개 스킬 장착
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
   * 모든 스킬 로드 (i18n 자동 적용)
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
   * 스킬 장착
   * CLAUDE.md 1.1.1절 - 최대 4개
   */
  function equipSkill(skill: Skill, slotIndex: number) {
    // 슬롯 인덱스 검증
    if (slotIndex < 0 || slotIndex >= 4) {
      throw new Error(`Invalid slot index: ${slotIndex}. Must be 0-3`)
    }

    // 이미 다른 슬롯에 장착되어 있는지 확인
    const existingIndex = equippedSkills.value.findIndex(s => s?.id === skill.id)
    if (existingIndex !== -1 && existingIndex !== slotIndex) {
      // 기존 슬롯에서 제거
      equippedSkills.value[existingIndex] = null
    }

    // 슬롯에 스킬 장착
    equippedSkills.value[slotIndex] = skill
  }

  /**
   * 스킬 장착 해제
   */
  function unequipSkill(slotIndex: number) {
    if (slotIndex < 0 || slotIndex >= 4) {
      throw new Error(`Invalid slot index: ${slotIndex}. Must be 0-3`)
    }

    equippedSkills.value[slotIndex] = null
  }

  /**
   * 모든 스킬 장착 해제
   */
  function unequipAllSkills() {
    equippedSkills.value = [null, null, null, null]
  }

  /**
   * 장착된 스킬 ID 목록 가져오기
   */
  function getEquippedSkillIds(): number[] {
    return equippedSkills.value
      .filter(skill => skill !== null)
      .map(skill => skill!.id)
  }

  /**
   * 특정 스킬이 장착되어 있는지 확인
   */
  function isSkillEquipped(skillId: number): boolean {
    return equippedSkills.value.some(skill => skill?.id === skillId)
  }

  /**
   * 장착 검증 (전투 시작 전)
   */
  function validateLoadout(): boolean {
    const equipped = equippedSkills.value.filter(s => s !== null)

    // 0~4개 사이여야 함
    if (equipped.length > 4) {
      return false
    }

    // 중복 체크
    const ids = equipped.map(s => s!.id)
    const unique = new Set(ids)
    if (ids.length !== unique.size) {
      return false
    }

    return true
  }

  /**
   * 장착된 스킬 로드 (저장된 ID 목록으로부터)
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

      // ID 순서대로 장착
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
