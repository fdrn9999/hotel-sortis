<template>
  <div class="pvp-matchmaking-view">
    <!-- 네비게이션 바 -->
    <div class="nav-bar">
      <button class="nav-btn" @click="goHome" aria-label="홈으로">
        ← {{ t('common.home') }}
      </button>
    </div>

    <div class="matchmaking-container">
      <h1 class="title">{{ t('pvp.matchmaking.title') }}</h1>

      <!-- 플레이어 정보 -->
      <div class="player-info">
        <div class="elo-display">
          <span class="label">{{ t('pvp.elo') }}:</span>
          <span class="value">{{ playerElo }}</span>
        </div>
        <div class="tier-display">
          <span class="label">{{ t('pvp.tier') }}:</span>
          <span class="value" :style="{ color: tierColor }">{{ tierName }}</span>
        </div>
      </div>

      <!-- 매칭 상태 -->
      <div v-if="!isMatching && !matchFound" class="matching-idle">
        <p class="description">{{ t('pvp.matchmaking.description') }}</p>
        <button class="btn-primary btn-large" @click="startMatching" :disabled="loading">
          {{ loading ? t('pvp.matchmaking.starting') : t('pvp.matchmaking.start') }}
        </button>
      </div>

      <!-- 매칭 중 -->
      <div v-else-if="isMatching && !matchFound" class="matching-active">
        <div class="spinner"></div>
        <h2>{{ t('pvp.matchmaking.searching') }}</h2>
        <p class="wait-time">
          {{ t('pvp.matchmaking.waitTime') }}: {{ waitTimeSeconds }}s
        </p>
        <p class="queue-size">
          {{ t('pvp.matchmaking.queueSize') }}: {{ queueSize }}
        </p>
        <p class="elo-range">
          {{ t('pvp.matchmaking.eloRange') }}: {{ playerElo - eloRange }} ~ {{ playerElo + eloRange }}
        </p>
        <button class="btn-secondary" @click="cancelMatching">
          {{ t('pvp.matchmaking.cancel') }}
        </button>
      </div>

      <!-- 매치 찾음 -->
      <div v-else-if="matchFound" class="match-found">
        <h2 class="success">{{ t('pvp.matchmaking.matchFound') }}</h2>
        <div class="opponent-info">
          <p>{{ t('pvp.matchmaking.opponent') }}: Player {{ opponentId }}</p>
          <p>{{ t('pvp.elo') }}: {{ opponentElo }}</p>
        </div>
        <p class="redirecting">{{ t('pvp.matchmaking.redirecting') }}</p>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { joinMatchmakingQueue, leaveMatchmakingQueue, findMatch, calculateTier, getTierColor } from '@/api/pvp'
import { usePvPWebSocket } from '@/composables/usePvPWebSocket'
import { useConfirmModal } from '@/composables/useConfirmModal'
import type { MatchFoundResponse } from '@/types/game'

const router = useRouter()
const { t } = useI18n()
const { confirm } = useConfirmModal()

// 플레이어 정보 (임시 - 실제로는 인증에서 가져와야 함)
const playerId = ref(1)
const playerElo = ref(1000)

// 매칭 상태
const isMatching = ref(false)
const matchFound = ref(false)
const loading = ref(false)
const error = ref('')

// 대기 정보
const queueSize = ref(0)
const waitTimeSeconds = ref(0)
const eloRange = ref(150)

// 매치 정보
const opponentId = ref(0)
const opponentElo = ref(0)
const battleId = ref(0)

// 폴링 인터벌
let pollingInterval: number | null = null
let waitTimeInterval: number | null = null

// 티어 계산
const tier = computed(() => calculateTier(playerElo.value))
const tierColor = computed(() => getTierColor(tier.value))
const tierName = computed(() => t(`pvp.tiers.${tier.value.toLowerCase()}`))

// WebSocket
const {
  connected,
  subscribePvP,
  unsubscribeAll,
  onMatchFound: wsOnMatchFound
} = usePvPWebSocket(playerId.value)

onMounted(() => {
  // WebSocket 구독
  subscribePvP()

  // 매치 찾기 이벤트 핸들러
  wsOnMatchFound.value = (match: MatchFoundResponse) => {
    console.log('Match found via WebSocket:', match)
    handleMatchFound(match)
  }
})

onUnmounted(() => {
  // 매칭 중이면 취소
  if (isMatching.value) {
    cancelMatching()
  }
  unsubscribeAll()
})

/**
 * 매칭 시작
 */
const startMatching = async () => {
  loading.value = true
  error.value = ''

  try {
    // 대기열 참가
    const response = await joinMatchmakingQueue(playerId.value)
    playerElo.value = response.elo
    queueSize.value = response.queueSize

    isMatching.value = true
    loading.value = false

    // 대기 시간 카운터 시작
    waitTimeSeconds.value = 0
    waitTimeInterval = window.setInterval(() => {
      waitTimeSeconds.value++

      // ELO 범위 확대 (30초마다 +50)
      if (waitTimeSeconds.value % 30 === 0) {
        eloRange.value += 50
      }

      // 3분 초과 시 범위 무제한
      if (waitTimeSeconds.value >= 180) {
        eloRange.value = 9999
      }
    }, 1000)

    // 폴링 시작 (2초마다 상대 찾기)
    pollingInterval = window.setInterval(async () => {
      try {
        const match = await findMatch(playerId.value)
        if (match) {
          handleMatchFound(match)
        }
      } catch (err: any) {
        console.error('Polling error:', err)
      }
    }, 2000)

  } catch (err: any) {
    console.error('Failed to join queue:', err)
    error.value = err.response?.data?.message || t('pvp.matchmaking.errorJoin')
    loading.value = false
    isMatching.value = false
  }
}

/**
 * 매칭 취소
 */
const cancelMatching = async () => {
  try {
    await leaveMatchmakingQueue(playerId.value)
    stopMatching()
  } catch (err: any) {
    console.error('Failed to leave queue:', err)
    error.value = err.response?.data?.message || t('pvp.matchmaking.errorLeave')
  }
}

/**
 * 매칭 중지
 */
const stopMatching = () => {
  isMatching.value = false

  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }

  if (waitTimeInterval) {
    clearInterval(waitTimeInterval)
    waitTimeInterval = null
  }

  waitTimeSeconds.value = 0
  eloRange.value = 150
}

/**
 * 매치 찾음 처리
 */
const handleMatchFound = (match: MatchFoundResponse) => {
  stopMatching()
  matchFound.value = true
  battleId.value = match.battleId
  opponentId.value = playerId.value === match.player1Id ? match.player2Id : match.player1Id
  opponentElo.value = playerId.value === match.player1Id ? match.player2Elo : match.player1Elo

  // 2초 후 전투 화면으로 이동
  setTimeout(() => {
    router.push({
      name: 'Battle',
      params: { battleId: battleId.value }
    })
  }, 2000)
}

/**
 * 홈으로
 */
const goHome = async () => {
  if (isMatching.value) {
    // CLAUDE.md 3.3.1 규칙 준수: confirm() 대체
    const confirmed = await confirm({
      title: t('pvp.matchmaking.cancel'),
      message: t('pvp.matchmaking.confirmLeave'),
      confirmText: t('common.confirm'),
      cancelText: t('common.cancel')
    })

    if (confirmed) {
      cancelMatching()
      router.push('/')
    }
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.pvp-matchmaking-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #1b1b27 0%, #0d0d0d 100%);
  color: #fffdd0;
  padding: 20px;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.nav-btn {
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid #d4af37;
  color: #d4af37;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.nav-btn:hover {
  background: rgba(212, 175, 55, 0.3);
  transform: translateY(-2px);
}

.matchmaking-container {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.title {
  font-size: 48px;
  color: #d4af37;
  margin-bottom: 40px;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.5);
}

.player-info {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 40px;
  font-size: 20px;
}

.label {
  color: #fffdd0;
  margin-right: 10px;
}

.value {
  color: #d4af37;
  font-weight: bold;
}

.matching-idle .description {
  font-size: 18px;
  margin-bottom: 30px;
  color: #c0c0c0;
}

.btn-primary,
.btn-secondary {
  padding: 15px 40px;
  font-size: 20px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: bold;
}

.btn-primary {
  background: linear-gradient(135deg, #d4af37 0%, #ffd700 100%);
  color: #1b1b27;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(212, 175, 55, 0.5);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: rgba(139, 0, 0, 0.5);
  color: #fffdd0;
  border: 1px solid #8b0000;
  margin-top: 20px;
}

.btn-secondary:hover {
  background: rgba(139, 0, 0, 0.7);
}

.matching-active {
  padding: 40px;
}

.spinner {
  width: 80px;
  height: 80px;
  border: 8px solid rgba(212, 175, 55, 0.2);
  border-top-color: #d4af37;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 30px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.matching-active h2 {
  font-size: 32px;
  color: #d4af37;
  margin-bottom: 20px;
}

.wait-time,
.queue-size,
.elo-range {
  font-size: 18px;
  margin: 10px 0;
  color: #c0c0c0;
}

.match-found h2 {
  font-size: 36px;
  color: #50c878;
  margin-bottom: 30px;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.opponent-info {
  font-size: 20px;
  margin-bottom: 20px;
}

.redirecting {
  color: #c0c0c0;
  font-style: italic;
}

.error-message {
  background: rgba(139, 0, 0, 0.3);
  border: 1px solid #8b0000;
  color: #ff6b6b;
  padding: 15px;
  border-radius: 5px;
  margin-top: 20px;
}
</style>
