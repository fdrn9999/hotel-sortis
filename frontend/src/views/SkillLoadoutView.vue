<template>
  <div class="skill-loadout-view">
    <!-- 네비게이션 바 (CLAUDE.md 3.3.2절) -->
    <div class="app-navigation">
      <button class="nav-btn home-btn" @click="goToHome" aria-label="홈으로">
        <span class="icon">🏠</span>
      </button>
      <button v-if="canGoBack" class="nav-btn back-btn" @click="goBack" aria-label="뒤로가기">
        <span class="icon">←</span>
      </button>
      <h1 class="nav-title">{{ $t('skills.loadout.title') }}</h1>
      <button class="nav-btn settings-btn" @click="goToSettings" aria-label="설정">
        <span class="icon">⚙️</span>
      </button>
    </div>

    <!-- 메인 컨텐츠 -->
    <div class="main-content">
      <!-- 로딩 상태 -->
      <div v-if="skillStore.loading" class="loading">
        <p>{{ $t('common.loading') }}</p>
      </div>

      <!-- 에러 상태 -->
      <div v-else-if="skillStore.error" class="error">
        <p>{{ $t('common.error') }}: {{ skillStore.error }}</p>
        <button @click="skillStore.loadAllSkills()" class="retry-btn">
          {{ $t('common.retry') }}
        </button>
      </div>

      <!-- 스킬 장착 화면 -->
      <div v-else class="loadout-container">
        <!-- 스킬 슬롯 (4개 고정) -->
        <section class="skill-slots-section">
          <h2>{{ $t('skills.loadout.equipped') }} ({{ skillStore.equippedCount }}/4)</h2>

          <div class="skill-slots">
            <div
              v-for="(skill, index) in skillStore.equippedSkills"
              :key="index"
              class="skill-slot"
              :class="{ empty: !skill, filled: skill }"
              @click="onSlotClick(index)"
            >
              <!-- 장착된 스킬 -->
              <div v-if="skill" class="skill-card">
                <div class="skill-header">
                  <span class="skill-name">{{ skill.name }}</span>
                  <span class="skill-rarity" :class="`rarity-${skill.rarity.toLowerCase()}`">
                    {{ skill.rarity }}
                  </span>
                </div>
                <p class="skill-description">{{ skill.description }}</p>
                <span class="skill-trigger">{{ skill.triggerType }}</span>
                <button class="unequip-btn" @click.stop="unequipSkill(index)">
                  ✕
                </button>
              </div>

              <!-- 빈 슬롯 -->
              <div v-else class="empty-slot">
                <span class="slot-number">{{ index + 1 }}</span>
                <p>{{ $t('skills.loadout.emptySlot') }}</p>
              </div>
            </div>
          </div>
        </section>

        <!-- 보유 스킬 목록 -->
        <section class="owned-skills-section">
          <h2>{{ $t('skills.loadout.available') }}</h2>

          <!-- 희귀도별 필터 -->
          <div class="rarity-filters">
            <button
              v-for="rarity in rarities"
              :key="rarity"
              class="filter-btn"
              :class="{ active: selectedRarity === rarity }"
              @click="selectedRarity = rarity"
            >
              {{ rarity }}
            </button>
          </div>

          <!-- 스킬 목록 -->
          <div class="skill-list">
            <div
              v-for="skill in filteredSkills"
              :key="skill.id"
              class="skill-item"
              :class="{
                equipped: skillStore.isSkillEquipped(skill.id),
                'rarity-common': skill.rarity === 'Common',
                'rarity-rare': skill.rarity === 'Rare',
                'rarity-epic': skill.rarity === 'Epic',
                'rarity-legendary': skill.rarity === 'Legendary'
              }"
              @click="equipSkill(skill)"
            >
              <div class="skill-item-header">
                <span class="skill-item-name">{{ skill.name }}</span>
                <span class="skill-item-rarity">{{ skill.rarity }}</span>
              </div>
              <p class="skill-item-description">{{ skill.description }}</p>
              <span class="skill-item-trigger">{{ skill.triggerType }}</span>

              <!-- 장착됨 표시 -->
              <span v-if="skillStore.isSkillEquipped(skill.id)" class="equipped-badge">
                ✓ {{ $t('skills.loadout.equipped') }}
              </span>
            </div>
          </div>
        </section>

        <!-- 전투 시작 버튼 -->
        <div class="action-buttons">
          <button class="start-battle-btn" @click="startBattle" :disabled="!skillStore.validateLoadout()">
            {{ $t('skills.loadout.startBattle') }} ({{ skillStore.equippedCount }}{{ $t('skills.loadout.skillsEquipped') }})
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useSkillStore } from '@/stores/skill'
import { useCampaignStore } from '@/stores/campaign'
import { useNotification } from '@/composables/useNotification'
import type { Skill } from '@/types/game'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const skillStore = useSkillStore()
const campaignStore = useCampaignStore()
const { info, error } = useNotification()

// Campaign context from route query
const isCampaignMode = computed(() => route.query.floor !== undefined)
const campaignFloor = computed(() => Number(route.query.floor) || null)

// State
const selectedRarity = ref<string>('All')
const rarities = ['All', 'Common', 'Rare', 'Epic', 'Legendary']

// Computed
const canGoBack = computed(() => {
  return window.history.length > 1
})

const filteredSkills = computed(() => {
  if (selectedRarity.value === 'All') {
    return skillStore.allSkills
  }
  return skillStore.skillsByRarity(selectedRarity.value)
})

// Methods
function goToHome() {
  router.push('/')
}

function goBack() {
  router.back()
}

function goToSettings() {
  router.push('/settings')
}

function equipSkill(skill: Skill) {
  // 이미 장착되어 있으면 제거
  if (skillStore.isSkillEquipped(skill.id)) {
    const index = skillStore.equippedSkills.findIndex(s => s?.id === skill.id)
    if (index !== -1) {
      skillStore.unequipSkill(index)
    }
    return
  }

  // 빈 슬롯에 장착
  const emptyIndex = skillStore.equippedSkills.findIndex(s => s === null)
  if (emptyIndex !== -1) {
    skillStore.equipSkill(skill, emptyIndex)
  } else {
    // 슬롯 가득 참 (커스텀 모달 사용 - CLAUDE.md 3.3.1절)
    info(t('skills.loadout.slotsFull'))
  }
}

function unequipSkill(index: number) {
  skillStore.unequipSkill(index)
}

function onSlotClick(index: number) {
  // 빈 슬롯 클릭 시 스킬 목록으로 스크롤
  if (!skillStore.equippedSkills[index]) {
    const skillListSection = document.querySelector('.owned-skills-section')
    skillListSection?.scrollIntoView({ behavior: 'smooth' })
  }
}

async function startBattle() {
  if (!skillStore.validateLoadout()) {
    error(t('skills.loadout.invalidLoadout'))
    return
  }

  const equippedSkillIds = skillStore.getEquippedSkillIds()

  // Campaign mode: call campaign API to start floor
  if (isCampaignMode.value && campaignFloor.value) {
    try {
      const playerId = 1 // TODO: Get from auth
      const response = await campaignStore.startFloor(
        playerId,
        campaignFloor.value,
        equippedSkillIds
      )

      if (!response) {
        error('Failed to start floor')
        return
      }

      // Navigate to battle with campaign context
      router.push({
        name: 'battle',
        query: {
          campaignMode: 'true',
          floor: campaignFloor.value.toString(),
          battleIndex: response.battleIndex.toString(),
          totalBattles: response.totalBattles.toString(),
          battleId: response.battleId.toString(),
          bossId: response.bossId || undefined,
          bossName: response.bossName || undefined,
          mutatorId: response.mutatorId || undefined,
          mutatorName: response.mutatorName || undefined,
          mutatorDescription: response.mutatorDescription || undefined,
          mutatorIcon: response.mutatorIcon || undefined,
          skills: equippedSkillIds.join(',')
        }
      })
    } catch (err) {
      console.error('Failed to start campaign floor:', err)
      error('Failed to start floor')
    }
  } else {
    // Normal mode: direct to battle
    router.push({
      name: 'battle',
      query: { skills: equippedSkillIds.join(',') }
    })
  }
}

// Lifecycle
onMounted(async () => {
  // 스킬 목록 로드 (i18n 자동 적용)
  await skillStore.loadAllSkills()
})
</script>

<style scoped>
.skill-loadout-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #1b1b27 0%, #2d2d3f 100%);
  color: #f5f5f5;
}

/* 네비게이션 바 */
.app-navigation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: rgba(27, 27, 39, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 2px solid #d4af37;
  z-index: 1000;
}

.nav-btn {
  width: 44px;
  height: 44px;
  border: none;
  background: transparent;
  color: #d4af37;
  font-size: 24px;
  cursor: pointer;
  transition: transform 0.2s;
}

.nav-btn:hover {
  transform: scale(1.1);
}

.nav-btn:active {
  transform: scale(0.95);
}

.nav-title {
  font-size: 20px;
  font-weight: bold;
  color: #d4af37;
  margin: 0;
}

/* 메인 컨텐츠 */
.main-content {
  margin-top: 60px;
  padding: 20px;
  padding-bottom: 80px;
}

/* 스킬 슬롯 섹션 */
.skill-slots-section {
  margin-bottom: 40px;
}

.skill-slots-section h2 {
  color: #d4af37;
  margin-bottom: 20px;
  font-size: 24px;
}

.skill-slots {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.skill-slot {
  min-height: 150px;
  border: 2px solid #444;
  border-radius: 8px;
  padding: 16px;
  background: rgba(45, 45, 63, 0.8);
  cursor: pointer;
  transition: all 0.3s;
}

.skill-slot.filled {
  border-color: #d4af37;
  background: rgba(212, 175, 55, 0.1);
}

.skill-slot.empty:hover {
  border-color: #666;
  background: rgba(45, 45, 63, 0.9);
}

.skill-card {
  position: relative;
}

.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.skill-name {
  font-size: 18px;
  font-weight: bold;
  color: #f5f5f5;
}

.skill-rarity {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.skill-rarity.rarity-common {
  background: #9e9e9e;
  color: #fff;
}

.skill-rarity.rarity-rare {
  background: #2196f3;
  color: #fff;
}

.skill-rarity.rarity-epic {
  background: #9c27b0;
  color: #fff;
}

.skill-rarity.rarity-legendary {
  background: #ff9800;
  color: #fff;
}

.skill-description {
  font-size: 14px;
  color: #ccc;
  margin-bottom: 8px;
}

.skill-trigger {
  font-size: 12px;
  color: #888;
}

.unequip-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 0, 0, 0.7);
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  transition: background 0.2s;
}

.unequip-btn:hover {
  background: rgba(255, 0, 0, 0.9);
}

.empty-slot {
  text-align: center;
  padding: 32px 16px;
}

.slot-number {
  display: block;
  font-size: 48px;
  color: #444;
  margin-bottom: 8px;
}

/* 보유 스킬 섹션 */
.owned-skills-section {
  margin-bottom: 40px;
}

.owned-skills-section h2 {
  color: #d4af37;
  margin-bottom: 20px;
  font-size: 24px;
}

.rarity-filters {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 8px 16px;
  border: 2px solid #444;
  background: rgba(45, 45, 63, 0.8);
  color: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn.active {
  border-color: #d4af37;
  background: rgba(212, 175, 55, 0.2);
  color: #d4af37;
}

.filter-btn:hover {
  border-color: #666;
}

.skill-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.skill-item {
  position: relative;
  padding: 16px;
  border: 2px solid #444;
  border-radius: 8px;
  background: rgba(45, 45, 63, 0.8);
  cursor: pointer;
  transition: all 0.3s;
}

.skill-item.equipped {
  border-color: #4caf50;
  background: rgba(76, 175, 80, 0.1);
}

.skill-item:hover {
  border-color: #666;
  transform: translateY(-2px);
}

.skill-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.skill-item-name {
  font-size: 16px;
  font-weight: bold;
  color: #f5f5f5;
}

.skill-item-rarity {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: bold;
  background: #444;
  color: #fff;
}

.skill-item-description {
  font-size: 13px;
  color: #ccc;
  margin-bottom: 8px;
}

.skill-item-trigger {
  font-size: 11px;
  color: #888;
}

.equipped-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  background: #4caf50;
  color: #fff;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

/* 전투 시작 버튼 */
.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.start-battle-btn {
  padding: 16px 48px;
  font-size: 20px;
  font-weight: bold;
  color: #1b1b27;
  background: linear-gradient(135deg, #d4af37 0%, #f5e6a8 100%);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.4);
}

.start-battle-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(212, 175, 55, 0.6);
}

.start-battle-btn:active {
  transform: translateY(0);
}

.start-battle-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 로딩/에러 상태 */
.loading,
.error {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.retry-btn {
  margin-top: 16px;
  padding: 12px 24px;
  background: #d4af37;
  color: #1b1b27;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
</style>
