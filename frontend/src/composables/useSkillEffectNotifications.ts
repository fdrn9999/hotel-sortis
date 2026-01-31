/**
 * 스킬 효과 알림 관리 Composable
 * CLAUDE.md - 스킬 효과 시각화
 */
import { ref } from 'vue'
import type { Skill } from '@/types/game'

export interface SkillEffectNotification {
  skill: Skill
  effectDescription: string
  duration?: number
}

const notificationQueue = ref<SkillEffectNotification[]>([])
const currentNotification = ref<SkillEffectNotification | null>(null)
const isShowing = ref(false)

export function useSkillEffectNotifications() {
  /**
   * 스킬 효과 알림 추가
   */
  function addNotification(notification: SkillEffectNotification) {
    notificationQueue.value.push(notification)

    // 현재 표시 중이 아니면 바로 표시
    if (!isShowing.value) {
      showNextNotification()
    }
  }

  /**
   * 다음 알림 표시
   */
  function showNextNotification() {
    if (notificationQueue.value.length === 0) {
      isShowing.value = false
      currentNotification.value = null
      return
    }

    isShowing.value = true
    currentNotification.value = notificationQueue.value.shift() || null

    // duration 후 다음 알림
    const duration = currentNotification.value?.duration || 3000
    setTimeout(() => {
      showNextNotification()
    }, duration)
  }

  /**
   * 스킬 발동 시 호출 (간편 헬퍼)
   */
  function notifySkillActivated(
    skill: Skill,
    effectDescription: string,
    duration: number = 3000
  ) {
    addNotification({
      skill,
      effectDescription,
      duration
    })
  }

  /**
   * 모든 알림 클리어
   */
  function clearNotifications() {
    notificationQueue.value = []
    currentNotification.value = null
    isShowing.value = false
  }

  /**
   * 알림 큐 크기
   */
  function getQueueSize() {
    return notificationQueue.value.length
  }

  return {
    // State
    currentNotification,
    isShowing,

    // Methods
    addNotification,
    notifySkillActivated,
    clearNotifications,
    getQueueSize
  }
}
