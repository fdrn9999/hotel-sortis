/**
 * Skill effect notification management Composable
 * CLAUDE.md - Skill effect visualization
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
   * Add skill effect notification
   */
  function addNotification(notification: SkillEffectNotification) {
    notificationQueue.value.push(notification)

    // Show immediately if not currently showing
    if (!isShowing.value) {
      showNextNotification()
    }
  }

  /**
   * Show next notification
   */
  function showNextNotification() {
    if (notificationQueue.value.length === 0) {
      isShowing.value = false
      currentNotification.value = null
      return
    }

    isShowing.value = true
    currentNotification.value = notificationQueue.value.shift() || null

    // Show next notification after duration
    const duration = currentNotification.value?.duration || 3000
    setTimeout(() => {
      showNextNotification()
    }, duration)
  }

  /**
   * Called when skill activates (convenience helper)
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
   * Clear all notifications
   */
  function clearNotifications() {
    notificationQueue.value = []
    currentNotification.value = null
    isShowing.value = false
  }

  /**
   * Notification queue size
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
