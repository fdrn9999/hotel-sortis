import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { campaignApi } from '@/api/campaign'
import type {
  CampaignFloorStatus,
  StartFloorResponse,
  FloorCompleteResponse,
  SkillRewardOption,
  BossPhaseTransition
} from '@/types/game'

export const useCampaignStore = defineStore('campaign', () => {
  // State
  const floors = ref<CampaignFloorStatus[]>([])
  const currentFloor = ref(1)
  const currentBattleIndex = ref(1)
  const totalBattlesOnFloor = ref(1)
  const currentBattleId = ref<number | null>(null)
  const bossId = ref<string | null>(null)
  const bossName = ref<string | null>(null)
  const bossPhase = ref<number | null>(null)
  const bossTotalPhases = ref<number | null>(null)
  const bossesDefeated = ref<Record<string, boolean>>({})
  const totalRuns = ref(0)
  const totalVictories = ref(0)
  const totalDefeats = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const offeredSkills = ref<SkillRewardOption[]>([])

  // Getters
  const isFloorUnlocked = computed(() => (floor: number) => {
    const f = floors.value.find(f => f.floor === floor)
    return f ? !f.locked : false
  })

  const isFloorCleared = computed(() => (floor: number) => {
    const f = floors.value.find(f => f.floor === floor)
    return f ? f.cleared : false
  })

  const currentFloorConfig = computed(() => {
    return floors.value.find(f => f.floor === currentFloor.value) || null
  })

  const isOnBossFloor = computed(() => {
    return currentFloorConfig.value?.floorType === 'BOSS'
  })

  const highestAvailableFloor = computed(() => {
    for (let i = floors.value.length - 1; i >= 0; i--) {
      if (!floors.value[i].locked) return floors.value[i].floor
    }
    return 1
  })

  // Actions
  async function loadCampaignProgress(playerId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await campaignApi.getCampaignProgress(playerId)
      floors.value = response.floors
      bossesDefeated.value = response.bossesDefeated
      totalRuns.value = response.totalRuns
      totalVictories.value = response.totalVictories
      totalDefeats.value = response.totalDefeats
    } catch (e) {
      error.value = 'Failed to load campaign progress'
      // Offline fallback: generate default floor data
      generateOfflineFloors()
    } finally {
      loading.value = false
    }
  }

  async function startFloor(
    playerId: number,
    floor: number,
    equippedSkillIds: number[]
  ): Promise<StartFloorResponse | null> {
    loading.value = true
    error.value = null
    try {
      const response = await campaignApi.startFloor(playerId, floor, equippedSkillIds)
      currentFloor.value = floor
      currentBattleIndex.value = response.battleIndex
      totalBattlesOnFloor.value = response.totalBattles
      currentBattleId.value = response.battleId
      bossId.value = response.bossId || null
      bossName.value = response.bossName || null
      bossPhase.value = response.bossPhase || null
      bossTotalPhases.value = response.bossTotalPhases || null
      return response
    } catch (e) {
      error.value = 'Failed to start floor'
      return null
    } finally {
      loading.value = false
    }
  }

  async function completeFloorBattle(
    playerId: number,
    battleId: number
  ): Promise<FloorCompleteResponse | null> {
    try {
      const response = await campaignApi.completeFloorBattle(battleId, playerId)
      if (response.cleared) {
        // Update local floor status
        const floorStatus = floors.value.find(f => f.floor === currentFloor.value)
        if (floorStatus) floorStatus.cleared = true

        // Unlock next floor
        const nextFloorStatus = floors.value.find(f => f.floor === (currentFloor.value + 1))
        if (nextFloorStatus) nextFloorStatus.locked = false

        totalVictories.value++
      }
      if (response.offeredSkills) {
        offeredSkills.value = response.offeredSkills
      }
      return response
    } catch (e) {
      return null
    }
  }

  function handleBossPhaseTransition(transition: BossPhaseTransition) {
    bossPhase.value = transition.newPhase
    bossTotalPhases.value = transition.totalPhases
  }

  function advanceBattle() {
    currentBattleIndex.value++
  }

  function resetBattleState() {
    currentBattleId.value = null
    bossId.value = null
    bossName.value = null
    bossPhase.value = null
    bossTotalPhases.value = null
    offeredSkills.value = []
  }

  function generateOfflineFloors() {
    const floorData: { type: string; battles: number; boss?: string; ai: number; reward?: string }[] = [
      { type: 'NORMAL', battles: 3, ai: 0 },
      { type: 'NORMAL', battles: 3, ai: 0 },
      { type: 'NORMAL', battles: 4, ai: 0 },
      { type: 'ELITE', battles: 1, ai: 1 },
      { type: 'BOSS', battles: 1, boss: 'mammon', ai: 1, reward: 'Rare' },
      { type: 'NORMAL', battles: 3, ai: 1 },
      { type: 'NORMAL', battles: 4, ai: 1 },
      { type: 'NORMAL', battles: 3, ai: 1 },
      { type: 'ELITE', battles: 1, ai: 2 },
      { type: 'BOSS', battles: 1, boss: 'eligor', ai: 2, reward: 'Epic' },
      { type: 'NORMAL', battles: 4, ai: 2 },
      { type: 'ELITE', battles: 1, ai: 2 },
      { type: 'NORMAL', battles: 4, ai: 2 },
      { type: 'ELITE', battles: 1, ai: 3 },
      { type: 'BOSS', battles: 1, boss: 'lucifuge', ai: 3, reward: 'Legendary' },
    ]

    floors.value = floorData.map((fd, i) => ({
      floor: i + 1,
      floorType: fd.type,
      battleCount: fd.battles,
      cleared: false,
      locked: i > 0,
      description: '',
      aiLevel: fd.ai,
      bossId: fd.boss,
      skillRewardRarity: fd.reward,
    }))
  }

  return {
    // State
    floors,
    currentFloor,
    currentBattleIndex,
    totalBattlesOnFloor,
    currentBattleId,
    bossId,
    bossName,
    bossPhase,
    bossTotalPhases,
    bossesDefeated,
    totalRuns,
    totalVictories,
    totalDefeats,
    loading,
    error,
    offeredSkills,
    // Getters
    isFloorUnlocked,
    isFloorCleared,
    currentFloorConfig,
    isOnBossFloor,
    highestAvailableFloor,
    // Actions
    loadCampaignProgress,
    startFloor,
    completeFloorBattle,
    handleBossPhaseTransition,
    advanceBattle,
    resetBattleState,
  }
})
