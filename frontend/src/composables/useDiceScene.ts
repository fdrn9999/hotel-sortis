import { ref, onMounted, onUnmounted, type Ref } from 'vue'
import { DiceScene, type DiceResult } from '@/game/DiceScene'

export function useDiceScene(containerRef: Ref<HTMLElement | null>) {
  const diceScene = ref<DiceScene | null>(null)
  const isRolling = ref(false)
  const lastResult = ref<DiceResult | null>(null)

  onMounted(() => {
    if (containerRef.value) {
      diceScene.value = new DiceScene(containerRef.value)
    }
  })

  onUnmounted(() => {
    if (diceScene.value) {
      diceScene.value.dispose()
      diceScene.value = null
    }
  })

  const roll = (): Promise<DiceResult> => {
    return new Promise((resolve) => {
      if (!diceScene.value || isRolling.value) {
        // 이미 굴리는 중이면 무시
        return
      }

      isRolling.value = true

      diceScene.value.roll((result) => {
        isRolling.value = false
        lastResult.value = result
        resolve(result)
      })
    })
  }

  return {
    diceScene,
    isRolling,
    lastResult,
    roll
  }
}
