<!--
  스킬 선택 화면 (보스 클리어 후)
  CLAUDE.md 2.2.1절 - 보스 클리어 시 스킬 선택
-->
<template>
  <div class="skill-selection">
    <!-- 배경 오버레이 -->
    <div class="overlay"></div>

    <!-- 메인 컨테이너 -->
    <div class="selection-container">
      <h1 class="title">{{ $t('skillSelection.title') }}</h1>

      <!-- 보스 정보 및 보상 등급 -->
      <div class="boss-info">
        <p class="boss-cleared">{{ getBossName(floor) }}</p>
        <p class="rarity-info">
          <span class="rarity-badge" :class="rarityClass">
            {{ $t(`rarity.${rarity.toLowerCase()}`) }}
          </span>
          {{ $t('skillSelection.rewardInfo') }}
        </p>
      </div>

      <!-- 스킬 카드 3개 -->
      <div class="skill-cards">
        <div
          v-for="skill in offeredSkills"
          :key="skill.id"
          class="skill-card"
          :class="{ selected: selectedSkill?.id === skill.id, [skill.rarity.toLowerCase()]: true }"
          @click="selectSkill(skill)"
        >
          <!-- 스킬 아이콘 -->
          <div class="skill-icon">
            <img v-if="skill.iconUrl" :src="skill.iconUrl" :alt="skill.name" />
            <div v-else class="icon-placeholder">
              {{ skill.name.charAt(0) }}
            </div>
          </div>

          <!-- 스킬 정보 -->
          <div class="skill-info">
            <h3 class="skill-name">{{ skill.name }}</h3>
            <p class="skill-rarity" :class="skill.rarity.toLowerCase()">
              {{ $t(`rarity.${skill.rarity.toLowerCase()}`) }}
            </p>
            <p class="skill-description">{{ skill.description }}</p>
            <p class="skill-trigger">
              <span class="trigger-label">{{ $t('skillSelection.trigger') }}:</span>
              {{ skill.triggerType }}
            </p>
          </div>

          <!-- 선택 표시 -->
          <div v-if="selectedSkill?.id === skill.id" class="selected-indicator">
            <span class="checkmark">✓</span>
          </div>
        </div>
      </div>

      <!-- 안내 문구 -->
      <p class="info-text">{{ $t('skillSelection.selectInfo') }}</p>

      <!-- 확인 버튼 -->
      <button
        class="confirm-btn"
        :disabled="!selectedSkill"
        @click="confirmSelection"
      >
        {{ $t('common.confirm') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Skill, SkillRarity } from '@/types/game'

const { t } = useI18n()

// Props
interface Props {
  floor: number
  offeredSkills: Skill[]
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'skill-selected': [skill: Skill]
  'close': []
}>()

// State
const selectedSkill = ref<Skill | null>(null)

// Computed
const rarity = computed<SkillRarity>(() => {
  if (props.floor === 5) return 'Rare'
  if (props.floor === 10) return 'Epic'
  if (props.floor === 15) return 'Legendary'
  return 'Common'
})

const rarityClass = computed(() => rarity.value.toLowerCase())

// Methods
function selectSkill(skill: Skill) {
  selectedSkill.value = skill
}

function confirmSelection() {
  if (selectedSkill.value) {
    emit('skill-selected', selectedSkill.value)
  }
}

function getBossName(floor: number): string {
  if (floor === 5) return t('bosses.mammon')
  if (floor === 10) return t('bosses.eligor')
  if (floor === 15) return t('bosses.lucifuge')
  return ''
}
</script>

<style scoped>
.skill-selection {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(10px);
}

.selection-container {
  position: relative;
  max-width: 1200px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  background: linear-gradient(135deg, #1b1b27 0%, #2a2a3e 100%);
  border: 3px solid #d4af37;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 10px 50px rgba(212, 175, 55, 0.3);
}

.title {
  font-size: 36px;
  color: #d4af37;
  text-align: center;
  margin-bottom: 20px;
  text-transform: uppercase;
  letter-spacing: 4px;
  font-family: 'Georgia', serif;
}

.boss-info {
  text-align: center;
  margin-bottom: 30px;
}

.boss-cleared {
  font-size: 24px;
  color: #fffdd0;
  margin-bottom: 10px;
}

.rarity-info {
  font-size: 18px;
  color: #cccccc;
}

.rarity-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: bold;
  margin-right: 8px;
}

.rarity-badge.common {
  background: #808080;
  color: white;
}

.rarity-badge.rare {
  background: #4169e1;
  color: white;
}

.rarity-badge.epic {
  background: #9370db;
  color: white;
}

.rarity-badge.legendary {
  background: #ffa500;
  color: white;
}

.skill-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

@media (max-width: 1024px) {
  .skill-cards {
    grid-template-columns: 1fr;
  }
}

.skill-card {
  position: relative;
  background: rgba(42, 42, 62, 0.8);
  border: 2px solid #555;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.skill-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(212, 175, 55, 0.2);
}

.skill-card.selected {
  border-color: #d4af37;
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.5);
}

.skill-card.common {
  border-left: 4px solid #808080;
}

.skill-card.rare {
  border-left: 4px solid #4169e1;
}

.skill-card.epic {
  border-left: 4px solid #9370db;
}

.skill-card.legendary {
  border-left: 4px solid #ffa500;
}

.skill-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 15px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #d4af37;
}

.skill-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.icon-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #3a3a5a, #2a2a4a);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 36px;
  color: #d4af37;
  font-weight: bold;
}

.skill-info {
  text-align: center;
}

.skill-name {
  font-size: 20px;
  color: #fffdd0;
  margin-bottom: 8px;
}

.skill-rarity {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 12px;
  text-transform: uppercase;
}

.skill-rarity.common { color: #808080; }
.skill-rarity.rare { color: #4169e1; }
.skill-rarity.epic { color: #9370db; }
.skill-rarity.legendary { color: #ffa500; }

.skill-description {
  font-size: 14px;
  color: #cccccc;
  line-height: 1.5;
  margin-bottom: 12px;
  min-height: 60px;
}

.skill-trigger {
  font-size: 12px;
  color: #999999;
}

.trigger-label {
  color: #d4af37;
  font-weight: bold;
}

.selected-indicator {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 40px;
  height: 40px;
  background: #d4af37;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.checkmark {
  color: #1b1b27;
  font-size: 24px;
  font-weight: bold;
}

.info-text {
  text-align: center;
  color: #cccccc;
  font-size: 14px;
  margin-bottom: 20px;
  font-style: italic;
}

.confirm-btn {
  display: block;
  width: 300px;
  margin: 0 auto;
  padding: 15px 30px;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  color: #1b1b27;
  border: none;
  border-radius: 4px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.confirm-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 5px 20px rgba(212, 175, 55, 0.5);
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
